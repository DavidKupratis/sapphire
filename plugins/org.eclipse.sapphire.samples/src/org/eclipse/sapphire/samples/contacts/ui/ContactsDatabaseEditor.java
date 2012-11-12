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

package org.eclipse.sapphire.samples.contacts.ui;

import org.eclipse.sapphire.samples.contacts.ContactsDatabase;
import org.eclipse.sapphire.ui.swt.xml.editor.SapphireEditorForXml;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ContactsDatabaseEditor extends SapphireEditorForXml
{
    public ContactsDatabaseEditor()
    {
        super( "org.eclipse.sapphire.samples" );
        
        setRootModelElementType( ContactsDatabase.TYPE );
        setEditorDefinitionPath( "org.eclipse.sapphire.samples/org/eclipse/sapphire/samples/contacts/ContactsDatabaseEditor.sdef/main" );
    }
    
}