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
package org.eclipse.syson.diagram.common.view.tools;

import java.util.Objects;

import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.builder.providers.INodeToolProvider;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.model.services.ModelMutationElementService;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Node Tool calling by "View as > XXXX View" and allowing to set the type of the View.
 *
 * @author arichard
 */
public class SetAsViewToolProvider implements INodeToolProvider {

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();

    private final ViewBuilders viewBuilderHelper = new ViewBuilders();

    private final String viewDefinition;

    private final String label;

    public SetAsViewToolProvider(String viewDefinition, String label) {
        this.viewDefinition = Objects.requireNonNull(viewDefinition);
        this.label = Objects.requireNonNull(label);
    }

    @Override
    public NodeTool create(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newNodeTool()
                .name(this.label)
                .iconURLsExpression("/icons/full/obj16/ViewUsage.svg")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(ModelMutationElementService::setAsView).aqlSelf(this.viewDefinition))
                        .build())
                .build();
    }
}
