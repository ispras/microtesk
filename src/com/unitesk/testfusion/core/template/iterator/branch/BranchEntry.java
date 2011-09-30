/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchEntry.java,v 1.8 2009/04/16 07:49:26 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

/**
 * Represents node of internal representation of branch structure.
 *  
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BranchEntry
{
    /** Entry corresponds to branch instruction. */
    public static final int BRANCH = 0;
    
    /** Entry corresponds to delay slot. */
    public static final int SLOT   = 1;

    /** Entry corresponds to basic block. */
    public static final int BLOCK  = 2;
    
    /**
     * Entry type: <code>BRANCH</code>, <code>SLOT</code>, or
     * <code>BLOCK</code>.
     */
    protected int entryType;

    /** Conditional or unconditional branch. */
    protected boolean conditionalBranch;
    
    /** Equivalence class of branch, delay slot, or basic block. */
    protected int equivalenceClass;
    
    /** Branch label (index of target instruction in structure). */
    protected int branchLabel;
    
    /** Trace of conditional branch execution. */
    protected BranchTrace branchTrace = new BranchTrace();
    
    /**
     * Block coverage (set of blocks that cover all segments of the branch
     * instruction).
     */
    protected BranchTraceSegment blockCoverage;

    /**
     * Slot coverage (set of slots that are included in all segments of the
     * branch instruction).
     */
    protected BranchTraceSegment slotCoverage;
    
    /**
     * Constructor.
     * 
     * @param <code>entryType</code> the entry type.
     * 
     * @param <code>equivalenceClass</code> the equivalence class.
     * 
     * @param <code>branchLabel</code> the branch label (index of target
     *        instruction in structure).
     */
    public BranchEntry(int entryType, boolean conditionalBranch, int equivalenceClass, int branchLabel)
    {
        this.entryType = entryType;
        this.conditionalBranch = conditionalBranch;
        this.equivalenceClass = equivalenceClass;
        this.branchLabel = branchLabel;
    }
    
    /** Default constructor. */
    public BranchEntry()
    {
        this(BLOCK, false, 0, 0);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to branch entry object.
     */
    protected BranchEntry(BranchEntry r)
    {
        entryType = r.entryType;
        conditionalBranch = r.conditionalBranch;
        equivalenceClass = r.equivalenceClass;
        branchLabel = r.branchLabel;
        branchTrace = r.branchTrace.clone();
    }
    
    /**
     * Checks if the entry is branch.
     * 
     * @return <code>true</code> if the entry is branch; <code>false</code>
     *         otherwise.
     */
    public boolean isBranch()
    {
        return entryType == BRANCH;
    }
    
    /**
     * Checks if the entry is delay slot.
     * 
     * @return <code>true</code> if the entry is delay slot;
     *         <code>false</code> otherwise.
     */
    public boolean isSlot()
    {
        return entryType == SLOT;
    }

    /**
     * Checks if the entry is basic block.
     * 
     * @return <code>true</code> if the entry is basic block;
     *         <code>false</code> otherwise.
     */
    public boolean isBlock()
    {
        return entryType == BLOCK;
    }
    
    /**
     * Returns type of the entry.
     * 
     * @return type of the entry.
     */
    public int getType()
    {
        return entryType;
    }
    
    /**
     * Sets type of the entry.
     * 
     * @param <code>entryType</code> type of the entry.
     */
    public void setType(int entryType)
    {
        this.entryType = entryType;
    }
    
    /**
     * Checks if the branch is conditional.
     * 
     * @return <code>true</code> if the branch is conditional;
     *         <code>false</code> otherwise.
     */
    public boolean isConditionalBranch()
    {
        return conditionalBranch;
    }

    /**
     * Sets type of branch: conditional or unconditional.
     * 
     * @param <code>conditionalBranch<code> type of the branch.
     */
    public void setConditionalBranch(boolean conditionalBranch)
    {
        this.conditionalBranch = conditionalBranch;
    }
    
    /**
     * Returns equivalence class of the entry.
     * 
     * @return equivalence class of the entry.
     */
    public int getEquivalenceClass()
    {
        return equivalenceClass;
    }
    
    /**
     * Sets equivalence class of the entry.
     * 
     * @param <code>equivalenceClass</code> equivalence class of the entry.
     */
    public void setEquivalenceClass(int equivalenceClass)
    {
        this.equivalenceClass = equivalenceClass;
    }
    
    /**
     * Returns branch label (index of target instruction in structure).
     * 
     * @return branch label.
     */
    public int getBranchLabel()
    {
        return branchLabel;
    }

    /**
     * Sets branch label (index of target instruction in structure).
     * 
     * @param <code>branchLabel</code> branch label.
     */
    public void setBranchLabel(int branchLabel)
    {
        this.branchLabel = branchLabel;
    }
    
    /**
     * Returns execution trace of the branch instruction.
     * 
     * @return execution trace of the branch instruction;
     */
    public BranchTrace getBranchTrace()
    {
        return branchTrace;
    }

    /**
     * Returns slot coverage.
     * 
     * @return slot coverage.
     */
    public BranchTraceSegment getSlotCoverage()
    {
        return slotCoverage;
    }
    
    /**
     * Sets slot coverage.
     * 
     * @param <code>slotCoverage</code> slot coverage.
     */
    public void setSlotCoverage(BranchTraceSegment slotCoverage)
    {
        this.slotCoverage = slotCoverage;
    }
    
    /**
     * Returns block coverage.
     * 
     * @return block coverage.
     */
    public BranchTraceSegment getBlockCoverage()
    {
        return blockCoverage;
    }
    
    /**
     * Sets block coverage.
     * 
     * @param <code>blockCoverage</code> block coverage.
     */
    public void setBlockCoverage(BranchTraceSegment blockCoverage)
    {
        this.blockCoverage = blockCoverage;
    }
    
    /**
     * Checks if block coverage is not <code>null</code>.
     * 
     * @return <code>true</code> if block coverage is not null;
     *         <code>false</code> otherwise.
     */
    public boolean canInsertStepIntoBlock()
    {
        return blockCoverage != null;
    }
    
    /**
     * Checks if slot coverage is not <code>null</code>.
     * 
     * @return <code>true</code> if slot coverage is not null;
     *         <code>false</code> otherwise.
     */
    public boolean canInsertStepIntoSlot()
    {
        return slotCoverage != null;
    }
    
    /**
     * Returns string representation of branch entry.
     * 
     * @return string representation of branch entry.
     */
    public String toString()
    {
        if(isBranch())
            { return "branch Target: " + getBranchLabel() + ", Trace: " + getBranchTrace(); }
        if(isSlot())
            { return "slot"; }
        
        return "block";
    }
    
    /**
     * Returns a copy of the branch entry.
     * 
     * @return a copy of the branch entry.
     */
    public BranchEntry clone()
    {
        return new BranchEntry(this);
    }
}
