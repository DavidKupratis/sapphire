/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.graphiti.providers;

import java.util.HashMap;

import org.eclipse.graphiti.features.impl.IIndependenceSolver;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.swt.graphiti.DiagramRenderingContext;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SapphireDiagramSolver implements IIndependenceSolver 
{
    private HashMap<String, SapphirePart> keyToBOMap;
    private HashMap<SapphirePart, String> bOToKeyMap;
    private int counter = 0;
    private HashMap<ISapphirePart, DiagramRenderingContext> partContextMap;

    public SapphireDiagramSolver()
    {
        this.keyToBOMap = new HashMap<String, SapphirePart>();
        this.bOToKeyMap = new HashMap<SapphirePart, String>();
        this.partContextMap = new HashMap<ISapphirePart, DiagramRenderingContext>();
    }
    
    public String getKeyForBusinessObject(Object bo) 
    {
        if (bo instanceof SapphirePart)            
        {
            SapphirePart part = (SapphirePart)bo;
            if (this.bOToKeyMap.containsKey(bo))
            {
                return this.bOToKeyMap.get(bo);
            }
            String key = Integer.toString(this.counter++);
            this.keyToBOMap.put(key, part);
            this.bOToKeyMap.put(part, key);
            
            return key;
        }
        return null;
    }

    public Object getBusinessObjectForKey(String key) 
    {
        if (key == null)
            return null;
        return this.keyToBOMap.get(key);
    }

    public boolean removeBO(Object bo)
    {
        if (this.partContextMap.containsKey(bo))
        {
            this.partContextMap.remove(bo);
        }
        
        if (this.bOToKeyMap.containsKey(bo))
        {
            String key = this.bOToKeyMap.get(bo);
            this.bOToKeyMap.remove(bo);
            this.keyToBOMap.remove(key);
            return true;
        }
        
        return false;
    }
    
    public void addRendingContext(ISapphirePart part, DiagramRenderingContext ctx)
    {
        this.partContextMap.put(part, ctx);
    }
    
    public DiagramRenderingContext getRenderingContext(ISapphirePart part)
    {
        return this.partContextMap.get(part);
    }
}
