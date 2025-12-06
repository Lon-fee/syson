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
package org.eclipse.syson.ai.importer;

import java.util.List;

import org.eclipse.syson.ai.services.ValidationIssue;

/**
 * Result of importing SysML V2 code into SysON.
 *
 * @param success Whether import was successful
 * @param modelId ID of created/updated model (null if failed)
 * @param message Result message
 * @param validationIssues Validation issues encountered
 *
 * @author AI Services Team
 */
public record ImportResult(
    boolean success,
    String modelId,
    String message,
    List<ValidationIssue> validationIssues
) {
    
    /**
     * Create a successful import result.
     */
    public static ImportResult success(String modelId, List<ValidationIssue> issues) {
        return new ImportResult(
            true,
            modelId,
            "Successfully imported code",
            issues
        );
    }
    
    /**
     * Create a validation failure result.
     */
    public static ImportResult validationFailed(List<ValidationIssue> issues) {
        return new ImportResult(
            false,
            null,
            "Validation failed",
            issues
        );
    }
    
    /**
     * Create an error result.
     */
    public static ImportResult error(String message) {
        return new ImportResult(
            false,
            null,
            message,
            List.of()
        );
    }
}
