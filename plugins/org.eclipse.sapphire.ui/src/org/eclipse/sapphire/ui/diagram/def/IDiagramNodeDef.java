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

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.java.JavaTypeConstraints;
import org.eclipse.sapphire.modeling.java.JavaTypeKind;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@GenerateImpl

public interface IDiagramNodeDef 

	extends ISapphirePartDef, IDiagramDimension
	
{
	ModelElementType TYPE = new ModelElementType( IDiagramNodeDef.class );
	
    // *** Id ***
    
    @Label( standard = "ID" )
    @XmlBinding( path = "id" )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
    
    Value<String> getId();
    void setId( String id );
    
    // *** ToolPaletteLabel ***
    
    @Label( standard = "tool palette label" )
    @XmlBinding( path = "tool-palette-label" )
    
    ValueProperty PROP_TOOL_PALETTE_LABEL = new ValueProperty( TYPE, "ToolPaletteLabel" );
    
    Value<String> getToolPaletteLabel();
    void setToolPaletteLabel( String paletteLabel );
    
    // *** ToolPaletteDesc ***
    
    @Label( standard = "tool palette description" )
    @XmlBinding( path = "tool-palette-desc" )
    
    ValueProperty PROP_TOOL_PALETTE_DESC = new ValueProperty( TYPE, "ToolPaletteDesc" );
    
    Value<String> getToolPaletteDesc();
    void setToolPaletteDesc( String paletteDesc );
    
    // *** resizable ***
    
    @Type( base = Boolean.class )
    @Label( standard = "resizable" )
    @XmlBinding( path = "resizable" )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_RESIZABLE = new ValueProperty( TYPE, "Resizable" );
    
    Value<Boolean> isResizable();
    void setResizable( String value );
    void setResizable( Boolean value );
    
    // *** HorizontalSpacing ***
	
    @Type( base = Integer.class )
    @Label( standard = "horizontal spacing" )
    @XmlBinding( path = "horizontal-spacing" )
    @DefaultValue( text = "0" )
    
    ValueProperty PROP_HORIZONTAL_SPACING = new ValueProperty( TYPE, "HorizontalSpacing" );
    
    Value<Integer> getHorizontalSpacing();
    void setHorizontalSpacing( String value );
    void setHorizontalSpacing( Integer value );
    
    // *** VerticalSpacing ***
	
    @Type( base = Integer.class )
    @Label( standard = "vertical spacing" )
    @XmlBinding( path = "vertical-spacing" )
    @DefaultValue( text = "0" )
    
    ValueProperty PROP_VERTICAL_SPACING = new ValueProperty( TYPE, "VerticalSpacing" );
    
    Value<Integer> getVerticalSpacing();
    void setVerticalSpacing( String value );
    void setVerticalSpacing( Integer value );

    // *** Property ***
    
    @Label( standard = "property" )
    @XmlBinding( path = "property" )
    
    ValueProperty PROP_PROPERTY = new ValueProperty( TYPE, "Property" );
    
    Value<String> getProperty();
    void setProperty( String property );
    
	// *** PossibleImages ***
    
    @Type( base = IDiagramImageChoice.class )
    @XmlListBinding( path = "possibleImages",
    				mappings = @XmlListBinding.Mapping( element = "imageChoice", type = IDiagramImageChoice.class ) )
                             
    ListProperty PROP_POSSIBLE_IMAGES = new ListProperty( TYPE, "PossibleImages" );
    
    ModelElementList<IDiagramImageChoice> getPossibleImages();
    
    // *** Image ***
    
    @Type( base = IDiagramNodeImageDef.class )
    @XmlBinding( path = "image" )

    ElementProperty PROP_IMAGE = new ElementProperty( TYPE, "Image" );
    
    ModelElementHandle<IDiagramNodeImageDef> getImage();
    
    // *** Label ***
    
    @Type( base = IDiagramLabelDef.class )
    @XmlBinding( path = "label" )
    
    ElementProperty PROP_LABEL = new ElementProperty( TYPE, "Label" );
    
    ModelElementHandle<IDiagramLabelDef> getLabel();
    	    
    // *** ValidationDecorator ***
    
    @Type( base = IDiagramDecoratorDef.class )
    @XmlBinding( path = "problem-indicator" )

    ImpliedElementProperty PROP_VALIDATION_DECORATOR = new ImpliedElementProperty( TYPE, "ValidationDecorator" );
    
    IDiagramDecoratorDef getValidationDecorator();

    // *** InstanceId ***
    
    @Type( base = Function.class )
    @Label( standard = "instance id" )
    @Localizable
    @XmlBinding( path = "instance-id" )
    
    ValueProperty PROP_INSTANCE_ID = new ValueProperty( TYPE, "InstanceId" );
    
    Value<Function> getInstanceId();
    void setInstanceId( String value );
    void setInstanceId( Function value );
        
    // *** EmbeddedConnections ***
    
    @Type( base = IDiagramConnectionBindingDef.class )
    @XmlBinding( path = "connection-binding" )

    ElementProperty PROP_EMBEDDED_CONNECTIONS = new ElementProperty( TYPE, "EmbeddedConnections" );
    
    ModelElementHandle<IDiagramConnectionBindingDef> getEmbeddedConnections();
    
    // *** DefaultAction ***
    
    @Type( base = IDiagramNodeDefaultActionDef.class )
    @XmlBinding( path = "default-action" )
    
    ElementProperty PROP_DEFAULT_ACTION = new ElementProperty( TYPE, "DefaultAction" );
    
    ModelElementHandle<IDiagramNodeDefaultActionDef> getDefaultAction();
    
    // *** DropTargetService ***
    
    @Label( standard = "drop target service" )    
    @Reference( target = Class.class )
    @JavaTypeConstraints( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.ui.diagram.DiagramDropTargetService" )
    @MustExist
    @XmlBinding( path = "drop-target-service" )
    
    ValueProperty PROP_DROP_TARGET_SERVICE = new ValueProperty( TYPE, "DropTargetService" );
    
    ReferenceValue<Class<?>> getDropTargetService();
    void setDropTargetService( String value );
    
}
