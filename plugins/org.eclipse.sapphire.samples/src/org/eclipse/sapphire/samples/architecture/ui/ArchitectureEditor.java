/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Shenxue Zhou - handle external file input
 ******************************************************************************/

package org.eclipse.sapphire.samples.architecture.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.samples.architecture.IArchitecture;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.form.editors.masterdetails.MasterDetailsEditorPage;
import org.eclipse.sapphire.ui.gef.diagram.editor.SapphireDiagramEditor;
import org.eclipse.sapphire.ui.swt.xml.editor.XmlEditorResourceStore;
import org.eclipse.ui.PartInitException;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ArchitectureEditor extends SapphireEditor
{
    private IArchitecture architectureModel;
    private StructuredTextEditor pageSource;
    private SapphireDiagramEditor pageDiagram;
    private MasterDetailsEditorPage pageDetails;
    
    public ArchitectureEditor()
    {
        super( "org.eclipse.sapphire.samples" );
    }

    @Override
    protected void createSourcePages() throws PartInitException 
    {
        this.pageSource = new StructuredTextEditor();
        this.pageSource.setEditorPart( this );
        
        int index = addPage( this.pageSource, getEditorInput() );
        setPageText( index, "Source" );
    }

    @Override
    protected IModelElement createModel() 
    {
        this.architectureModel = IArchitecture.TYPE.instantiate( new RootXmlResource( new XmlEditorResourceStore( this, this.pageSource ) ) );
        return this.architectureModel;
    }

    @Override
    protected void createDiagramPages() throws PartInitException
    {
        IPath path = new Path( "org.eclipse.sapphire.samples/org/eclipse/sapphire/samples/architecture/ArchitectureEditor.sdef/DiagramPage" );
        this.pageDiagram = new SapphireDiagramEditor( this.architectureModel, path );
        addPage( 0, this.pageDiagram, getEditorInput() );
        setPageText( 0, "Diagram" );
        setPageId( this.pages.get( 0 ), "Diagram", this.pageDiagram.getPart() );
    }
    
    @Override
    protected void createFormPages() throws PartInitException 
    {
        IPath path = new Path( "org.eclipse.sapphire.samples/org/eclipse/sapphire/samples/architecture/ArchitectureEditor.sdef/DetailsPage" );
        this.pageDetails = new MasterDetailsEditorPage( this, this.architectureModel, path );
        addPage( 1, this.pageDetails );
        setPageText( 1, "Details" );
        setPageId( this.pages.get( 1 ), "Details", this.pageDetails.getPart() );        
    }

    @Override
    public void doSave( final IProgressMonitor monitor )
    {
        this.pageDiagram.doSave( monitor );
        super.doSave( monitor );        
    }
    
}
