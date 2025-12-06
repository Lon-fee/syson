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
package org.eclipse.syson.ai.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import reactor.core.publisher.Flux;

/**
 * AI Service interface for SysML V2 modeling assistance.
 * Provides chat, code generation, analysis, and validation capabilities.
 *
 * @author AI Services Team
 */
public interface IAIService {
    
    /**
     * Send a chat message to the AI with SysML V2 context.
     *
     * @param message User's message
     * @param context Current modeling context
     * @return AI response
     */
    CompletableFuture<String> chat(String message, ModelContext context);
    
    /**
     * Stream chat responses for real-time interaction.
     *
     * @param message User's message
     * @param context Current modeling context
     * @return Stream of response chunks
     */
    Flux<String> chatStream(String message, ModelContext context);
    
    /**
     * Analyze a SysML V2 model element.
     *
     * @param modelContent Model element content
     * @param elementType Type of the element
     * @return Analysis result
     */
    CompletableFuture<AnalysisResult> analyzeModel(String modelContent, String elementType);
    
    /**
     * Provide context-aware suggestions.
     *
     * @param context Current modeling context
     * @return List of suggestions
     */
    CompletableFuture<List<Suggestion>> provideSuggestions(ModelContext context);
    
    /**
     * Validate a model element against SysML V2 rules.
     *
     * @param elementContent Element content to validate
     * @return List of validation issues
     */
    CompletableFuture<List<ValidationIssue>> validateElement(String elementContent);
    
    /**
     * Generate SysML V2 code from natural language description.
     *
     * @param description Natural language description
     * @param context Current modeling context
     * @return Generated SysML V2 code
     */
    CompletableFuture<String> generateCode(String description, ModelContext context);
    
    /**
     * Stream generated SysML V2 code for real-time preview.
     *
     * @param description Natural language description
     * @param context Current modeling context
     * @return Stream of code chunks
     */
    Flux<String> generateCodeStream(String description, ModelContext context);
}
