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
 * AI-generated suggestion for modeling.
 *
 * @param type Type of suggestion (e.g., "improvement", "alternative", "warning")
 * @param title Brief title of the suggestion
 * @param description Detailed description
 * @param code Optional SysML V2 code example
 *
 * @author AI Services Team
 */
public record Suggestion(
    String type,
    String title,
    String description,
    String code
) {}
