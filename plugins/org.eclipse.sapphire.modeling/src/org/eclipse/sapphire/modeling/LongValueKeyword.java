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

package org.eclipse.sapphire.modeling;

import java.text.NumberFormat;


/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LongValueKeyword

    extends ValueKeyword
    
{

    public LongValueKeyword( final String keyword,
                             final String value )
    {
        super( keyword, value );
    }

    @Override
    protected String createDisplayString( final String keyword,
                                          final String value )
    {
        String formattedValue = value;
        
        try
        {
            final long parsedValue = Long.parseLong( value );
            formattedValue = NumberFormat.getInstance().format( parsedValue );
        }
        catch( NumberFormatException e )
        {
            LoggingService.log( e );
        }
        
        final StringBuilder buf = new StringBuilder();
        
        buf.append( keyword );
        buf.append( " (" );
        buf.append( formattedValue );
        buf.append( ")" );
        
        return buf.toString();
    }
    
}
