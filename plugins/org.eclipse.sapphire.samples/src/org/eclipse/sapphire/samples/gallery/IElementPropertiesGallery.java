/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.gallery;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface IElementPropertiesGallery

    extends IModelElement

{
    ModelElementType TYPE = new ModelElementType( IElementPropertiesGallery.class );
    
    // *** Homogeneous ***
    
    @Type( base = IChildElement.class )
    @XmlElementBinding( path = "homogeneous", mappings = @XmlElementBinding.Mapping( element = "child", type = IChildElement.class ) )
    
    ElementProperty PROP_HOMOGENEOUS = new ElementProperty( TYPE, "Homogeneous" );
    
    ModelElementHandle<IChildElement> getHomogeneous();
    
    // *** Heterogeneous ***
    
    @Type( base = IChildElement.class, possible = { IChildElement.class, IChildElementWithInteger.class, IChildElementWithEnum.class } )
    
    @XmlElementBinding
    (
        path = "heterogeneous", 
        mappings = 
        {
            @XmlElementBinding.Mapping( element = "child", type = IChildElement.class ),
            @XmlElementBinding.Mapping( element = "child-with-integer", type = IChildElementWithInteger.class ),
            @XmlElementBinding.Mapping( element = "child-with-enum", type = IChildElementWithEnum.class )
        }
    )
    
    ElementProperty PROP_HETEROGENEOUS = new ElementProperty( TYPE, "Heterogeneous" );
    
    ModelElementHandle<IChildElement> getHeterogeneous();
    
    // *** CustomPossibleTypes ***
    
    @Type( base = ElementPropertyCustomGallery.class )
    @XmlBinding( path = "custom-possible-types" )
    
    ImpliedElementProperty PROP_CUSTOM_POSSIBLE_TYPES = new ImpliedElementProperty( TYPE, "CustomPossibleTypes" );
    
    ElementPropertyCustomGallery getCustomPossibleTypes();
    
    // *** Implied ***
    
    @Type( base = IChildElementWithInteger.class )
    @XmlBinding( path = "implied" )
    
    ImpliedElementProperty PROP_IMPLIED = new ImpliedElementProperty( TYPE, "Implied" );
    
    IChildElementWithInteger getImplied();
    
    // *** StringValue ***
    
    @XmlBinding( path = "string" )
    @Label( standard = "root string value" )
    
    ValueProperty PROP_STRING_VALUE = new ValueProperty( TYPE, "StringValue" );
    
    Value<String> getStringValue();
    void setStringValue( String value );
    
    // *** IntegerValue ***
    
    @Type( base = Integer.class )
    @Label( standard = "root integer value" )
    @XmlBinding( path = "integer" )

    ValueProperty PROP_INTEGER_VALUE = new ValueProperty( TYPE, "IntegerValue" );
    
    Value<Integer> getIntegerValue();
    void setIntegerValue( String value );
    void setIntegerValue( Integer value );
    
}
