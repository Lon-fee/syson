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

import java.util.Map;

/**
 * Model context containing information about the current modeling state.
 * This context is passed to AI services to provide context-aware responses.
 *
 * @param modelId Current model identifier
 * @param selectedElementId ID of the selected element
 * @param elementType Type of the selected element (e.g., "PartDefinition", "RequirementDefinition")
 * @param elementName Name of the selected element
 * @param currentCode Current SysML V2 code (for code generation mode)
 * @param additionalContext Additional context information
 *
 * @author AI Services Team
 */
public record ModelContext(
    String modelId,
    String selectedElementId,
    String elementType,
    String elementName,
    String currentCode,
    Map<String, Object> additionalContext
) {
    
    /**
     * Create a basic model context with only model ID.
     */
    public static ModelContext basic(String modelId) {
        return new ModelContext(modelId, null, null, null, null, Map.of());
    }
    
    /**
     * Create a context for code generation mode.
     */
    public static ModelContext forCodeGeneration(String modelId, String currentCode) {
        return new ModelContext(modelId, null, null, null, currentCode, Map.of());
    }
}
