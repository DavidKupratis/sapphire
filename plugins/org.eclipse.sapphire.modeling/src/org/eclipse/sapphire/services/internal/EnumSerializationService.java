/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.services.internal;

import org.eclipse.sapphire.modeling.EnumValueType;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.EnumSerialization;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.services.ValueSerializationService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class EnumSerializationService extends ValueSerializationService
{
    private EnumValueType enumValueType;
    
    @Override
    protected void init()
    {
        super.init();
        
        this.enumValueType = new EnumValueType( context( ValueProperty.class ).getTypeClass() );        
    }

    @Override
    protected Enum<?> decodeFromString( final String value )
    {
        Enum<?> result = null;
        
        for( Enum<?> enumItem : this.enumValueType.getItems() )
        {
            final EnumSerialization enumSerializationAnnotation = this.enumValueType.getAnnotation( enumItem, EnumSerialization.class );
            
            if( enumSerializationAnnotation == null )
            {
                if( enumItem.name().equalsIgnoreCase( value ) )
                {
                    result = enumItem;
                }
            }
            else
            {
                if( enumSerializationAnnotation.caseSensitive() )
                {
                    if( enumSerializationAnnotation.primary().equals( value ) )
                    {
                        result = enumItem;
                    }
                    else
                    {
                        for( String x : enumSerializationAnnotation.alternative() )
                        {
                            if( x.equals( value ) )
                            {
                                result = enumItem;
                                break;
                            }
                        }
                    }
                }
                else
                {
                    if( enumSerializationAnnotation.primary().equalsIgnoreCase( value ) )
                    {
                        result = enumItem;
                    }
                    else
                    {
                        for( String x : enumSerializationAnnotation.alternative() )
                        {
                            if( x.equalsIgnoreCase( value ) )
                            {
                                result = enumItem;
                                break;
                            }
                        }
                    }
                }
            }

            if( result != null )
            {
                break;
            }
        }

        return result;
    }

    @Override
    public String encode( final Object value )
    {
        String result = null;
        
        if( value != null )
        {
            final Enum<?> val = (Enum<?>) value;
            final EnumSerialization enumStringBindingAnnotation = this.enumValueType.getAnnotation( val, EnumSerialization.class );
            
            if( enumStringBindingAnnotation == null )
            {
                result = val.name();
            }
            else
            {
                result = enumStringBindingAnnotation.primary();
            }
        }
        
        return result;
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class ); 
            return ( property != null && property.isOfType( Enum.class ) );
        }
    
        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new EnumSerializationService();
        }
    }
    
}