/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.gef.diagram.editor.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class LabelNodeCommand extends Command {
	
	private DiagramNodePart part;
	private String labelText;

	public LabelNodeCommand(DiagramNodePart part, String labelText) {
		this.part = part;
		this.labelText = labelText;
	}

	@Override
	public void execute() {
		part.setLabel(labelText);
	}
	
}