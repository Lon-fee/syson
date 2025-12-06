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
 * Result of model analysis operation.
 *
 * @param summary Brief summary of the analysis
 * @param details Detailed analysis text
 * @param suggestions List of improvement suggestions
 * @param issues List of identified issues
 *
 * @author AI Services Team
 */
public record AnalysisResult(
    String summary,
    String details,
    java.util.List<String> suggestions,
    java.util.List<ValidationIssue> issues
) {}
