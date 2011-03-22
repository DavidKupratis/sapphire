/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.def;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@GenerateImpl

public interface IDiagramDimension

	extends IModelElement
	
{
	ModelElementType TYPE = new ModelElementType( IDiagramDimension.class );
	
    // *** Width ***
	
    @Type( base = Integer.class )
    @Label( standard = "width" )
    @NumericRange( min = "1" )
    @XmlBinding( path = "width" )
    
    ValueProperty PROP_WIDTH = new ValueProperty( TYPE, "Width" );
    
    Value<Integer> getWidth();
    void setWidth( String width );
    void setWidth( Integer width );

    // *** Height ***
	
    @Type( base = Integer.class )
    @Label( standard = "height" )
    @NumericRange( min = "1" )
    @XmlBinding( path = "height" )
    
    ValueProperty PROP_HEIGHT = new ValueProperty( TYPE, "Height" );
    
    Value<Integer> getHeight();
    void setHeight( String width );
    void setHeight( Integer width );
	
}
