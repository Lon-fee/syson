/*******************************************************************************
 * Copyright (c) 2025 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.syson.ai.services.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.syson.ai.configuration.AIServiceConfiguration;
import org.eclipse.syson.ai.prompt.SysMLPromptManager;
import org.eclipse.syson.ai.services.AnalysisResult;
import org.eclipse.syson.ai.services.IAIService;
import org.eclipse.syson.ai.services.ModelContext;
import org.eclipse.syson.ai.services.Suggestion;
import org.eclipse.syson.ai.services.ValidationIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Google Gemini AI service implementation.
 * Uses Google's Generative Language API for SysML V2 assistance.
 *
 * @author AI Services Team
 */
@Service
public class GeminiAIService implements IAIService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiAIService.class);
    
    private final WebClient webClient;
    private final AIServiceConfiguration config;
    private final SysMLPromptManager promptManager;
    
    public GeminiAIService(AIServiceConfiguration config, SysMLPromptManager promptManager) {
        this.config = config;
        this.promptManager = promptManager;
        
        // Create WebClient for Gemini API
        this.webClient = WebClient.builder()
            .baseUrl(config.getGemini().getEndpoint())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
        
        LOGGER.info("Initialized Gemini AI Service with model: {}", config.getGemini().getModel());
    }
    
    @Override
    public CompletableFuture<String> chat(String message, ModelContext context) {
        String prompt = promptManager.buildPrompt(message, context);
        return callGemini(prompt, false).toFuture();
    }
    
    @Override
    public Flux<String> chatStream(String message, ModelContext context) {
        String prompt = promptManager.buildPrompt(message, context);
        return callGeminiStream(prompt);
    }
    
    @Override
    public CompletableFuture<AnalysisResult> analyzeModel(String modelContent, String elementType) {
        String prompt = String.format("""
            Analyze the following SysML V2 %s:
            
            ```sysml
            %s
            ```
            
            Provide:
            1. Brief summary
            2. Detailed analysis
            3. Improvement suggestions
            4. Potential issues
            """, elementType, modelContent);
        
        return callGemini(prompt, false)
            .map(response -> parseAnalysisResult(response))
            .toFuture();
    }
    
    @Override
    public CompletableFuture<List<Suggestion>> provideSuggestions(ModelContext context) {
        String prompt = "Based on the current modeling context, provide 3-5 helpful suggestions for the user.";
        
        return callGemini(promptManager.buildPrompt(prompt, context), false)
            .map(response -> parseSuggestions(response))
            .toFuture();
    }
    
    @Override
    public CompletableFuture<List<ValidationIssue>> validateElement(String elementContent) {
        String prompt = String.format("""
            Validate the following SysML V2 code for syntax and semantic correctness:
            
            ```sysml
            %s
            ```
            
            List any errors, warnings, or suggestions.
            """, elementContent);
        
        return callGemini(prompt, false)
            .map(response -> parseValidationIssues(response))
            .toFuture();
    }
    
    @Override
    public CompletableFuture<String> generateCode(String description, ModelContext context) {
        String prompt = promptManager.buildCodeGenerationPrompt(description, context);
        return callGemini(prompt, true).toFuture();
    }
    
    @Override
    public Flux<String> generateCodeStream(String description, ModelContext context) {
        String prompt = promptManager.buildCodeGenerationPrompt(description, context);
        return callGeminiStream(prompt);
    }
    
    /**
     * Call Gemini API with the given prompt.
     */
    private Mono<String> callGemini(String prompt, boolean codeMode) {
        var requestBody = buildGeminiRequest(prompt, codeMode);
        
        return webClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/models/{model}:generateContent")
                .queryParam("key", config.getGemini().getApiKey())
                .build(config.getGemini().getModel()))
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .timeout(Duration.ofMillis(config.getTimeout()))
            .map(this::extractTextFromResponse)
            .doOnError(error -> LOGGER.error("Gemini API call failed", error))
            .onErrorResume(error -> Mono.just("Error: " + error.getMessage()));
    }
    
    /**
     * Call Gemini API with streaming response.
     */
    private Flux<String> callGeminiStream(String prompt) {
        var requestBody = buildGeminiRequest(prompt, false);
        
        return webClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/models/{model}:streamGenerateContent")
                .queryParam("key", config.getGemini().getApiKey())
                .queryParam("alt", "sse")
                .build(config.getGemini().getModel()))
            .bodyValue(requestBody)
            .retrieve()
            .bodyToFlux(String.class)
            .timeout(Duration.ofMillis(config.getTimeout()))
            .mapNotNull(this::extractTextFromStreamChunk)
            .doOnError(error -> LOGGER.error("Gemini streaming failed", error));
    }
    
    /**
     * Build Gemini API request body.
     */
    private Map<String, Object> buildGeminiRequest(String prompt, boolean codeMode) {
        var generationConfig = Map.of(
            "temperature", config.getGemini().getTemperature(),
            "topK", config.getGemini().getTopK(),
            "topP", config.getGemini().getTopP(),
            "maxOutputTokens", config.getGemini().getMaxOutputTokens()
        );
        
        var content = Map.of(
            "parts", List.of(
                Map.of("text", prompt)
            )
        );
        
        return Map.of(
            "contents", List.of(content),
            "generationConfig", generationConfig
        );
    }
    
    /**
     * Extract text from Gemini API response.
     */
    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            var candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                var content = (Map<String, Object>) candidates.get(0).get("content");
                var parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
            return "No response generated";
        } catch (Exception e) {
            LOGGER.error("Failed to parse Gemini response", e);
            return "Error parsing response";
        }
    }
    
    /**
     * Extract text from streaming chunk.
     */
    private String extractTextFromStreamChunk(String chunk) {
        // Gemini SSE format: data: {json}
        if (chunk.startsWith("data: ")) {
            // Parse JSON and extract text
            // Simplified implementation - in production, use proper JSON parsing
            return chunk.substring(6);
        }
        return null;
    }
    
    /**
     * Parse analysis result from AI response.
     */
    private AnalysisResult parseAnalysisResult(String response) {
        // Simplified parsing - in production, use structured output or better parsing
        return new AnalysisResult(
            "Analysis completed",
            response,
            List.of(),
            List.of()
        );
    }
    
    /**
     * Parse suggestions from AI response.
     */
    private List<Suggestion> parseSuggestions(String response) {
        // Simplified parsing
        return List.of(
            new Suggestion("improvement", "AI Suggestion", response, null)
        );
    }
    
    /**
     * Parse validation issues from AI response.
     */
    private List<ValidationIssue> parseValidationIssues(String response) {
        // Simplified parsing
        if (response.toLowerCase().contains("error")) {
            return List.of(ValidationIssue.error("Validation failed", response));
        }
        return List.of();
    }
}
