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

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.java.ClassBasedJavaType;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.java.JavaTypeReferenceService;
import org.eclipse.sapphire.modeling.ClassLocator;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.ui.def.IPackageReference;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class JavaTypeReferenceServiceForSdef extends JavaTypeReferenceService
{
    @Override
    public JavaType resolve( final String name )
    {
        final ISapphireUiDef sdef = context( ISapphireUiDef.class );
        final ClassLocator locator = sdef.adapt( ClassLocator.class );
        
        if( locator != null )
        {
            for( IPackageReference packageRef : sdef.getImportedPackages() )
            {
                final String packageName = packageRef.getName().getText();
                
                if( packageName != null )
                {
                    final String fullClassName = packageName + "." + name;
                    final Class<?> cl = locator.find( fullClassName );
                    
                    if( cl != null )
                    {
                        return new ClassBasedJavaType( cl );
                    }
                }
            }
        }
        
        return null;
    }
    
    public static final class Factory extends ServiceFactory
    {
        @Override
        public boolean applicable( final ServiceContext context,
                                   final Class<? extends Service> service )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            
            if( property != null && property.getTypeClass() == JavaTypeName.class && context.find( ISapphireUiDef.class ) != null )
            {
                final Reference referenceAnnotation = property.getAnnotation( Reference.class );
                
                if( referenceAnnotation != null && referenceAnnotation.target() == JavaType.class )
                {
                    return true;
                }
            }
            
            return false;
        }

        @Override
        public Service create( final ServiceContext context,
                               final Class<? extends Service> service )
        {
            return new JavaTypeReferenceServiceForSdef();
        }
    }

}
