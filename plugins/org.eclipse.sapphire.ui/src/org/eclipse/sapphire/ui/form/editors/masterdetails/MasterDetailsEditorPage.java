/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.form.editors.masterdetails;

import static org.eclipse.sapphire.ui.SapphireActionSystem.ACTION_OUTLINE_HIDE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.CONTEXT_EDITOR_PAGE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.CONTEXT_EDITOR_PAGE_OUTLINE;
import static org.eclipse.sapphire.ui.SapphireActionSystem.CONTEXT_EDITOR_PAGE_OUTLINE_HEADER;
import static org.eclipse.sapphire.ui.SapphireActionSystem.CONTEXT_EDITOR_PAGE_OUTLINE_NODE;
import static org.eclipse.sapphire.ui.internal.TableWrapLayoutUtil.twlayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.util.MutableReference;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.SapphireEditorFormPage;
import org.eclipse.sapphire.ui.SapphireEditorPagePart;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.SapphirePartEvent;
import org.eclipse.sapphire.ui.SapphirePartListener;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.SapphireSection;
import org.eclipse.sapphire.ui.def.IEditorPageDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentation;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.def.ISapphirePartDef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;
import org.eclipse.sapphire.ui.def.SapphireUiDefFactory;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsEditorPageDef;
import org.eclipse.sapphire.ui.form.editors.masterdetails.state.IMasterDetailsEditorPageState;
import org.eclipse.sapphire.ui.internal.SapphireUiFrameworkPlugin;
import org.eclipse.sapphire.ui.swt.renderer.SapphireActionPresentationManager;
import org.eclipse.sapphire.ui.swt.renderer.SapphireKeyboardActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.SapphireMenuActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.SapphireToolBarActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.SapphireToolBarManagerActionPresentation;
import org.eclipse.sapphire.ui.swt.renderer.internal.formtext.SapphireFormText;
import org.eclipse.sapphire.ui.util.SapphireHelpSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class MasterDetailsEditorPage 

    extends SapphireEditorFormPage
    
