/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - fixes to case lookup logic
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyEvent;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.ui.PartVisibilityEvent;
import org.eclipse.sapphire.ui.diagram.shape.def.ImageDef;
import org.eclipse.sapphire.ui.diagram.shape.def.LineShapeDef;
import org.eclipse.sapphire.ui.diagram.shape.def.RectangleDef;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeDef;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeFactoryCaseDef;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeFactoryDef;
import org.eclipse.sapphire.ui.diagram.shape.def.TextDef;
import org.eclipse.sapphire.util.CollectionsUtil;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class ShapeFactoryPart extends ShapePart 
{
	private ShapeFactoryDef shapeFactoryDef;
	private Element modelElement;	
	private ListProperty modelProperty;
	private String propertyName;
	private List<ShapePart> children;
	private Listener shapePropertyListener;
	private ShapePart separator;
	
	@Override
    protected void init()
    {
        super.init();
        this.modelElement = getModelElement();
        this.shapeFactoryDef = (ShapeFactoryDef)super.definition;
        this.children = new ArrayList<ShapePart>();
        
        this.propertyName = this.shapeFactoryDef.getProperty().content();
        this.modelProperty = (ListProperty)resolve(this.modelElement, this.propertyName);
        ElementList<?> list = this.modelElement.property(this.modelProperty);
        for( Element listEntryModelElement : list )
        {
        	ShapeFactoryCaseDef shapeFactoryCase = getShapeFactoryCase(listEntryModelElement);
        	ShapePart childShapePart = createShapePart(shapeFactoryCase, listEntryModelElement);
        	if (childShapePart != null)
        	{
        		this.children.add(childShapePart);
        	}
        }
        
        // Separator
        if (this.shapeFactoryDef.getSeparator().content() != null)
        {
        	this.separator = createShapePart(this.shapeFactoryDef.getSeparator().content(), this.modelElement);
        }
        
        // Add listeners
        this.shapePropertyListener = new FilteredListener<PropertyContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final PropertyContentEvent event )
            {
                handleModelPropertyChange( event );
            }
        };
        this.modelElement.attach(this.shapePropertyListener, this.propertyName);
        
    }
	
	public List<ShapePart> getChildren()
	{
		return this.children;
	}
	
	@Override
    public List<ShapePart> getActiveChildren()
    {
    	return this.children;
    }	
	
	public ShapePart getSeparator()
	{
		return this.separator;
	}

    public ElementList<Element> getModelElementList()
    {
    	ElementList<Element> list = this.modelElement.property(this.modelProperty);
    	return list;
    }
    
    public void moveChild(ShapePart childPart, int newIndex)
    {
    	ElementList<Element> list = this.modelElement.property(this.modelProperty);
    	int oldIndex = list.indexOf(childPart.getLocalModelElement());
    	this.modelElement.detach(this.shapePropertyListener, this.propertyName);
    	
    	int newNewIndex = newIndex == -1 ? this.children.size() : newIndex;
    	
    	if (oldIndex < newNewIndex)
    	{
    		
    		for (int i = oldIndex; i < newNewIndex; i++)
    		{
    			list.moveDown(childPart.getLocalModelElement());
    		}
    	}
    	else
    	{
    		for (int i = newIndex; i < oldIndex; i++)
    		{
    			list.moveUp(childPart.getLocalModelElement());
    		}
    	}
    	this.modelElement.attach(this.shapePropertyListener, this.propertyName);
    	this.children.remove(childPart);
    	if (newIndex == -1) {
        	this.children.add(childPart);
    	} else {
        	this.children.add(newIndex, childPart);
    	}
    	broadcast(new ShapeReorderEvent(this));
    }
    
	@Override
    public void dispose()
    {
        super.dispose();
        this.modelElement.detach(this.shapePropertyListener, this.propertyName);
        List<ShapePart> shapeParts = getChildren();
        for (ShapePart shapePart : shapeParts)
        {
        	shapePart.dispose();
        }        
    }
    
    public ShapePart getShapePart(Element element)
    {
        List<ShapePart> shapeParts = getChildren();
        for (ShapePart shapePart : shapeParts)
        {
            if (shapePart.getLocalModelElement() == element)
            {
                return shapePart;
            }
        }
        return null;
    }

    private ShapeFactoryCaseDef getShapeFactoryCase( final Element element )
	{
        for( ShapeFactoryCaseDef shapeFactoryCaseDef : this.shapeFactoryDef.getCases() )
        {
            final JavaType type = shapeFactoryCaseDef.getType().resolve();
            
            if( type == null )
            {
                return shapeFactoryCaseDef;
            }
            else
            {
                final Class<?> cl = type.artifact();

                if( cl == null || cl.isAssignableFrom( element.getClass() ) )
                {
                    return shapeFactoryCaseDef;
                }
            }
        }

        throw new RuntimeException();
	}
	
    private ShapePart createShapePart(ShapeDef shapeDef, Element modelElement)
    {
    	ShapePart shapePart = null;
    	if (shapeDef instanceof TextDef)
    	{
	        shapePart = new TextPart();
    	}
    	else if (shapeDef instanceof ImageDef)
    	{
    		shapePart = new ImagePart();
    	}
    	else if (shapeDef instanceof LineShapeDef)
    	{
    		shapePart = new LinePart();
    	}
    	else if (shapeDef instanceof RectangleDef)
    	{
    		shapePart = new RectanglePart();
    	}
    	else if (shapeDef instanceof ShapeFactoryDef)
    	{
    		shapePart = new ShapeFactoryPart();
    	}
    	if (shapePart != null)
    	{
    		shapePart.init(this, modelElement, shapeDef, Collections.<String,String>emptyMap());
    		shapePart.setActive(true);
            shapePart.attach
            (
                new FilteredListener<TextChangeEvent>()
                {
                    @Override
                    protected void handleTypedEvent( TextChangeEvent event )
                    {
                    	broadcast(event);
                    }
                }
            );
            shapePart.attach
            (
                new FilteredListener<ShapeUpdateEvent>()
                {
                    @Override
                    protected void handleTypedEvent( ShapeUpdateEvent event )
                    {
                    	broadcast(event);
                    }
                }
            );
            shapePart.attach
            (
                 new FilteredListener<PartVisibilityEvent>()
                 {
                    @Override
                    protected void handleTypedEvent( final PartVisibilityEvent event )
                    {
                    	broadcast(event);
                    }
                 }
            );    		
            shapePart.attach
            (
                 new FilteredListener<ShapeAddEvent>()
                 {
                    @Override
                    protected void handleTypedEvent( final ShapeAddEvent event )
                    {
                    	broadcast(event);
                    }
                 }
            );            
            shapePart.attach
            (
                 new FilteredListener<ShapeDeleteEvent>()
                 {
                    @Override
                    protected void handleTypedEvent( final ShapeDeleteEvent event )
                    {
                    	broadcast(event);
                    }
                 }
            );            
            shapePart.attach
            (
                 new FilteredListener<ShapeReorderEvent>()
                 {
                    @Override
                    protected void handleTypedEvent( final ShapeReorderEvent event )
                    {
                    	broadcast(event);
                    }
                 }
            );            
            
    	}
    	return shapePart;
    }
    
    private ShapePart createShapePart(ShapeFactoryCaseDef shapeFactoryCase, Element modelElement)
    {
    	ShapeDef shapeDef = shapeFactoryCase.getShape().content();
    	ShapePart shapePart = createShapePart(shapeDef, modelElement);
    	if (shapeFactoryCase.getSelectionPresentation() != null)
    	{
    		shapePart.setSelectionPresentation(shapeFactoryCase.getSelectionPresentation());
    	}
    	return shapePart;
    }
    
    private void handleModelPropertyChange(final PropertyEvent event)
    {
    	ElementList<?> newList = (ElementList<?>) event.property();
    	
    	List<ShapePart> children = getChildren();
		List<Element> oldList = new ArrayList<Element>(children.size());
		for (ShapePart shapePart : children)
		{
			oldList.add(shapePart.getLocalModelElement());
		}
    	
    	List<Element> deletedShapes = CollectionsUtil.removedBasedOnEntryIdentity(oldList, newList);
    	List<Element> newShapes = CollectionsUtil.removedBasedOnEntryIdentity(newList, oldList);
    	if (deletedShapes.isEmpty() && newShapes.isEmpty())
    	{
    		// List has been re-ordered
    		List<ShapePart> newChildren = new ArrayList<ShapePart>();
    		for (Element listEle : newList)
    		{
    			ShapePart shapePart = getShapePart(listEle);
    			newChildren.add(shapePart);
    		}
    		this.children.clear();
    		this.children.addAll(newChildren);
    		broadcast(new ShapeReorderEvent(this));
    	}
    	else
    	{
			for (Element deletedShape : deletedShapes)
			{
				ShapePart shapePart = getShapePart(deletedShape);
				if (shapePart != null)
				{
					shapePart.dispose();
					this.children.remove(shapePart);
					broadcast(new ShapeDeleteEvent(shapePart));
				}
			}    	    	
			for (Element newShape : newShapes)
			{
	        	ShapeFactoryCaseDef shapeFactoryCase = getShapeFactoryCase(newShape);
				
		    	ShapePart shapePart = createShapePart(shapeFactoryCase, newShape);
		    	this.children.add(shapePart);
		    	broadcast(new ShapeAddEvent(shapePart));
			}
    	}    	
    }
        
}
