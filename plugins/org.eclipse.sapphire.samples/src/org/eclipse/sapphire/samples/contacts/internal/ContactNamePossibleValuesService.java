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

package org.eclipse.sapphire.samples.contacts.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.samples.contacts.IContact;
import org.eclipse.sapphire.samples.contacts.IContactsDatabase;
import org.eclipse.sapphire.services.PossibleValuesService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ContactNamePossibleValuesService extends PossibleValuesService
{
    @Override
    protected void fillPossibleValues( final SortedSet<String> values )
    {
        for( IContact contact : context( IContactsDatabase.class ).getContacts() )
        {
            final String name = contact.getName().getText();
            
            if( name != null )
            {
                values.add( name );
            }
        }
    }

}