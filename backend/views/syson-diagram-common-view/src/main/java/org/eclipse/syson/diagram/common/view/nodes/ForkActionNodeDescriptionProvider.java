/*******************************************************************************
 * Copyright (c) 2024 Obeo.
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
package org.eclipse.syson.diagram.common.view.nodes;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.IDescriptionNameGenerator;

/**
 * Used to create the Fork node description of an Action.
 *
 * @author Jerome Gout
 */
public class ForkActionNodeDescriptionProvider extends AbstractControlNodeActionNodeDescriptionProvider {

    public static final String FORK_ACTION_NAME = "ForkAction";

    public ForkActionNodeDescriptionProvider(IColorProvider colorProvider, IDescriptionNameGenerator descriptionNameGenerator) {
        super(colorProvider, descriptionNameGenerator);
    }

    @Override
    protected String getNodeDescriptionName() {
        return FORK_ACTION_NAME;
    }

    @Override
    protected EClass getDomainType() {
        return SysmlPackage.eINSTANCE.getForkNode();
    }

    @Override
    protected String getImagePath() {
        return "images/fork_action.svg";
    }

    @Override
    protected String getRemoveToolLabel() {
        return "Remove Fork";
    }
}
