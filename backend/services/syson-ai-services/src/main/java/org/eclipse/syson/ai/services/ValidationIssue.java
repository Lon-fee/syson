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

/**
 * Validation issue found in model element.
 *
 * @param severity Severity level ("ERROR", "WARNING", "INFO")
 * @param message Issue description
 * @param suggestion How to fix the issue
 * @param lineNumber Optional line number where issue occurs
 *
 * @author AI Services Team
 */
public record ValidationIssue(
    String severity,
    String message,
    String suggestion,
    Integer lineNumber
) {
    
    public static ValidationIssue error(String message, String suggestion) {
        return new ValidationIssue("ERROR", message, suggestion, null);
    }
    
    public static ValidationIssue warning(String message, String suggestion) {
        return new ValidationIssue("WARNING", message, suggestion, null);
    }
    
    public static ValidationIssue info(String message) {
        return new ValidationIssue("INFO", message, null, null);
    }
}
