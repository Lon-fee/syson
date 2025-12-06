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
package org.eclipse.syson.ai.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration properties for AI services.
 * Reads configuration from application.properties with prefix "syson.ai".
 *
 * @author AI Services Team
 */
@Configuration
@ConfigurationProperties(prefix = "syson.ai")
@Data
public class AIServiceConfiguration {
    
    /**
     * Whether AI services are enabled.
     */
    private boolean enabled = true;
    
    /**
     * AI provider (gemini, openai, azure, etc.).
     */
    private String provider = "gemini";
    
    /**
     * API timeout in milliseconds.
     */
    private long timeout = 60000;
    
    /**
     * Google Gemini-specific configuration.
     */
    private GeminiConfig gemini = new GeminiConfig();
    
    /**
     * OpenAI-specific configuration.
     */
    private OpenAIConfig openai = new OpenAIConfig();
    
    @Data
    public static class GeminiConfig {
        /**
         * Google AI API key.
         */
        private String apiKey;
        
        /**
         * Google AI API endpoint.
         */
        private String endpoint = "https://generativelanguage.googleapis.com/v1beta";
        
        /**
         * Model to use (gemini-2.0-flash-exp, gemini-1.5-pro, etc.).
         */
        private String model = "gemini-2.0-flash-exp";
        
        /**
         * Temperature for generation (0.0 - 2.0).
         */
        private double temperature = 0.7;
        
        /**
         * Maximum output tokens.
         */
        private int maxOutputTokens = 8192;
        
        /**
         * Top-p sampling parameter.
         */
        private double topP = 0.95;
        
        /**
         * Top-k sampling parameter.
         */
        private int topK = 40;
    }
    
    @Data
    public static class OpenAIConfig {
        /**
         * OpenAI API key.
         */
        private String apiKey;
        
        /**
         * OpenAI API endpoint.
         */
        private String endpoint = "https://api.openai.com/v1";
        
        /**
         * Model to use (gpt-4, gpt-3.5-turbo, etc.).
         */
        private String model = "gpt-4";
        
        /**
         * Temperature for generation (0.0 - 2.0).
         */
        private double temperature = 0.7;
        
        /**
         * Maximum tokens in response.
         */
        private int maxTokens = 4096;
    }
}
