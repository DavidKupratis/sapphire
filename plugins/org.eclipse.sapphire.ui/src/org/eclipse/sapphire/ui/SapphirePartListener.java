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

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.modeling.Status;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class SapphirePartListener
{
    public void handleEvent( final SapphirePartEvent event )
    {
        // The default implementation doesn't do anything.
    }
    
    public void handleValidateStateChange( final Status oldValidateState,
                                           final Status newValidationState )
    {
        // The default implementation doesn't do anything.
    }
    
    public void handleStructureChangedEvent( final SapphirePartEvent event )
    {
        // The default implementation doesn't do anything.
    }
    
    public void handleFocusReceivedEvent( final SapphirePartEvent event )
    {
        // The default implementation doesn't do anything.
    }
    
}
