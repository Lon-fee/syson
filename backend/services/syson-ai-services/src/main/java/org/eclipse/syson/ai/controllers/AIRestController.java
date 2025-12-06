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
package org.eclipse.syson.ai.controllers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.syson.ai.importer.ImportResult;
import org.eclipse.syson.ai.importer.SysMLImportService;
import org.eclipse.syson.ai.importer.SysMLImportService.UpdateStrategy;
import org.eclipse.syson.ai.services.IAIService;
import org.eclipse.syson.ai.services.ModelContext;
import org.eclipse.syson.ai.validation.SysMLCodeValidator;
import org.eclipse.syson.ai.validation.SysMLCodeValidator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

/**
 * REST API controller for AI services.
 * Provides endpoints for chat, code generation, analysis, and validation.
 *
 * @author AI Services Team
 */
@RestController
@RequestMapping("/api/ai")
public class AIRestController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AIRestController.class);
    
    private final IAIService aiService;
    private final SysMLCodeValidator codeValidator;
    private final SysMLImportService importService;
    
    public AIRestController(
        IAIService aiService,
        SysMLCodeValidator codeValidator,
        SysMLImportService importService
    ) {
        this.aiService = aiService;
        this.codeValidator = codeValidator;
        this.importService = importService;
    }
    
    /**
     * Chat with AI assistant.
     *
     * POST /api/ai/chat
     * Body: {
     *   "message": "user question",
     *   "context": { modelId, elementType, ... }
     * }
     */
    @PostMapping("/chat")
    public CompletableFuture<ResponseEntity<String>> chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        ModelContext context = extractContext(request);
        
        LOGGER.info("Chat request: {}", message);
        
        return aiService.chat(message, context)
            .thenApply(ResponseEntity::ok)
            .exceptionally(error -> {
                LOGGER.error("Chat failed", error);
                return ResponseEntity.internalServerError().body("Error: " + error.getMessage());
            });
    }
    
    /**
     * Streaming chat with AI assistant.
     *
     * POST /api/ai/chat/stream
     * Returns: Server-Sent Events stream
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        ModelContext context = extractContext(request);
        
        LOGGER.info("Streaming chat request: {}", message);
        
        return aiService.chatStream(message, context)
            .map(chunk -> ServerSentEvent.<String>builder()
                .event("message")
                .data(chunk)
                .build())
            .doOnError(error -> LOGGER.error("Streaming chat failed", error));
    }
    
    /**
     * Generate SysML V2 code from description.
     *
     * POST /api/ai/code/generate
     * Body: {
     *   "description": "create a vehicle with engine",
     *   "context": { currentCode, ... }
     * }
     */
    @PostMapping("/code/generate")
    public CompletableFuture<ResponseEntity<Map<String, String>>> generateCode(
        @RequestBody Map<String, Object> request
    ) {
        String description = (String) request.get("description");
        ModelContext context = extractContext(request);
        
        LOGGER.info("Code generation request: {}", description);
        
        return aiService.generateCode(description, context)
            .thenApply(code -> ResponseEntity.ok(Map.of("code", code)))
            .exceptionally(error -> {
                LOGGER.error("Code generation failed", error);
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", error.getMessage()));
            });
    }
    
    /**
     * Stream generated SysML V2 code.
     *
     * POST /api/ai/code/generate/stream
     * Returns: Server-Sent Events stream
     */
    @PostMapping(value = "/code/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateCodeStream(@RequestBody Map<String, Object> request) {
        String description = (String) request.get("description");
        ModelContext context = extractContext(request);
        
        LOGGER.info("Streaming code generation request: {}", description);
        
        return aiService.generateCodeStream(description, context)
            .map(chunk -> ServerSentEvent.<String>builder()
                .event("code-chunk")
                .data(chunk)
                .build())
            .doOnError(error -> LOGGER.error("Streaming code generation failed", error));
    }
    
    /**
     * Validate SysML V2 code.
     *
     * POST /api/ai/validate
     * Body: {
     *   "code": "part def Vehicle { ... }"
     * }
     */
    @PostMapping("/validate")
    public CompletableFuture<ResponseEntity<?>> validate(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        
        LOGGER.info("Validation request for code length: {}", code != null ? code.length() : 0);
        
        return aiService.validateElement(code)
            .thenApply(issues -> ResponseEntity.ok(Map.of(
                "valid", issues.isEmpty(),
                "issues", issues
            )))
            .exceptionally(error -> {
                LOGGER.error("Validation failed", error);
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", error.getMessage()));
            });
    }
    
    /**
     * Analyze model element.
     *
     * POST /api/ai/analyze
     * Body: {
     *   "content": "model content",
     *   "elementType": "PartDefinition"
     * }
     */
    @PostMapping("/analyze")
    public CompletableFuture<ResponseEntity<?>> analyze(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String elementType = request.get("elementType");
        
        LOGGER.info("Analysis request for element type: {}", elementType);
        
        return aiService.analyzeModel(content, elementType)
            .thenApply(ResponseEntity::ok)
            .exceptionally(error -> {
                LOGGER.error("Analysis failed", error);
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", error.getMessage()));
            });
    }
    
    /**
     * Get suggestions based on context.
     *
     * POST /api/ai/suggestions
     * Body: {
     *   "context": { ... }
     * }
     */
    @PostMapping("/suggestions")
    public CompletableFuture<ResponseEntity<?>> getSuggestions(@RequestBody Map<String, Object> request) {
        ModelContext context = extractContext(request);
        
        LOGGER.info("Suggestions request for context: {}", context);
        
        return aiService.provideSuggestions(context)
            .thenApply(suggestions -> ResponseEntity.ok(Map.of("suggestions", suggestions)))
            .exceptionally(error -> {
                LOGGER.error("Suggestions failed", error);
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", error.getMessage()));
            });
    }
    
    /**
     * Directly validate SysML V2 code (using validator, not AI).
     *
     * POST /api/ai/code/validate
     * Body: {
     *   "code": "part def Vehicle { ... }"
     * }
     */
    @PostMapping("/code/validate")
    public ResponseEntity<?> validateCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        
        LOGGER.info("Direct code validation request for {} characters", 
            code != null ? code.length() : 0);
        
        ValidationResult result = codeValidator.validate(code);
        
        return ResponseEntity.ok(Map.of(
            "valid", result.isValid(),
            "hasErrors", result.hasErrors(),
            "hasWarnings", result.hasWarnings(),
            "errors", result.getErrors(),
            "warnings", result.getWarnings(),
            "allIssues", result.issues()
        ));
    }
    
    /**
     * Import SysML V2 code into SysON project.
     *
     * POST /api/ai/code/import
     * Body: {
     *   "code": "part def Vehicle { ... }",
     *   "projectId": "project-123"
     * }
     */
    @PostMapping("/code/import")
    public ResponseEntity<?> importCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String projectId = request.get("projectId");
        
        LOGGER.info("Code import request for project: {}", projectId);
        
        ImportResult result = importService.importCode(code, projectId);
        
        if (result.success()) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "modelId", result.modelId(),
                "message", result.message(),
                "validationIssues", result.validationIssues()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", result.message(),
                "validationIssues", result.validationIssues()
            ));
        }
    }
    
    /**
     * Update existing model with new code.
     *
     * POST /api/ai/code/update
     * Body: {
     *   "modelId": "model-123",
     *   "code": "part def Vehicle { ... }",
     *   "strategy": "MERGE" | "REPLACE" | "APPEND"
     * }
     */
    @PostMapping("/code/update")
    public ResponseEntity<?> updateModel(@RequestBody Map<String, String> request) {
        String modelId = request.get("modelId");
        String code = request.get("code");
        String strategyStr = request.get("strategy");
        
        UpdateStrategy strategy;
        try {
            strategy = UpdateStrategy.valueOf(strategyStr.toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid strategy. Use: MERGE, REPLACE, or APPEND"
            ));
        }
        
        LOGGER.info("Model update request: {} with strategy: {}", modelId, strategy);
        
        ImportResult result = importService.updateModel(modelId, code, strategy);
        
        if (result.success()) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "modelId", result.modelId(),
                "message", result.message()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", result.message()
            ));
        }
    }
    
    /**
     * Extract ModelContext from request body.
     */
    @SuppressWarnings("unchecked")
    private ModelContext extractContext(Map<String, Object> request) {
        Map<String, Object> contextMap = (Map<String, Object>) request.get("context");
        if (contextMap == null) {
            return ModelContext.basic(null);
        }
        
        return new ModelContext(
            (String) contextMap.get("modelId"),
            (String) contextMap.get("selectedElementId"),
            (String) contextMap.get("elementType"),
            (String) contextMap.get("elementName"),
            (String) contextMap.get("currentCode"),
            Map.of()
        );
    }
}