{
    private IMasterDetailsEditorPageDef definition;
    private RootSection mainSection;
    private ContentOutline contentOutlinePage;
    private IPartListener2 partListener;
    private SapphirePartListener editorPagePartListener;
    
    public MasterDetailsEditorPage( final SapphireEditor editor,
                                    final IModelElement rootModelElement,
                                    final IPath pageDefinitionLocation ) 
    {
        this( editor, rootModelElement, pageDefinitionLocation, null );
    }

    public MasterDetailsEditorPage( final SapphireEditor editor,
                                    final IModelElement rootModelElement,
                                    final IPath pageDefinitionLocation,
                                    final String pageName ) 
    {
        super( editor, createEditorPagePart( editor, rootModelElement, pageDefinitionLocation ) );
        
        final MasterDetailsEditorPagePart part = getPart();
        
        IMasterDetailsEditorPageState state;
        
        try
        {
            final File stateFile = editor.getDefaultStateStorageFile( this );
            state = IMasterDetailsEditorPageState.TYPE.instantiate( new RootXmlResource( new XmlResourceStore( stateFile ) ) );
        }
        catch( ResourceStoreException e )
        {
            state = IMasterDetailsEditorPageState.TYPE.instantiate();
        }
        
        part.setState( state );

        this.definition = part.getDefinition();
        
        String partName = pageName;
        
        if( partName == null )
        {
            partName = this.definition.getPageName().getLocalizedText( CapitalizationType.TITLE_STYLE, false );
        }
        
        setPartName( partName );
        
        // Content Outline
        
        final SapphireAction outlineHideAction = getPart().getActions( CONTEXT_EDITOR_PAGE ).getAction( ACTION_OUTLINE_HIDE );
        
        final SapphireActionHandler outlineHideActionHandler = new SapphireActionHandler()
        {
            @Override
            protected Object run( final SapphireRenderingContext context )
            {
                setDetailsMaximized( ! isDetailsMaximized() );
                return null;
            }
        };
        
        outlineHideActionHandler.init( outlineHideAction, null );
        outlineHideActionHandler.setChecked( isDetailsMaximized() );
        outlineHideAction.addHandler( outlineHideActionHandler );
    }

    private static MasterDetailsEditorPagePart createEditorPagePart( final SapphireEditor editor,
                                                                     final IModelElement rootModelElement,
                                                                     final IPath pageDefinitionLocation )
    {
        final String bundleId = pageDefinitionLocation.segment( 0 );
        final String pageId = pageDefinitionLocation.lastSegment();
        final String relPath = pageDefinitionLocation.removeFirstSegments( 1 ).removeLastSegments( 1 ).toPortableString();
        
        final ISapphireUiDef def = SapphireUiDefFactory.load( bundleId, relPath );
        final IEditorPageDef editorPageDef = (IEditorPageDef) def.getPartDef( pageId, true, IEditorPageDef.class );
        
        if( editorPageDef == null )
        {
            throw new RuntimeException(); // Needs error message.
        }
        
        final MasterDetailsEditorPagePart editorPagePart = new MasterDetailsEditorPagePart();
        editorPagePart.init( editor, rootModelElement, editorPageDef, Collections.<String,String>emptyMap() );
        
        return editorPagePart;
    }

    public ISapphirePartDef getDefinition()
    {
        return this.definition;
    }
    
    @Override
    public String getId()
    {
        return getPartName();
    }

    @Override
    public MasterDetailsEditorPagePart getPart()
    {
        return (MasterDetailsEditorPagePart) super.getPart();
    }
    
    public MasterDetailsContentOutline getContentTree()
    {
        return getPart().getContentOutline();
    }
    
    public IDetailsPage getCurrentDetailsPage()
    {
        return this.mainSection.getCurrentDetailsSection();
    }
    
    protected void createFormContent( final IManagedForm managedForm ) 
    {
        final SapphireEditorPagePart part = getPart();
        final ScrolledForm form = managedForm.getForm();
        
        try
        {
            FormToolkit toolkit = managedForm.getToolkit();
            toolkit.decorateFormHeading(managedForm.getForm().getForm());
            
            form.setText( this.definition.getPageHeaderText().getLocalizedText( CapitalizationType.TITLE_STYLE, false ) );
            
            this.mainSection = new RootSection();
            this.mainSection.createContent( managedForm );
            
            final ISapphireDocumentation doc = this.definition.getDocumentation().element();
            
            if( doc != null )
            {
                ISapphireDocumentationDef docdef = null;
                
                if( doc instanceof ISapphireDocumentationDef )
                {
                    docdef = (ISapphireDocumentationDef) doc;
                }
                else
                {
                    docdef = ( (ISapphireDocumentationRef) doc ).resolve();
                }
                
                if( docdef != null )
                {
                    SapphireHelpSystem.setHelp( managedForm.getForm().getBody(), docdef );
                }
            }
            
            final SapphireActionGroup actions = part.getActions( CONTEXT_EDITOR_PAGE );
            final SapphireToolBarManagerActionPresentation actionPresentation = new SapphireToolBarManagerActionPresentation( part, getSite().getShell(), actions );
            actionPresentation.setToolBarManager( form.getToolBarManager() );
            actionPresentation.render();
            
            this.editorPagePartListener = new SapphirePartListener()
            {
                @Override
                public void handleEvent( final SapphirePartEvent event )
                {
                    if( event instanceof MasterDetailsEditorPagePart.DetailsFocusRequested )
                    {
                        setFocusOnDetails();
                    }
                }
            };
            
            part.addListener( this.editorPagePartListener );
        }
        catch( final Exception e )
        {
            if( this.mainSection != null )
            {
                this.mainSection.dispose();
                this.mainSection = null;
                
                final Composite body = (Composite) ( (Form) form.getChildren()[ 0 ] ).getChildren()[ 1 ];
                
                for( Control control : body.getChildren() )
                {
                    control.dispose();
                }
                
                final Color bgcolor = body.getDisplay().getSystemColor( SWT.COLOR_WHITE );
                
                final Composite composite = new Composite( body, SWT.NONE );
                composite.setLayoutData( gdfill() );
                composite.setLayout( glayout( 1, 5, 5, 10, 5 ) );
                composite.setBackground( bgcolor );
                
                final Composite msgAndShowStackTraceLinkComposite = new Composite( composite, SWT.NONE );
                msgAndShowStackTraceLinkComposite.setLayoutData( gdhfill() );
                msgAndShowStackTraceLinkComposite.setLayout( glayout( 2, 0, 0 ) );
                msgAndShowStackTraceLinkComposite.setBackground( bgcolor );
                
                final SapphireFormText text = new SapphireFormText( msgAndShowStackTraceLinkComposite, SWT.NONE );
                text.setLayoutData( gdhfill() );
                text.setText( "<form><li style=\"image\" value=\"error\">" + e.getMessage() + "</li></form>", true, false );
                text.setImage( "error", PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
                text.setBackground( bgcolor );

                final SapphireFormText showStackTraceLink = new SapphireFormText( msgAndShowStackTraceLinkComposite, SWT.NONE );
                showStackTraceLink.setLayoutData( gd() );
                showStackTraceLink.setText( "<form><p><a href=\"show-stack\">Show stack trace...</a></p></form>", true, false );
                showStackTraceLink.setBackground( bgcolor );
                
                showStackTraceLink.addHyperlinkListener
                (
                    new HyperlinkAdapter()
                    {
                        @Override
                        public void linkActivated( final HyperlinkEvent event )
                        {
                            showStackTraceLink.setVisible( false );
                            
                            final Label separator = new Label( composite, SWT.SEPARATOR | SWT.HORIZONTAL );
                            separator.setLayoutData( gdhfill() );
                            separator.setBackground( bgcolor );
                            
                            final Text stack = new Text( composite, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL );
                            stack.setLayoutData( gdfill() );
                            stack.setBackground( bgcolor );
                            
                            final StringWriter w = new StringWriter();
                            e.printStackTrace( new PrintWriter( w ) );
                            stack.setText( w.getBuffer().toString() );
                            
                            body.layout( true, true );
                        }
                    }
                );
            }
        }
        
        this.partListener = new IPartListener2()
        {
            public void partActivated( final IWorkbenchPartReference partRef )
            {
            }

            public void partBroughtToTop( final IWorkbenchPartReference partRef )
            {
            }

            public void partClosed( final IWorkbenchPartReference partRef )
            {
                if( ! isDetailsMaximized() )
                {
                    setOutlineRatioCookie( MasterDetailsEditorPage.this.mainSection.getOutlineRatio() );
                }
            }

            public void partDeactivated( final IWorkbenchPartReference partRef )
            {
            }

            public void partOpened( final IWorkbenchPartReference partRef )
            {
            }

            public void partHidden( final IWorkbenchPartReference partRef )
            {
            }

            public void partVisible( final IWorkbenchPartReference partRef )
            {
            }

            public void partInputChanged( final IWorkbenchPartReference partRef )
            {
            }
        };
        
        getSite().getPage().addPartListener( this.partListener );
    }
    
    public IContentOutlinePage getContentOutlinePage()
    {
        if( this.contentOutlinePage == null )
        {
            this.contentOutlinePage = new ContentOutline();
        }
        
        return this.contentOutlinePage;
    }
    
    public boolean isDetailsMaximized()
    {
        return ! getPart().getState().getContentOutlineState().getVisible().getContent();
    }
    
    public void setDetailsMaximized( final boolean maximized )
    {
        this.mainSection.setDetailsMaximized( maximized );
        
        try
        {
            getPart().getState().getContentOutlineState().setVisible( ! maximized );
            getPart().getState().resource().save();
        }
        catch( Exception e )
        {
            SapphireUiFrameworkPlugin.log( e );
        }
    }
    
    public double getOutlineRatio()
    {
        double contentOutlineRatio = getPart().getState().getContentOutlineState().getRatio().getContent();
        
        if( contentOutlineRatio < 0 || contentOutlineRatio > 1 )
        {
            contentOutlineRatio = 0.3d;
        }
        
        return contentOutlineRatio;
    }
    
    public void setOutlineRatio( final Double ratio )
    {
        if( ratio < 0 || ratio > 1 )
        {
            throw new IllegalArgumentException();
        }
        
        this.mainSection.setOutlineRatio( ratio );
        setOutlineRatioCookie( ratio );
    }
    
    private void setOutlineRatioCookie( final Double ratio )
    {
        try
        {
            getPart().getState().getContentOutlineState().setRatio( ratio );
            getPart().getState().resource().save();
        }
        catch( Exception e )
        {
            SapphireUiFrameworkPlugin.log( e );
        }
    }
    
    @Override
    public void setFocus()
    {
        if( isDetailsMaximized() )
        {
            setFocusOnDetails();
        }
        else
        {
            setFocusOnContentOutline();
        }
    }
    
    public void setFocusOnContentOutline()
    {
        if( isDetailsMaximized() )
        {
            setDetailsMaximized( false );
        }

        if( this.mainSection != null )
        {
            this.mainSection.masterSection.tree.setFocus();
        }
    }
    
    public void setFocusOnDetails()
    {
        final Control control = findFirstFocusableControl( this.mainSection.detailsSectionControl );
        
        if( control != null )
        {
            control.setFocus();
        }
    }
    
    private Control findFirstFocusableControl( final Control control )
    {
        if( control instanceof Text || control instanceof Combo || control instanceof Link ||
            control instanceof List<?> || control instanceof Table || control instanceof Tree )
        {
            return control;
        }
        else if( control instanceof Text )
        {
            if( ( ( (Text) control ).getStyle() & SWT.READ_ONLY ) == 0 )
            {
                return control;
            }
        }
        else if( control instanceof Button )
        {
            int style = control.getStyle();
            
            if( ( style & SWT.CHECK ) != 0 || (style & SWT.RADIO ) != 0 )
            {
                return control;
            }
        }
        else if( control instanceof Composite )
        {
            for( Control child : ( (Composite) control ).getChildren() )
            {
                final Control res = findFirstFocusableControl( child );
                
                if( res != null )
                {
                    return res;
                }
            }
        }
        
        return null;
    }

    private FilteredTree createContentOutline( final Composite parent,
                                               final MasterDetailsContentOutline contentTree,
                                               final boolean addBorders )
    {
        final int treeStyle = ( addBorders ? SWT.BORDER : SWT.NONE ) | SWT.MULTI;
        
        final ContentOutlineFilteredTree filteredTree = new ContentOutlineFilteredTree( parent, treeStyle, contentTree );
        final TreeViewer treeViewer = filteredTree.getViewer();
        
        final ITreeContentProvider contentProvider = new ITreeContentProvider()
        {
            public Object[] getElements( final Object inputElement )
            {
                return contentTree.getRoot().getChildNodes().toArray();
            }
        
            public Object[] getChildren( final Object parentElement )
            {
                return ( (MasterDetailsContentNode) parentElement ).getChildNodes().toArray();
            }
        
            public Object getParent( final Object element )
            {
                return ( (MasterDetailsContentNode) element ).getParentNode();
            }
        
            public boolean hasChildren( final Object element )
            {
                return ( ( (MasterDetailsContentNode) element ).getChildNodes() ).size() > 0;
            }
        
            public void inputChanged( final Viewer viewer,
                                      final Object oldInput,
                                      final Object newInput )
            {
            }

            public void dispose()
            {
            }
        };
        
        final LabelProvider labelProvider = new LabelProvider()
        {
            private final Map<ImageDescriptor,Image> images = new HashMap<ImageDescriptor,Image>();
            
            @Override
            public String getText( final Object element ) 
            {
                return ( (MasterDetailsContentNode) element ).getLabel();
            }
        
            @Override
            public Image getImage( final Object element ) 
            {
                return getImage( (MasterDetailsContentNode) element );
            }
            
            private Image getImage( final MasterDetailsContentNode node )
            {
                final ImageDescriptor imageDescriptor = node.getImage();
                Image image = this.images.get( imageDescriptor );
                
                if( image == null )
                {
                    image = imageDescriptor.createImage();
                    this.images.put( imageDescriptor, image );
                }
                
                return image;
            }
            
            @Override
            public void dispose()
            {
                for( Image image : this.images.values() )
                {
                    image.dispose();
                }
            }
        };
        
        treeViewer.setContentProvider( contentProvider );
        treeViewer.setLabelProvider( labelProvider );
        treeViewer.setInput( new Object() );
        
        final MutableReference<Boolean> ignoreSelectionChange = new MutableReference<Boolean>( false );
        final MutableReference<Boolean> ignoreExpandedStateChange = new MutableReference<Boolean>( false );
        
        final MasterDetailsContentOutline.Listener contentTreeListener = new MasterDetailsContentOutline.Listener()
        {
            @Override
            public void handleNodeUpdate( final MasterDetailsContentNode node )
            {
                treeViewer.update( node, null );
            }

            @Override
            public void handleNodeStructureChange( final MasterDetailsContentNode node )
            {
                treeViewer.refresh( node );
            }

            @Override
            public void handleSelectionChange( final List<MasterDetailsContentNode> selection )
            {
                if( ignoreSelectionChange.get() == true )
                {
                    return;
                }
                
                final IStructuredSelection sel;
                
                if( selection.isEmpty() )
                {
                    sel = StructuredSelection.EMPTY;
                }
                else
                {
                    sel = new StructuredSelection( selection );
                }
                
                if( ! treeViewer.getSelection().equals( sel ) )
                {
                    for( MasterDetailsContentNode node : selection )
                    {
                        treeViewer.reveal( node );
                    }
                    
                    treeViewer.setSelection( sel );
                }
            }
            
            @Override
            public void handleNodeExpandedStateChange( final MasterDetailsContentNode node )
            {
                if( ignoreExpandedStateChange.get() == true )
                {
                    return;
                }
                
                final boolean expandedState = node.isExpanded();
                
                if( treeViewer.getExpandedState( node ) != expandedState )
                {
                    treeViewer.setExpandedState( node, expandedState );
                }
            }

            @Override
            public void handleFilterChange( final String newFilterText )
            {
                filteredTree.changeFilterText( newFilterText );
            }
        };

        contentTree.addListener( contentTreeListener );
        
        treeViewer.addSelectionChangedListener
        (
            new ISelectionChangedListener()
            {
                public void selectionChanged( final SelectionChangedEvent event )
                {
                    ignoreSelectionChange.set( true );
                    
                    try
                    {
                        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                        final List<MasterDetailsContentNode> nodes = new ArrayList<MasterDetailsContentNode>();
                        
                        for( Iterator<?> itr = selection.iterator(); itr.hasNext(); )
                        {
                            nodes.add( (MasterDetailsContentNode) itr.next() );
                        }
                        
                        contentTree.setSelectedNodes( nodes );
                    }
                    finally
                    {
                        ignoreSelectionChange.set( false );
                    }
                }
            }
        );
        
        treeViewer.addTreeListener
        (
            new ITreeViewerListener()
            {
                public void treeExpanded( final TreeExpansionEvent event )
                {
                    ignoreExpandedStateChange.set( true );
                    
                    try
                    {
                        final MasterDetailsContentNode node = (MasterDetailsContentNode) event.getElement();
                        node.setExpanded( true );
                    }
                    finally
                    {
                        ignoreExpandedStateChange.set( false );
                    }
                }

                public void treeCollapsed( final TreeExpansionEvent event )
                {
                    ignoreExpandedStateChange.set( true );
                    
                    try
                    {
                        final MasterDetailsContentNode node = (MasterDetailsContentNode) event.getElement();
                        node.setExpanded( false );
                    }
                    finally
                    {
                        ignoreExpandedStateChange.set( false );
                    }
                }
            }
        );
        
        final Tree tree = treeViewer.getTree();
        
        final ContentOutlineActionSupport actionSupport = new ContentOutlineActionSupport( contentTree, tree );
        
        treeViewer.setExpandedElements( contentTree.getExpandedNodes().toArray() );
        contentTreeListener.handleSelectionChange( contentTree.getSelectedNodes() );
        
        filteredTree.changeFilterText( contentTree.getFilterText() );
        
        tree.addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    contentTree.removeListener( contentTreeListener );
                    actionSupport.dispose();
                }
            }
        );

        return filteredTree;
    }
    
    private static void updateExpandedState( final MasterDetailsContentOutline contentTree,
                                             final Tree tree )
    {
        final Set<MasterDetailsContentNode> expandedNodes = new HashSet<MasterDetailsContentNode>();
        gatherExpandedNodes( tree.getItems(), expandedNodes );
        contentTree.setExpandedNodes( expandedNodes );
    }
    
    private static void gatherExpandedNodes( final TreeItem[] items,
                                             final Set<MasterDetailsContentNode> result )
    {
        for( TreeItem item : items )
        {
            if( item.getExpanded() == true )
            {
                result.add( (MasterDetailsContentNode) item.getData() );
                gatherExpandedNodes( item.getItems(), result );
            }
        }
    }
    
    public void dispose() 
    {
        super.dispose();
        
        getContentTree().dispose();
        
        if( this.mainSection != null ) 
        {
            this.mainSection.dispose();
        }
        
        if( this.partListener != null )
        {
            getSite().getPage().removePartListener( this.partListener );
        }
        
        if( this.editorPagePartListener != null )
        {
            getPart().removeListener( this.editorPagePartListener );
        }
    }
    
    private static final class ContentOutlineFilteredTree
    
        extends FilteredTree
        
    {
        private final MasterDetailsContentOutline contentTree;
        private WorkbenchJob refreshJob;

        public ContentOutlineFilteredTree( final Composite parent,
                                           final int treeStyle,
                                           final MasterDetailsContentOutline contentTree )
        {
            super( parent, treeStyle, new PatternFilter(), true );
            
            this.contentTree = contentTree;
            
            setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
        }

        @Override
        protected WorkbenchJob doCreateRefreshJob()
        {
            final WorkbenchJob base = super.doCreateRefreshJob();
            
            this.refreshJob = new WorkbenchJob( base.getName() ) 
            {
                public IStatus runInUIThread( final IProgressMonitor monitor ) 
                {
                    IStatus st = base.runInUIThread( new NullProgressMonitor() );
                    
                    if( st.getSeverity() == IStatus.CANCEL )
                    {
                        return st;
                    }
                    
                    ContentOutlineFilteredTree.this.contentTree.setFilterText( getFilterString() );
                    
                    updateExpandedState( ContentOutlineFilteredTree.this.contentTree, getViewer().getTree() );
                    
                    return Status.OK_STATUS;
                }
            };
            
            return this.refreshJob;
        }
        
        public void changeFilterText( final String filterText )
        {
            final String currentFilterText = getFilterString();
            
            if( currentFilterText != null && ! currentFilterText.equals( filterText ) )
            {
                setFilterText( filterText );
                textChanged();
                
                //this.refreshJob.cancel();
                //this.refreshJob.runInUIThread( null );
            }
        }
    }
    
    private final class ContentOutline

        extends Page
        implements IContentOutlinePage
        
    {
        private Composite outerComposite = null;
        private FilteredTree filteredTree = null;
        private TreeViewer treeViewer = null;
        
        public void init( final IPageSite pageSite ) 
        {
            super.init( pageSite );
            pageSite.setSelectionProvider( this );
        }
        
        @Override
        public void createControl( final Composite parent )
        {
            this.outerComposite = new Composite( parent, SWT.NONE );
            this.outerComposite.setLayout( glayout( 1, 5, 5 ) );
            this.outerComposite.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_WHITE ) );
            
            this.filteredTree = createContentOutline( this.outerComposite, getContentTree(), false );
            this.filteredTree.setLayoutData( gdfill() );
            
            this.treeViewer = this.filteredTree.getViewer();
            
            final SapphireEditorPagePart part = getPart();
            
            final SapphireActionGroup actions = part.getActions( CONTEXT_EDITOR_PAGE_OUTLINE_HEADER );
            
            final SapphireToolBarManagerActionPresentation actionsPresentation 
                = new SapphireToolBarManagerActionPresentation( part, getSite().getShell(), actions );
            
            actionsPresentation.setToolBarManager( getSite().getActionBars().getToolBarManager() );
            actionsPresentation.render();
        }
        
        @Override
        public Control getControl()
        {
            return this.outerComposite;
        }

        @Override
        public void setFocus()
        {
            this.treeViewer.getControl().setFocus();
        }

        public ISelection getSelection()
        {
            if( this.treeViewer == null ) 
            {
                return StructuredSelection.EMPTY;
            }
            
            return this.treeViewer.getSelection();
        }

        public void setSelection( final ISelection selection )
        {
            if( this.treeViewer != null ) 
            {
                this.treeViewer.setSelection( selection );
            }
        }

        public void addSelectionChangedListener( final ISelectionChangedListener listener )
        {
        }

        public void removeSelectionChangedListener( final ISelectionChangedListener listener )
        {
        }
    }
    
    private final class ContentOutlineActionSupport
    {
        private final MasterDetailsContentOutline contentTree;
        private final MasterDetailsContentOutline.Listener contentOutlineListener;
        private final Tree tree;
        private final Menu menu;
        private SapphireActionPresentationManager actionPresentationManager;
        private SapphireActionGroup tempActions;
        
        private ContentOutlineActionSupport( final MasterDetailsContentOutline contentOutline,
                                             final Tree tree )
        {
            this.contentTree = contentOutline;
            this.tree = tree;
            
            this.menu = new Menu( tree );
            this.tree.setMenu( this.menu );
            
            this.contentOutlineListener = new MasterDetailsContentOutline.Listener()
            {
                @Override
                public void handleSelectionChange( final List<MasterDetailsContentNode> selection )
                {
                    handleSelectionChangedEvent( selection );
                }
            };

            this.contentTree.addListener( this.contentOutlineListener );
            
            handleSelectionChangedEvent( contentOutline.getSelectedNodes() );
        }
        
        private void handleSelectionChangedEvent( final List<MasterDetailsContentNode> selection )
        {
            for( MenuItem item : this.menu.getItems() )
            {
                item.dispose();
            }
            
            if( this.tempActions != null )
            {
                this.tempActions.dispose();
                this.tempActions = null;
            }
            
            if( this.actionPresentationManager != null )
            {
                this.actionPresentationManager.dispose();
                this.actionPresentationManager = null;
            }
            
            final SapphireEditorPagePart part = getPart();
            final SapphireActionGroup actions;
            
            if( selection.size() == 1 )
            {
                final MasterDetailsContentNode node = selection.get( 0 );
                actions = node.getActions( CONTEXT_EDITOR_PAGE_OUTLINE_NODE );
            }
            else
            {
                this.tempActions = new SapphireActionGroup( part, CONTEXT_EDITOR_PAGE_OUTLINE );
                actions = this.tempActions;
            }
            
            this.actionPresentationManager 
                = new SapphireActionPresentationManager( new SapphireRenderingContext( part, this.menu.getShell() ), actions );
            
            final SapphireMenuActionPresentation menuActionPresentation = new SapphireMenuActionPresentation( this.actionPresentationManager );
            menuActionPresentation.setMenu( this.menu );
            menuActionPresentation.render();
            
            final SapphireKeyboardActionPresentation keyboardActionPresentation = new SapphireKeyboardActionPresentation( this.actionPresentationManager );
            keyboardActionPresentation.attach( this.tree );
            keyboardActionPresentation.render();
        }
        
        public void dispose()
        {
            this.contentTree.removeListener( this.contentOutlineListener );
            
            if( this.tempActions != null )
            {
                this.tempActions.dispose();
                this.tempActions = null;
            }
            
            if( this.actionPresentationManager != null )
            {
                this.actionPresentationManager.dispose();
                this.actionPresentationManager = null;
            }
        }
    }
    
    private final class RootSection 
    
        extends MasterDetailsBlock
        
    {
        private MasterSection masterSection;
        private List<IDetailsPage> detailsSections;
        private Control detailsSectionControl;
        
        public RootSection() 
        {
            this.detailsSections = new ArrayList<IDetailsPage>();
            this.detailsSectionControl = null;
        }

        @Override
        public void createContent( final IManagedForm managedForm ) 
        {
            super.createContent( managedForm );
            
            setOutlineRatio( MasterDetailsEditorPage.this.getOutlineRatio() );
            
            try
            {
                final Field field = this.detailsPart.getClass().getDeclaredField( "pageBook" );
                field.setAccessible( true );
                this.detailsSectionControl = (Control) field.get( this.detailsPart );
            }
            catch( Exception e )
            {
                SapphireUiFrameworkPlugin.log( e );
            }
            
            this.masterSection.handleSelectionChangedEvent( getContentTree().getSelectedNodes() );
            
            setDetailsMaximized( isDetailsMaximized() );
        }
        
        @Override
        protected void createMasterPart( final IManagedForm managedForm, 
                                         final Composite parent ) 
        {
            this.masterSection = new MasterSection( managedForm, parent );
            final SectionPart spart = new SectionPart(this.masterSection);
            managedForm.addPart(spart);
        }

        @Override
        protected void registerPages( final DetailsPart detailsPart ) 
        {
            final IDetailsPage detailsPage = new DetailsSection();
            detailsPart.registerPage( MasterDetailsContentNode.class, detailsPage );
            this.detailsSections.add( detailsPage );
        }
        
        @Override
        protected void applyLayoutData( final SashForm sashForm )
        {
            sashForm.setLayoutData( gdwhint( gdhhint( gdfill(), 200 ), 400 ) );
        }

        public IDetailsPage getCurrentDetailsSection()
        {
            return this.detailsPart.getCurrentPage();
        }
        
        public void setDetailsMaximized( final boolean maximized )
        {
            this.sashForm.setMaximizedControl( maximized ? this.detailsSectionControl : null );
        }
        
        public double getOutlineRatio()
        {
            final Control[] children = this.sashForm.getChildren();
            
            final int outline = children[ 0 ].getSize().x;
            final int details = children[ 1 ].getSize().x;
            final int total = outline + details;
            final double ratio = ( (double) outline ) / ( (double) total );
            
            return ratio;
        }
        
        public void setOutlineRatio( final double ratio )
        {
            final int total = Integer.MAX_VALUE;
            final int outline = (int) ( total * ratio );
            final int details = total - outline;
            
            this.sashForm.setWeights( new int[] { outline, details } );
        }
        
        public void dispose()
        {
            if( this.masterSection != null )
            {
                this.masterSection.dispose();
            }
            
            for( IDetailsPage section : this.detailsSections )
            {
                section.dispose();
            }
        }
        
        @Override
        protected void createToolBarActions( IManagedForm managedForm )
        {
        }
    }
    
    private final class MasterSection 

        extends Section
        
    {
        private IManagedForm managedForm;
        private SectionPart sectionPart;
        private TreeViewer treeViewer;
        private Tree tree;
        
        public MasterSection( final IManagedForm managedForm,
                              final Composite parent) 
        {
            super( parent, Section.TITLE_BAR );
            
            final FormToolkit toolkit = managedForm.getToolkit();
            
            FormColors colors = toolkit.getColors();
            this.setMenu(parent.getMenu());
            toolkit.adapt(this, true, true);
            if (this.toggle != null) {
                this.toggle.setHoverDecorationColor(colors
                        .getColor(IFormColors.TB_TOGGLE_HOVER));
                this.toggle.setDecorationColor(colors
                        .getColor(IFormColors.TB_TOGGLE));
            }
            this.setFont(createBoldFont(colors.getDisplay(), this.getFont()));
            colors.initializeSectionToolBarColors();
            this.setTitleBarBackground(colors.getColor(IFormColors.TB_BG));
            this.setTitleBarBorderColor(colors
                    .getColor(IFormColors.TB_BORDER));
            this.setTitleBarForeground(colors
                    .getColor(IFormColors.TB_TOGGLE));
            
            this.marginWidth = 10;
            this.marginHeight = 10;
            setLayoutData( gdfill() );
            setLayout( glayout( 1, 0, 0 ) );
            
            setText( MasterDetailsEditorPage.this.definition.getOutlineHeaderText().getLocalizedText( CapitalizationType.TITLE_STYLE, false ) );
            
            final Composite client = toolkit.createComposite( this );
            client.setLayout( glayout( 1, 0, 0 ) );
            
            this.managedForm = managedForm;
            
            final MasterDetailsContentOutline contentTree = getContentTree();
            
            final FilteredTree filteredTree = createContentOutline( client, contentTree, true );
            this.treeViewer = filteredTree.getViewer();
            this.tree = this.treeViewer.getTree();
            
            this.sectionPart = new SectionPart( this );
            this.managedForm.addPart( this.sectionPart );
    
            contentTree.addListener
            (
                new MasterDetailsContentOutline.Listener()
                {
                    @Override
                    public void handleSelectionChange( final List<MasterDetailsContentNode> selection )
                    {
                        handleSelectionChangedEvent( selection );
                    }
                }
            );
            
            final ToolBar toolbar = new ToolBar( this, SWT.FLAT | SWT.HORIZONTAL );
            setTextClient( toolbar );
            
            final SapphireEditorPagePart part = getPart();
            
            final SapphireActionGroup actions = part.getActions( CONTEXT_EDITOR_PAGE_OUTLINE_HEADER );
            
            final SapphireToolBarActionPresentation actionsPresentation 
                = new SapphireToolBarActionPresentation( part, getSite().getShell(), actions );
            
            actionsPresentation.setToolBar( toolbar );
            actionsPresentation.render();
            
            toolkit.paintBordersFor( this );
            setClient( client );
        }
        
        private Font createBoldFont(Display display, Font regularFont) {
            FontData[] fontDatas = regularFont.getFontData();
            for (int i = 0; i < fontDatas.length; i++) {
                fontDatas[i].setStyle(fontDatas[i].getStyle() | SWT.BOLD);
            }
            return new Font(display, fontDatas);
        }
        
        private void handleSelectionChangedEvent( final List<MasterDetailsContentNode> selection )
        {
            final IStructuredSelection sel
                = ( selection.isEmpty() ? StructuredSelection.EMPTY : new StructuredSelection( selection.get( 0 ) ) );
            
            this.managedForm.fireSelectionChanged( this.sectionPart, sel );
        }
    }
    
    private static class DetailsSection 

        extends SapphireRenderingContext
        implements IDetailsPage
        
    {
        private MasterDetailsContentNode node;
        protected IManagedForm mform;
        protected FormToolkit toolkit;
        
        public DetailsSection()
        {
            super( null, null );
        }
        
        public SapphirePart getPart()
        {
            return this.node;
        }
        
        public void initialize( final IManagedForm form ) 
        {
            this.mform = form;
            this.toolkit = this.mform.getToolkit();
        }
    
        public final void createContents( final Composite parent ) 
        {
            this.composite = parent;
            this.shell = this.composite.getShell();
            
            final TableWrapLayout twl = twlayout( 1, 10, 10, 10, 10 );
            twl.verticalSpacing = 20;

            parent.setLayout( twl );
            
            createSections();
        }
        
        public void commit(boolean onSave) {
        }
    
        public void dispose() {
        }
    
        public boolean isDirty() {
            return false;
        }
    
        public boolean isStale() {
            return false;
        }
    
        public void refresh()
        {
        }

        public void setFocus() {
        }
        
        public boolean setFormInput(Object input) {
            return false;
        }
    
        public void adapt( final Control control )
        {
            super.adapt( control );
            
            if( control instanceof Composite )
            {
                this.toolkit.adapt( (Composite) control );
            }
            else if( control instanceof Label )
            {
                this.toolkit.adapt( control, false, false );
            }
            else
            {
                this.toolkit.adapt( control, true, true );
            }
        }
        
        public void selectionChanged( final IFormPart part, 
                                      final ISelection selection ) 
        {
            final IStructuredSelection ssel = (IStructuredSelection) selection;
            
            if( ssel.size() == 1 ) 
            {
                this.node = (MasterDetailsContentNode) ssel.getFirstElement();
            }
            else
            {
                this.node = null;
            }
            
            createSections();
        }
        
        protected void createSections()
        {
            final Composite rootComposite = getComposite();
            
            for( Control control : rootComposite.getChildren() )
            {
                control.setVisible( false );
                control.dispose();
            }
            
            if( this.node != null )
            {
                for( SapphireSection section : this.node.getSections() )
                {
                    if( section.checkVisibleWhenCondition() == false )
                    {
                        continue;
                    }
                    
                    section.render( this );
                }
            }
            
            rootComposite.getParent().layout( true, true );
        }
    }

}