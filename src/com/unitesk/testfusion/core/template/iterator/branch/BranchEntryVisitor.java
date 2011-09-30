/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchEntryVisitor.java,v 1.2 2009/03/19 11:37:54 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

/**
 * Branch entry visitor.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class BranchEntryVisitor
{
    /** Branch structure walker. */
    protected BranchStructureWalker walker;
    
    /**
     * Returns branch structure walker.
     * 
     * @return branch structure walker.
     */
    public BranchStructureWalker getBranchStructureWalker()
    {
        return walker;
    }
    
    /**
     * Sets branch structure walker.
     * 
     * @param <code>walker</code> branch structure walker.
     */
    public void setBranchStructureWalker(BranchStructureWalker walker)
    {
        this.walker = walker;
    }

    /** Stops traversal. */
    public void stop()
    {
        walker.stop();
    }

    /**
     * Process the branch node.
     * 
     * @param <code>index</code> the index of the branch node.
     * 
     * @param <code>entry</code> the branch node.
     * 
     * @param <code>execution</code> the execution of the branch node.
     */
    public abstract void onBranch(int index, BranchEntry entry, BranchExecution execution);
    
    /**
     * Process the delay slot node.
     * 
     * @param <code>index</code> the index of the delay slot node.
     *
     * @param <code>entry</code> the delay slot node
     */
    public abstract void onSlot(int index, BranchEntry entry);
    
    /**
     * Process the basic block node.
     * 
     * @param <code>index</code> the index of the basic block node.
     * 
     * @param <code>entry</code> the basic block node.
     */
    public abstract void onBlock(int index, BranchEntry entry);
}
