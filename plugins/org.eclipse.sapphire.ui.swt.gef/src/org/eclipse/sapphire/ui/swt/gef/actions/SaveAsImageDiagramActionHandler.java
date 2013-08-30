/******************************************************************************
 * Copyright (c) 2013 Liferay and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gregory Amerson - initial implementation
 *    Konstantin Komissarchik - initial implementation review and related changes
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.actions;

import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.swt.gef.DiagramRenderingContext;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

/**
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class SaveAsImageDiagramActionHandler extends SapphireActionHandler
{
    @Text( "Save as Image" )
    private static LocalizableText saveAsImageMessage;

    static
    {
        LocalizableText.init( SaveAsImageDiagramActionHandler.class );
    }

    @Override
    protected Object run( final Presentation context )
    {
        DiagramRenderingContext diagramContext = (DiagramRenderingContext) context;
        SapphireDiagramEditor diagramEditor = diagramContext.getDiagramEditor();

        if( diagramEditor != null )
        {
            FileDialog dialog = new FileDialog( context.getShell(), SWT.SAVE );

            IEditorInput editorInput = diagramEditor.getPart().adapt( IEditorInput.class );

            if( editorInput instanceof IFileEditorInput )
            {
                dialog.setFilterPath( ( (IFileEditorInput) editorInput ).getFile().getParent().getLocation().toOSString() );
            }
            
            final String editorInputName = editorInput.getName();
            final int editorInputLastDot = editorInputName.lastIndexOf( '.' );
            final StringBuilder initialFileName = new StringBuilder();
            
            if( editorInputLastDot == -1 )
            {
                initialFileName.append( editorInputName );
            }
            else
            {
                initialFileName.append( editorInputName.substring( 0, editorInputLastDot ) );
            }
            
            initialFileName.append( ".png" );

            dialog.setFileName( initialFileName.toString() );

            dialog.setFilterExtensions( new String[] { "*.png" } );

            dialog.setText( saveAsImageMessage.text() );

            dialog.setOverwrite( true );

            String filePath = dialog.open();

            if( filePath == null )
            {
                return null;
            }

            GraphicalViewer graphicalViewer = (GraphicalViewer) diagramEditor.getAdapter( GraphicalViewer.class );

            ScalableFreeformRootEditPart rootEditPart 
                = (ScalableFreeformRootEditPart) graphicalViewer.getRootEditPart();

            IFigure figure = rootEditPart.getLayer( LayerConstants.PRINTABLE_LAYERS );

            Rectangle rectangle = figure.getBounds();

            Image image = new Image( Display.getDefault(), rectangle.width, rectangle.height );

            FileOutputStream output = null;
            GC gc = null;
            SWTGraphics graphics = null;
            try
            {
                gc = new GC( image );
                graphics = new SWTGraphics( gc );
                figure.paint( graphics );

                ImageLoader loader = new ImageLoader();
                loader.data = new ImageData[] { image.getImageData() };
                
                output = new FileOutputStream( filePath );

                loader.save( output, SWT.IMAGE_PNG );
                output.flush();
            }
            catch( Exception e )
            {
                LoggingService.log( e );
            }
            finally
            {
                image.dispose();
                
                if (gc != null)
                    gc.dispose();
                
                if (graphics != null)
                    graphics.dispose();
                
            	if (output != null)
            	{
            		try
            		{
            			output.close();
            		}
            		catch (IOException e) {}
            	}
            }
        }

        return null;
    }

}
