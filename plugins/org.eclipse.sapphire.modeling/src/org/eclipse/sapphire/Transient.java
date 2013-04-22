/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire;

import static org.eclipse.sapphire.modeling.util.MiscUtil.equal;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class Transient<T> extends Property
{
    private T content;

    public Transient( final Element element,
                      final TransientProperty property )
    {
        super( element, property );
    }
    
    @Override
    public void refresh()
    {
        synchronized( root() )
        {
            assertNotDisposed();
    
            refreshEnablement( false );
            refreshValidation( false );
        }
    }
    
    @Override
    public TransientProperty definition()
    {
        return (TransientProperty) super.definition();
    }
    
    public T content()
    {
        init();
        
        synchronized( this )
        {
            return this.content;
        }
    }
    
    @Override
    public boolean empty()
    {
        synchronized( root() )
        {
            init();
            
            return ( this.content == null );
        }
    }

    public void write( final T content )
    {
        init();
        
        PropertyEvent event = null;
        
        synchronized( this )
        {
            if( ! equal( this.content, content ) )
            {
                this.content = content;
                event = new PropertyContentEvent( this );
            }
        }
        
        if( event != null )
        {
            broadcast( event );
            
            refreshEnablement( false );
            refreshValidation( false );
        }
    }

    @Override
    public void clear()
    {
        write( null );
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    
    public void copy( final Element source )
    {
        if( source == null )
        {
            throw new IllegalArgumentException();
        }
        
        final Property p = source.property( definition() );
        
        if( p instanceof Transient<?> )
        {
            write( ( (Transient<T>) p ).content() );
        }
    }
    
    @Override
    public String toString()
    {
        final T content = content();
        return ( content == null ? "<null>" : content.toString() );
    }
    
}
