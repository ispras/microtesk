/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchStructure.java,v 1.4 2009/08/13 15:54:23 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

import java.util.ArrayList;

/**
 * Internal representation of branch structure.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BranchStructure extends ArrayList<BranchEntry>
{
    public static final long serialVersionUID = 0;
        
    /**
     * Constructor.
     * 
     * @param <code>size</code> size of branch structure.
     */
    public BranchStructure(int size)
    {
        for(int i = 0; i < size; i++)
            { add(new BranchEntry()); }
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the branch structure object.
     */
    protected BranchStructure(BranchStructure r)
    {
        for(BranchEntry item : r)
            { add(item.clone()); }
    }
    
    /**
     * Returns string representation of branch structure.
     * 
     * @return string representation of branch structure.
     */
    public String toString()
    {
        int i, size;
        StringBuffer buffer = new StringBuffer();
        
        size = size();
        for(i = 0; i < size; i++)
        {
            BranchEntry entry = get(i);
            
            buffer.append(i + ": ");
            buffer.append(entry);
            buffer.append("\n");
        }
        
        return buffer.toString();
    }
    
    /**
     * Returns a copy of the branch structure.
     * 
     * @return a copy of the branch structure.
     */
    public BranchStructure clone()
    {
        return new BranchStructure(this);
    }
}
