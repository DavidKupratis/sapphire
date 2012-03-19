/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - Moved it out of SapphireDiagramEditorPageEditPart class;
 *                   Support DND from project explorer.
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.policies;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeTemplate;
import org.eclipse.sapphire.ui.swt.gef.commands.CreateNodeCommand;
import org.eclipse.sapphire.ui.swt.gef.commands.DndObjectCommand;
import org.eclipse.sapphire.ui.swt.gef.commands.MoveConnectionLabelCommand;
import org.eclipse.sapphire.ui.swt.gef.commands.MoveNodeCommand;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramConnectionLabelEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramNodeEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.IConfigurationManagerHolder;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramXYLayoutEditPolicy extends XYLayoutEditPolicy 
{
	private DiagramModel model;
	
	public DiagramXYLayoutEditPolicy(DiagramModel model)
	{
		this.model = model;
	}

	@Override
	protected Rectangle getCurrentConstraintFor(GraphicalEditPart child) {
		if (child instanceof DiagramNodeEditPart) {
			return super.getCurrentConstraintFor(child);
		}
		return null;
	}

	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		if (child instanceof DiagramNodeEditPart)
			return new DiagramNodeSelectionEditPolicy();
		return new NonResizableEditPolicy();
	}

	@Override
	protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint) {
		if (child instanceof DiagramNodeEditPart && constraint instanceof Rectangle) {
			DiagramNodeModel node = ((DiagramNodeEditPart)child).getCastedModel();
			return new MoveNodeCommand(node, (Rectangle)constraint);
		}
		return super.createChangeConstraintCommand(request, child, constraint);
	}

	@Override
	protected Command createChangeConstraintCommand(EditPart part, Object constraint) {
		if (part instanceof DiagramConnectionLabelEditPart && constraint instanceof Rectangle) {
			return new MoveConnectionLabelCommand((DiagramConnectionLabelEditPart)part, (Rectangle)constraint);
		}
		return null;
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		Command cmd = UnexecutableCommand.INSTANCE;
		// determine constraint
		Rectangle rectangle = null;
		Point pt = new Point(-1, -1);
		if (request.getLocation() != null) {
			rectangle = (Rectangle) getConstraintFor(request);
			pt = new Point(rectangle.x, rectangle.y);
		}
		IConfigurationManagerHolder host = (IConfigurationManagerHolder)getHost();
		if (request.getNewObjectType() == DiagramNodeTemplate.class) {
			DiagramNodeTemplate nodeTemplate = (DiagramNodeTemplate)request.getNewObject();				
			cmd = new CreateNodeCommand(this.model, host, nodeTemplate, pt);
		}
		else if (request.getNewObjectType() == ISelection.class) {
			// DND from project explorer
			ISelection selection = (ISelection)request.getNewObject();
			DiagramModel diagramModel = (DiagramModel)getHost().getModel();
			cmd = new DndObjectCommand(diagramModel, host, selection, pt);
		}
		return cmd;
	}

}
