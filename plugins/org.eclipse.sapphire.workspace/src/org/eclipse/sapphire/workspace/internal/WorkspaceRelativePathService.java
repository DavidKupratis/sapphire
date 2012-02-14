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

package org.eclipse.sapphire.workspace.internal;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.services.RelativePathService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.workspace.WorkspaceRelativePath;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class WorkspaceRelativePathService extends RelativePathService
{
    @Override
    public List<Path> roots()
    {
        return Collections.singletonList( new Path( ResourcesPlugin.getWorkspace().getRoot().getLocation().toPortableString() ) );
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && Path.class.isAssignableFrom( property.getTypeClass() ) && property.hasAnnotation( WorkspaceRelativePath.class ) );
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new WorkspaceRelativePathService();
        }
    }
    
}
