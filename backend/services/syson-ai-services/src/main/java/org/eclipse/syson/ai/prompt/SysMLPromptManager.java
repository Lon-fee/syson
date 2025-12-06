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
package org.eclipse.syson.ai.prompt;

import org.eclipse.syson.ai.services.ModelContext;
import org.springframework.stereotype.Component;

/**
 * Manages SysML V2 expert prompts for AI interactions.
 * Builds dynamic prompts based on context and injects relevant knowledge.
 *
 * @author AI Services Team
 */
@Component
public class SysMLPromptManager {
    
    private static final String SYSTEM_IDENTITY = """
        # Role
        You are SysON AI Assistant, a professional SysML V2 modeling expert.
        
        # Core Capabilities
        - **SysML V2 Specification Expert**: Deep knowledge of OMG SysML V2 standard and KerML
        - **MBSE Practitioner**: Rich experience in Model-Based Systems Engineering
        - **Modeling Consultant**: Provide methodology, architecture design, and best practices
        - **Tool Expert**: Familiar with Eclipse SysON and Sirius Web platform
        
        # Communication Style
        - Professional but friendly, use clear and understandable language
        - Provide specific actionable advice, not abstract concepts
        - Use examples and diagrams to assist explanation
        - Cite relevant specification sections to enhance credibility
        - Support bilingual communication in Chinese and English
        
        # Behavior Principles
        1. **Accuracy First**: Only provide accurate information based on SysML V2 specification
        2. **Context Aware**: Always consider user's current modeling context
        3. **Educational**: Not only answer "how", but also explain "why"
        4. **Practical**: Focus on actual modeling scenarios, avoid being too theoretical
        5. **Cautious Advice**: For complex architectural decisions, provide multiple options with trade-offs
        """;
    
    /**
     * Build complete prompt for AI interaction.
     *
     * @param userQuestion User's question or request
     * @param context Current modeling context
     * @return Complete prompt including system instructions, context, and user question
     */
    public String buildPrompt(String userQuestion, ModelContext context) {
        StringBuilder prompt = new StringBuilder();
        
        // 1. System identity and capabilities
        prompt.append(SYSTEM_IDENTITY).append("\n\n");
        
        // 2. Current context
        if (context != null) {
            prompt.append(buildContextSection(context)).append("\n\n");
        }
        
        // 3. User question
        prompt.append("## User Question\n");
        prompt.append(userQuestion).append("\n\n");
        
        // 4. Response guidelines
        prompt.append(getResponseGuidelines()).append("\n");
        
        return prompt.toString();
    }
    
    /**
     * Build context section based on current modeling state.
     */
    private String buildContextSection(ModelContext context) {
        StringBuilder section = new StringBuilder("## Current Modeling Context\n");
        
        if (context.elementType() != null && context.elementName() != null) {
            section.append("- Current element: **")
                   .append(context.elementType())
                   .append("** \"")
                   .append(context.elementName())
                   .append("\"\n");
        }
        
        if (context.currentCode() != null) {
            section.append("- Current SysML V2 code:\n```sysml\n")
                   .append(context.currentCode())
                   .append("\n```\n");
            section.append("When using edit operations, COPY text exactly from above!\n");
        }
        
        if (context.modelId() != null) {
            section.append("- Model ID: ").append(context.modelId()).append("\n");
        }
        
        return section.toString();
    }
    
    /**
     * Get response formatting guidelines.
     */
    private String getResponseGuidelines() {
        return """
            ## Response Format Requirements
            Please organize your answer in the following format:
            
            1. **Brief Answer**: Directly answer the user's question (2-3 sentences)
            2. **Detailed Explanation**: Provide necessary background and explanation
            3. **Code Example**: If applicable, provide SysML V2 code examples
            4. **Best Practices**: Relevant suggestions and considerations
            5. **References**: Cite specification sections or documentation links
            
            Answer in Chinese, with code parts in English. Maintain a professional but understandable tone.
            """;
    }
    
    /**
     * Build prompt specifically for code generation.
     */
    public String buildCodeGenerationPrompt(String description, ModelContext context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("""
            # Role
            You are a SysML V2 Code Generation Expert. Generate syntactically correct and semantically meaningful SysML V2 code.
            
            # Code Generation Rules
            1. **Definitions** use `def` keyword: `part def`, `requirement def`, etc.
            2. **Usages** omit `def`: `part myEngine : Engine;`
            3. **Inheritance** uses `:>`: `part def ElectricEngine :> Engine {}`
            4. **Typing** uses `:`: `attribute speed : Real;`
            5. **Multiplicity** in brackets: `[1]`, `[0..1]`, `[*]`
            
            # Naming Conventions
            - Definitions: PascalCase (e.g., VehicleController)
            - Usages: camelCase (e.g., frontEngine)
            - Packages: PascalCase
            
            # Structure Pattern
            ```sysml
            package SystemName {
                import ScalarValues::*;
                
                part def ComponentType {
                    attribute name : Type;
                    port portName : InterfaceType;
                }
                
                part systemInstance : SystemType {
                    part component1 : Type1;
                }
            }
            ```
            
            """);
        
        if (context != null && context.currentCode() != null) {
            prompt.append("## Current Code\n```sysml\n")
                  .append(context.currentCode())
                  .append("\n```\n\n");
        }
        
        prompt.append("## User Request\n")
              .append(description)
              .append("\n\nGenerate valid SysML V2 code.");
        
        return prompt.toString();
    }
}
