/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchExecution.java,v 1.7 2009/03/31 12:36:16 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

import com.unitesk.testfusion.core.iterator.BooleanIterator;

/**
 * Represents single execution of a branch instruction.
 *  
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BranchExecution extends BooleanIterator
{
    /**
     * Block coverage count (number of coverage's blocks executions followed by
     * branch execution.
     */
    protected int blockCoverageCount;
    
    /**
     * Slot coverage count (number of slot executions followed by branch
     * execution).
     */
    protected int slotCoverageCount;
    
    /**
     * Sequence of basic blocks between two executions of branch
     * instruction.
     */
    protected BranchTraceSegment blocks = new BranchTraceSegment();

    /**
     * Sequence of delay slots between two executions of branch
     * instruction.
     */
    protected BranchTraceSegment slots = new BranchTraceSegment();
    
    /**
     * Constructor.
     * 
     * @param <code>conditionalBranch</code> the flag that indicates if the
     *        branch is conditional or not.
     */
    public BranchExecution(boolean conditionalBranch)
    {
        super();
        
        init(); setValue(!conditionalBranch);
    }

    /** Default constructor. */
    public BranchExecution()
    {
        this(true);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to a branch execution object. 
     */
    protected BranchExecution(BranchExecution r)
    {
        super(r);
        
        blockCoverageCount = r.blockCoverageCount;
        slotCoverageCount  = r.slotCoverageCount;
        
        blocks = r.blocks.clone();
        slots  = r.slots.clone();
    }
    
    /**
     * Returns block or slot coverage count.
     * 
     * @param <code>isBlock</code> flag that selects which coverage should be
     *        used.
     * 
     * @return block or slot coverage depending on <code>isBlock</code> value.
     */
    public int getCoverageCount(boolean isBlock)
    {
        if(isBlock)
            { return blockCoverageCount; }
        
        return slotCoverageCount;
    }
    
    /**
     * Returns block coverage count.
     * 
     * @return block coverage count.
     */
    public int getBlockCoverageCount()
    {
        return blockCoverageCount;
    }
    
    /**
     * Sets block coverage count.
     * 
     * @param <code>blockCoverageCount</code> block coverage count.
     */
    public void setBlockCoverageCount(int blockCoverageCount)
    {
        this.blockCoverageCount = blockCoverageCount;
    }

    /**
     * Returns slot coverage count.
     * 
     * @return slot coverage count.
     */
    public int getSlotCoverageCount()
    {
        return slotCoverageCount;
    }
    
    /**
     * Sets block coverage count.
     * 
     * @param <code>blockCoverageCount</code> block coverage count.
     */
    public void setSlotCoverageCount(int slotCoverageCount)
    {
        this.slotCoverageCount = slotCoverageCount;
    }
    
    /**
     * Returns the trace segment consisting of basic blocks.
     * 
     * @return the trace segment consisting of basic blocks.
     */
    public BranchTraceSegment getBlockSegment()
    {
        return blocks;
    }

    /**
     * Returns the trace segment consisting of delay slots.
     * 
     * @return the trace segment consisting of delay slots.
     */
    public BranchTraceSegment getSlotSegment()
    {
        return slots;
    }
    
    /** Clears the trace segment. */
    public void clear()
    {
        blockCoverageCount = 0;
        
        blocks.clear();
        slots.clear();
    }
    
    /** Initializes iterator. */
    public void init()
    {
        super.init();
        
        clear();
    }

    /**
     * Returns the value of branch condition.
     * 
     * @return the value of branch condition.
     */
    public boolean condition()
    {
        return booleanValue();
    }
    
    /** Makes iteration. */
    public void next()
    {
        super.next();
        
        clear();
    }
    
    /**
     * Returns a string representation of the branch execution object.
     * 
     * @return a string representation of the branch execution object.
     */
    public String toString()
    {
        return condition() + (blocks.isEmpty() ? "" : " " + blocks.toString());
    }

    /**
     * Returns a copy of the branch execution object.
     * 
     * @return a copy of the branch execution object.
     */
    public BranchExecution clone()
    {
        return new BranchExecution(this);
    }
}
