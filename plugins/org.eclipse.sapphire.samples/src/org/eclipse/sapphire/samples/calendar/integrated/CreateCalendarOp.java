/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.samples.calendar.integrated;

import org.eclipse.sapphire.PreferDefaultValue;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.workspace.CreateWorkspaceFileOp;
import org.eclipse.sapphire.workspace.WorkspaceFileType;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@WorkspaceFileType( ICalendar.class )
@GenerateImpl

public interface CreateCalendarOp extends CreateWorkspaceFileOp
{
    ModelElementType TYPE = new ModelElementType( CreateCalendarOp.class );
    
    // *** FileName ***
    
    @DefaultValue( text = "calendar.xml" )
    @PreferDefaultValue

    ValueProperty PROP_FILE_NAME = new ValueProperty( TYPE, CreateWorkspaceFileOp.PROP_FILE_NAME );
    
}
