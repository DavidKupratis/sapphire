/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.commands;

import java.util.List;

import org.eclipse.draw2d.Connection;
import org.eclipse.gef.commands.Command;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramConnectionTemplate;
import org.eclipse.sapphire.ui.diagram.editor.DiagramEmbeddedConnectionTemplate;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class CreateConnectionCommand extends Command {
    
	private SapphireDiagramEditorPagePart diagramPart;
    private IDiagramConnectionDef connDef;

    /** The connection instance. */
	//private DiagramConnectionPart connection;

	/** Start endpoint for the connection. */
	private final DiagramNodeModel source;
	/** Target endpoint for the connection. */
	private DiagramNodeModel target;

	/**
	 * Instantiate a command that can create a connection between two shapes.
	 * 
	 * @param source
	 *            the source endpoint (a non-null Shape instance)
	 * @param lineStyle
	 *            the desired line style. See Connection#setLineStyle(int) for
	 *            details
	 * @throws IllegalArgumentException
	 *             if source is null
	 * @see Connection#setLineStyle(int)
	 */
	public CreateConnectionCommand(DiagramNodeModel source, IDiagramConnectionDef connDef) {
		if (source == null) {
			throw new IllegalArgumentException();
		}
		setLabel("connection creation");
		this.source = source;
		this.connDef = connDef;
		this.diagramPart = source.getModelPart().nearest(SapphireDiagramEditorPagePart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	public boolean canExecute() {
		// disallow source -> source connections
		if (target == null /*|| source.equals(target)*/) {
			return false;
		}
		DiagramConnectionTemplate connectionTemplate = getConnectionTemplate(this.source);
		if (connectionTemplate != null && connectionTemplate.canCreateNewConnection(this.source.getModelPart(), this.target.getModelPart()))
			return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		DiagramConnectionTemplate connectionTemplate = getConnectionTemplate(this.source);
		DiagramConnectionPart connection = connectionTemplate.createNewDiagramConnection(this.source.getModelPart(), this.target.getModelPart());
		
		// activate direct editing after object creation
		if (connection.canEditLabel()) {
			diagramPart.selectAndDirectEdit(connection);
		}
	}

	/**
	 * Set the target endpoint for the connection.
	 * 
	 * @param target
	 *            that target endpoint (a non-null DiagramNodeModel instance)
	 * @throws IllegalArgumentException
	 *             if target is null
	 */
	public void setTarget(DiagramNodeModel target) {
		if (target == null) {
			throw new IllegalArgumentException();
		}
		this.target = target;
	}

    private DiagramConnectionTemplate getConnectionTemplate(DiagramNodeModel srcNode)
    {
        DiagramEmbeddedConnectionTemplate embeddedConn = srcNode.getModelPart().getDiagramNodeTemplate().getEmbeddedConnectionTemplate();
        if (embeddedConn != null && 
                embeddedConn.getConnectionId().equalsIgnoreCase(this.connDef.getId().content()))
        {
            return embeddedConn;
        }
        
        // check top level connections
        List<DiagramConnectionTemplate> connTemplates = this.diagramPart.getConnectionTemplates();
        for (DiagramConnectionTemplate connTemplate : connTemplates)
        {
            if (connTemplate.getConnectionId().equalsIgnoreCase(this.connDef.getId().content()))
            {
                return connTemplate;
            }
        }
        return null;
    }
}
