/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [384683] PossibleValuesServiceFromModel does not load values initially
 ******************************************************************************/

package org.eclipse.sapphire.services.internal;

import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.PropertyVisitor;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ElementDisposeEvent;
import org.eclipse.sapphire.modeling.ModelPath;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.util.NLS;
import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.util.SetFactory;

/**
 * Implementation of PossibleValuesService based on @PossibleValues annotation's property attribute..
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class ModelBasedPossibleValuesService extends PossibleValuesService
{
    private ModelPath path;
    private String invalidValueMessageTemplate;
    private Status.Severity invalidValueSeverity;
    private boolean caseSensitive;
    private boolean ordered;
    private Set<String> values;
    private boolean initialized;
    private boolean readPriorToInit;
    
    @Override
    protected void init()
    {
        super.init();
        
        final Value<?> value = context( Value.class );
        final Element element = value.element();
        
        final PossibleValues a = value.definition().getAnnotation( PossibleValues.class );
        
        this.path = new ModelPath( a.property() );
        this.invalidValueMessageTemplate = a.invalidValueMessage();
        this.invalidValueSeverity = a.invalidValueSeverity();
        this.caseSensitive = a.caseSensitive();
        this.ordered = a.ordered();
        
        final Listener listener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                refresh();
            }
        };
        
        element.attach( listener, this.path );
        
        refresh();
        
        element.attach
        (
            new FilteredListener<ElementDisposeEvent>()
            {
                @Override
                protected void handleTypedEvent( final ElementDisposeEvent event )
                {
                    element.detach( listener, ModelBasedPossibleValuesService.this.path );
                }
            }
        );
        
        this.initialized = true;
    }

    @Override
    protected void fillPossibleValues( final Set<String> values )
    {
        if( ! this.initialized )
        {
            this.readPriorToInit = true;
        }
        
        values.addAll( this.values );
    }
    
    @Override
    public String getInvalidValueMessage( final String invalidValue )
    {
        return NLS.bind( this.invalidValueMessageTemplate, invalidValue, context( PropertyDef.class ).getLabel( true, CapitalizationType.NO_CAPS, false ) );
    }
    
    @Override
    public Status.Severity getInvalidValueSeverity( final String invalidValue )
    {
        return this.invalidValueSeverity;
    }
    
    @Override
    public boolean isCaseSensitive()
    {
        return this.caseSensitive;
    }

    @Override
    public boolean ordered()
    {
        return this.ordered;
    }

    private void refresh()
    {
        final Element element = context( Element.class );
        
        if( ! element.disposed() )
        {
            final SetFactory<String> newValuesFactory = SetFactory.start();
            
            context( Element.class ).visit
            (
                this.path,
                new PropertyVisitor()
                {
                    @Override
                    public boolean visit( final Value<?> property )
                    {
                        newValuesFactory.add( property.text() );
                        return true;
                    }
                }
            );
            
            final Set<String> newValues = newValuesFactory.result();
            
            if( ! this.values.equals( newValues ) )
            {
                this.values = newValues;
                
                if( this.initialized || this.readPriorToInit )
                {
                    broadcast();
                }
            }
        }
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && property.hasAnnotation( PossibleValues.class ) && property.getAnnotation( PossibleValues.class ).property().length() > 0 );
        }
    }
    
}
