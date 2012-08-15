/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.ui.diagram.shape.def.ValidationMarkerDef;
import org.eclipse.sapphire.ui.diagram.shape.def.ValidationMarkerSize;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class ValidationMarkerPart extends ShapePart 
{
	private ValidationMarkerDef markerDef;
	private IModelElement modelElement;
	
	@Override
    protected void init()
    {
        super.init();
        this.markerDef = (ValidationMarkerDef)super.definition;
        this.modelElement = getModelElement();
                
    }
	
	
    public ValidationMarkerSize getSize()
    {
    	return this.markerDef.getSize().getContent();
    }
        

}
