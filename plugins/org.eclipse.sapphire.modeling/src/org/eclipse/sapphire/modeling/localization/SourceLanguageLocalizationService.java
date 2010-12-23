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

package org.eclipse.sapphire.modeling.localization;

import org.eclipse.sapphire.modeling.CapitalizationType;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SourceLanguageLocalizationService

    extends LocalizationService
    
{
    @Override
    public String string( final String sourceLangString,
                          final CapitalizationType capitalizationType,
                          final boolean includeMnemonic )
    {
        return transform( sourceLangString, capitalizationType, includeMnemonic );
    }

}
