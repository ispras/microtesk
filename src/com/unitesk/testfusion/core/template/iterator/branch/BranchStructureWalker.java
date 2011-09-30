/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchStructureWalker.java,v 1.3 2009/04/16 07:49:26 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

/**
 * Branch structure walker.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BranchStructureWalker
{
    /** Flag that shows if walker is active or not. */
    protected boolean active = true;

    /** Branch structure to be traversed. */
    protected BranchStructure branchStructure;
    
    /** Branch entry visitor. */
    protected BranchEntryVisitor visitor;
    
    private int[] count;
    
    /**
     * Constructor.
     * 
     * @param <code>branchStructure</code> the branch structure to be traversed.
     * 
     * @param <code>visitor</code> the branch entry visitor.
     */
    public BranchStructureWalker(BranchStructure branchStructure, BranchEntryVisitor visitor)
    {
        count = new int[branchStructure.size()];
        
        this.branchStructure = branchStructure;
        this.visitor = visitor;
        
        visitor.setBranchStructureWalker(this);
    }
    
    /**
     * Starts traversal.
     * 
     * @param <code>index</code> the index of the initial branch entry.
     */
    public void start(int index)
    {
        int size = branchStructure.size();
        
        active = true;
        
        for(int i = 0; i < size; i++)
            { count[i] = 0; }
            
        while(active && index < size)
        {
            BranchEntry entry = branchStructure.get(index);
            BranchTrace trace = entry.getBranchTrace();
            
            if(entry.isBranch())
            {
                BranchExecution execution = trace.get(count[index]++);;

                visitor.onBranch(index, entry, execution);
                
                if(index < size - 1)
                {
                    BranchEntry next = branchStructure.get(index + 1);
                    
                    if(next.isSlot())
                        { visitor.onSlot(++index, entry); }
                }
                
                index = execution.condition() ? entry.getBranchLabel() : index + 1;
            }
            else if(entry.isSlot())
                { visitor.onSlot(index++, entry); }
            else
                { visitor.onBlock(index++, entry); }
        }
    }

    /** Starts traversal. */
    public void start()
    {
        start(0);
    }
    
    /** Stops traversal. */
    public void stop()
    {
        active = false;
    }
}
