/******************************************************************************
 * Copyright (c) 2013 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - [371697] ClassCastException in XmlEditorResourceStore for non-local files
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.xml.editor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.sapphire.Context;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.ElementDisposeEvent;
import org.eclipse.sapphire.modeling.ResourceStoreException;
import org.eclipse.sapphire.modeling.ValidateEditException;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.modeling.xml.XmlNode;
import org.eclipse.sapphire.modeling.xml.XmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.modeling.xml.XmlValueBindingImpl;
import org.eclipse.sapphire.ui.DelayedTasksExecutor;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.sapphire.ui.SourceEditorService;
import org.eclipse.sapphire.ui.swt.xml.editor.internal.JavaProjectContext;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.internal.provisional.extensions.ISourceEditingTextTools;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.provisional.IDOMSourceEditingTextTools;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@SuppressWarnings( "restriction" )

public class XmlEditorResourceStore extends XmlResourceStore
{
    private SapphireEditor sapphireEditor;
    private StructuredTextEditor sourceEditor;
    private Element rootModelElement;
    private final Map<Node,List<Element>> nodeToModelElementsMap;
    private final Scrubber scrubber;
    private final Listener modelElementDisposeListener;
    private final XmlSourceEditorService sourceEditorService;
    private final INodeAdapter xmlNodeListener;
    
    public XmlEditorResourceStore( final SapphireEditor sapphireEditor,
                                   final StructuredTextEditor sourceEditor )
    {
        super( (ByteArrayResourceStore) null );
        
        this.sapphireEditor = sapphireEditor;
        this.sourceEditor = sourceEditor;
        this.rootModelElement = null;
        this.nodeToModelElementsMap = new HashMap<Node,List<Element>>();
        this.scrubber = new Scrubber();
        this.scrubber.start();
        this.sourceEditorService = new XmlSourceEditorService();
        
        this.modelElementDisposeListener = new FilteredListener<ElementDisposeEvent>()
        {
            @Override
            protected void handleTypedEvent( final ElementDisposeEvent event )
            {
                handleElementDisposed( event.element() );
            }
        };
        
        final ISourceEditingTextTools sourceEditingTextTools = (ISourceEditingTextTools) this.sourceEditor.getAdapter( ISourceEditingTextTools.class );
        final IDOMSourceEditingTextTools domSourceEditingTextTools = (IDOMSourceEditingTextTools) sourceEditingTextTools;
        
        setDomDocument( domSourceEditingTextTools.getDOMDocument() );
        
        this.xmlNodeListener = new INodeAdapter()
        {
            public boolean isAdapterForType( final Object type )
            {
                return false;
            }

            public void notifyChanged( final INodeNotifier notifier,
                                       final int eventType,
                                       final Object changedFeature,
                                       final Object oldValue,
                                       final Object newValue,
                                       final int pos )
            {
                /* System.err.println( "notifyChanged" );
                System.err.println( "  notifier = " + notifier.getClass().getName() );
                System.err.println( "  eventType = " + eventType );
                System.err.println( "  changedFeature = " + ( changedFeature == null ? "null" : changedFeature.getClass().getName() ) );
                System.err.println( "  oldValue = " + ( oldValue == null ? "null" : oldValue.getClass().getName() ) );
                System.err.println( "  newValue = " + ( newValue == null ? "null" : newValue.getClass().getName() ) );
                System.err.println( "  pos = " + pos ); */
                
                if( eventType == INodeNotifier.ADD && newValue instanceof IDOMNode )
                {
                    attachXmlNodeListener( (IDOMNode) newValue );
                }
                
                DelayedTasksExecutor.schedule( new RefreshElementsTask( getModelElements( (Node) notifier ) ) );
            }
        };

        attachXmlNodeListener();
    }
    
    public final SapphireEditor getEditor()
    {
        return this.sapphireEditor;
    }
    
    public final StructuredTextEditor getXmlEditor()
    {
        return this.sourceEditor;
    }

    @Override
    public boolean isXmlDeclarationNeeded()
    {
        return true;
    }

    @Override
    public void save() throws ResourceStoreException
    {
        final IEditorInput input = this.sourceEditor.getEditorInput();
        
        if( input instanceof FileEditorInput )
        {
            final IFile file = ( (FileEditorInput) input ).getFile();
            
            if( ! file.exists() )
            {
                return;
            }
        }

        validateSave();
        this.sourceEditor.doSave( new NullProgressMonitor() );
    }

    @Override
    public void validateEdit()
    {
        final IEditorInput input = this.sourceEditor.getEditorInput();
        
        if( input instanceof FileEditorInput )
        {
            final IFile file = ( (FileEditorInput) input ).getFile();
            
            if( ! file.exists() )
            {
                final IStatus st = ResourcesPlugin.getWorkspace().validateEdit( new IFile[] { file }, IWorkspace.VALIDATE_PROMPT );
                
                if( st.getSeverity() == IStatus.ERROR )
                {
                    throw new ValidateEditException();
                }
                
                try
                {
                    file.create( new ByteArrayInputStream( new byte[ 0 ] ), true, new NullProgressMonitor() );
                }
                catch( CoreException e )
                {
                    throw new ValidateEditException( e );
                }
            }
        }
        
        if( this.sourceEditor.validateEditorInputState() == false )
        {
            throw new ValidateEditException();
        }
    }
    
    @Override
    public void validateSave()
    {
        if( this.sourceEditor.validateEditorInputState() == false )
        {
            throw new ValidateEditException();
        }
    }
    
    @Override
    public <A> A adapt( final Class<A> adapterType )
    {
        A result = null;
        
        if( adapterType == ITextEditor.class )
        {
            result = adapterType.cast( getXmlEditor() );
        }
        else if( adapterType == SourceEditorService.class )
        {
            result = adapterType.cast( this.sourceEditorService );
        }
        else if( adapterType == File.class )
        {
            final IEditorInput input = this.sourceEditor.getEditorInput();
            
            if( input instanceof FileEditorInput)
            {
                result = adapterType.cast( ( (FileEditorInput) input ).getFile().getLocation().toFile() );
            }
            else if( input instanceof FileStoreEditorInput )
            {
                //Handle files that are not part of the current workspace.
                
                final URI uri = ( (FileStoreEditorInput) input ).getURI();
                result = adapterType.cast( new File( uri ) );
            }
        }
        else if( adapterType == IFile.class )
        {
            final IEditorInput input = this.sourceEditor.getEditorInput();
            
            if( input instanceof FileEditorInput )
            {
                result = adapterType.cast( ( (FileEditorInput) input ).getFile() );
            }
        }
        else if( adapterType == IProject.class )
        {
            final IEditorInput input = this.sourceEditor.getEditorInput();
            
            if( input instanceof FileEditorInput )
            {
                result = adapterType.cast( ( (FileEditorInput) input ).getFile().getProject() );
            }
        }
        else if( adapterType == IEditorInput.class )
        {
        	result = adapterType.cast( this.sourceEditor.getEditorInput() );
        }
        else if( adapterType == SapphireEditor.class )
        {
            result = adapterType.cast( this.sapphireEditor );
        }
        else if( adapterType == Context.class )
        {
            final IProject project = adapt( IProject.class );
            
            if( project != null )
            {
                final IJavaProject jproject = JavaCore.create( project );
                return adapterType.cast( new JavaProjectContext( jproject ) );
            }
        }
        else
        {
            result = super.adapt( adapterType );
        }
        
        return result;
    }

    @Override
    public void registerRootModelElement( final Element rootModelElement )
    {
        this.rootModelElement = rootModelElement;
    }

    @Override
    public void registerModelElement( final Node xmlNode,
                                      final Element element )
    {
        synchronized( this.nodeToModelElementsMap )
        {
            List<Element> elements = this.nodeToModelElementsMap.get( xmlNode );
            
            if( elements == null )
            {
                elements = new CopyOnWriteArrayList<Element>();
                this.nodeToModelElementsMap.put( xmlNode, elements );
            }
            
            elements.add( element );
            element.attach( this.modelElementDisposeListener );
        }
    }
    
    @Override
    public void unregisterModelElement( final Node xmlNode,
                                        final Element element )
    {
        synchronized( this.nodeToModelElementsMap )
        {
            List<Element> elements = this.nodeToModelElementsMap.get( xmlNode );
            
            if( elements != null )
            {
                elements.remove( element );
                
                if( elements.size() == 0 )
                {
                    this.nodeToModelElementsMap.remove( xmlNode );
                }
            }
        }
    }

    @Override
    public void dispose()
    {
        super.dispose();
        detachXmlNodeListener();
        this.scrubber.dispose();
    }

    private void handleElementDisposed( final Element element )
    {
        final Resource resource = element.resource();
        
        if( resource instanceof XmlResource )
        {
            final XmlElement xmlElement = ( (XmlResource) resource ).getXmlElement(); 
            
            if( xmlElement != null )
            {
                synchronized( this.nodeToModelElementsMap )
                {
                    final Node xmlNode = xmlElement.getDomNode();
                    final List<Element> elements = this.nodeToModelElementsMap.get( xmlNode );
                    
                    if( elements != null )
                    {
                        elements.remove( element );
                        
                        if( elements.isEmpty() )
                        {
                            this.nodeToModelElementsMap.remove( xmlNode );
                        }
                    }
                }
            }
        }
    }

    public final List<Element> getModelElements( final Node xmlNode )
    {
        synchronized( this.nodeToModelElementsMap )
        {
            Node node = xmlNode;
            List<Element> elements = this.nodeToModelElementsMap.get( node );
            
            while( elements == null && node != null && ! ( node instanceof Document ) )
            {
                node = node.getParentNode();
                elements = this.nodeToModelElementsMap.get( node );
            }
            
            if( elements == null )
            {
                elements = Collections.singletonList( this.rootModelElement );
            }
            else
            {
                elements = Collections.unmodifiableList( elements );
            }
            
            return elements;
        }
    }
    
    private void attachXmlNodeListener()
    {
        attachXmlNodeListener( (IDOMNode) getDomDocument() );
    }
    
    private void attachXmlNodeListener( final IDOMNode node )
    {
        node.addAdapter( this.xmlNodeListener );
        
        final NodeList children = node.getChildNodes();
        
        for( int i = 0, n = children.getLength(); i < n; i++ )
        {
            attachXmlNodeListener( (IDOMNode) children.item( i ) );
        }
    }
    
    private void detachXmlNodeListener()
    {
        detachXmlNodeListener( (IDOMNode) getDomDocument() );
    }
    
    private void detachXmlNodeListener( final IDOMNode node )
    {
        node.removeAdapter( this.xmlNodeListener );
        
        final NodeList children = node.getChildNodes();
        
        for( int i = 0, n = children.getLength(); i < n; i++ )
        {
            detachXmlNodeListener( (IDOMNode) children.item( i ) );
        }
    }
    
    private static final class RefreshElementsTask extends DelayedTasksExecutor.Task
    {
        private final List<Element> elements;
        
        public RefreshElementsTask( final List<Element> elements )
        {
            this.elements = elements;
        }
        
        @Override
        public boolean equals( final Object obj )
        {
            if( obj != null && obj instanceof RefreshElementsTask )
            {
                return ( this.elements.equals( ( (RefreshElementsTask) obj ).elements ) );
            }
            
            return false;
        }
        
        @Override
        public int hashCode()
        {
            return this.elements.hashCode();
        }
        
        public void run()
        {
            for( final Element element : this.elements )
            {
                if( ! element.disposed() )
                {
                    element.refresh();
                }
            }
        }
    }

    private final class Scrubber extends Thread
    {
        private boolean stopRequested = false;
        
        public void run()
        {
            final Map<Node,List<Element>> nodeToModelElementsMap = XmlEditorResourceStore.this.nodeToModelElementsMap;
            
            while( true )
            {
                try
                {
                    sleep( 10000 );
                }
                catch( InterruptedException e ) {}
                
                synchronized( this )
                {
                    if( this.stopRequested == true )
                    {
                        return;
                    }
                }
                
                synchronized( nodeToModelElementsMap )
                {
                    for( Iterator<Map.Entry<Node,List<Element>>> itr = nodeToModelElementsMap.entrySet().iterator();
                         itr.hasNext(); )
                    {
                        final Map.Entry<Node,List<Element>> entry = itr.next();
                        
                        if( entry.getKey().getParentNode() == null )
                        {
                            /* final String nodeClassName = entry.getKey().getClass().getSimpleName();
                            final int nodeHashCode = entry.getKey().hashCode();
                            final String modelElementClassName = entry.getValue().getClass().getSimpleName();
                            final int modelElementHashCode = entry.getValue().hashCode();
                            
                            System.err.println( "SCRUBBER REMOVED: " + nodeClassName + ":" + nodeHashCode + " -> " +
                                                modelElementClassName + ":" + modelElementHashCode ); */
                            
                            itr.remove();
                        }
                    }
                }
            }
        }
        
        public synchronized void dispose()
        {
            this.stopRequested = true;
            interrupt();
        }
    }
    
    private final class XmlSourceEditorService extends SourceEditorService
    {
        @Override
        public boolean find( final Element element,
                             final PropertyDef property )
        {
            return ( element.resource() instanceof XmlResource );
        }
        
        @Override
        public void show( final Element element,
                          final PropertyDef property )
        {
            final ITextEditor sourceView = getXmlEditor();
            final Range range = new Range();
            
            if( property != null )
            {
                final List<XmlNode> xmlNodes = getXmlNodes( element, property );
                
                if( ! xmlNodes.isEmpty() )
                {
                    if( property instanceof ValueProperty )
                    {
                        final IDOMNode domNode = (IDOMNode) xmlNodes.get( 0 ).getDomNode();
                        
                        if( domNode instanceof IDOMElement )
                        {
                            final IDOMElement domElement = (IDOMElement) domNode;
                            
                            if( domElement.hasEndTag() )
                            {
                                range.merge( domElement.getStartEndOffset(), domElement.getEndStartOffset() );
                            }
                            else
                            {
                                range.merge( domNode.getStartOffset(), domNode.getEndOffset() );
                            }
                        }
                        else if( domNode instanceof IDOMAttr )
                        {
                            final IDOMAttr domAttr = (IDOMAttr) domNode;
                            final int start = domAttr.getValueRegionStartOffset();
                            range.merge( start + 1, start + domAttr.getValueRegionText().length() - 1 );
                        }
                        else
                        {
                            range.merge( domNode.getStartOffset(), domNode.getEndOffset() );
                        }
                    }
                    else
                    {
                        for( XmlNode xmlNode : xmlNodes )
                        {
                            final IDOMNode domNode = (IDOMNode) xmlNode.getDomNode();
                            range.merge( domNode.getStartOffset(), domNode.getEndOffset() );
                        }
                    }
                }
            }
            
            if( ! range.initialized() )
            {
                Element modElement = element;
                Resource resource = modElement.resource();
                XmlElement xmlElement = null;
                
                if( resource != null )
                {
                    xmlElement = ( (XmlResource) resource ).getXmlElement();
                }
                
                while( xmlElement == null && modElement != null )
                {
                    final Property parent = modElement.parent();
                    
                    if( parent == null )
                    {
                        modElement = null;
                    }
                    else
                    {
                        modElement = parent.element();
                        resource = modElement.resource();
                        
                        if( resource != null )
                        {
                            xmlElement = ( (XmlResource) resource ).getXmlElement();
                        }
                    }
                }
                    
                if( xmlElement != null )
                {
                    final IDOMNode domNode = (IDOMNode) xmlElement.getDomNode();
                    range.merge( domNode.getStartOffset(), domNode.getEndOffset() );
                }
            }
            
            final TextSelection textSelection
                = ( range.initialized() ? new TextSelection( range.start(), range.end() - range.start() ) : null );

            sourceView.getSelectionProvider().setSelection( textSelection );
            
            getEditor().showPage( sourceView );
        }
        
        private List<XmlNode> getXmlNodes( final Element element,
                                           final PropertyDef property )
        {
            if( property instanceof ListProperty )
            {
                final ElementList<?> list = element.property( (ListProperty) property );
                final List<XmlNode> xmlNodes = new ArrayList<XmlNode>( list.size() );
                
                for( Element entry : list )
                {
                    final Resource resource = entry.resource();
                    
                    if( resource instanceof XmlResource )
                    {
                        final XmlNode xmlNode = ( (XmlResource) resource ).getXmlElement();
                        
                        if( xmlNode != null )
                        {
                            xmlNodes.add( xmlNode );
                        }
                    }
                }
                
                return xmlNodes;
            }
            else if( property instanceof ElementProperty && ! ( property instanceof ImpliedElementProperty ) )
            {
                final Element child = element.property( (ElementProperty) property ).content();
                
                if( child != null )
                {
                    final Resource resource = child.resource();
                    
                    if( resource instanceof XmlResource )
                    {
                        final XmlNode xmlNode = ( (XmlResource) resource ).getXmlElement();
                        
                        if( xmlNode != null )
                        {
                            return Collections.singletonList( xmlNode );
                        }
                    }
                }
            }
            else
            {
                final Resource resource = element.resource();
                
                if( resource instanceof XmlResource )
                {
                    final XmlResource r = (XmlResource) resource;
                    final XmlNode xmlNode = ( (XmlValueBindingImpl) r.binding( element.property( property ) ) ).getXmlNode();
                    
                    if( xmlNode != null )
                    {
                        return Collections.singletonList( xmlNode );
                    }
                }
            }
            
            return Collections.emptyList();
        }
    }
    
}
