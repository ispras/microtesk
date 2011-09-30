/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchTraceSituation.java,v 1.11 2009/07/09 14:48:09 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.core.situation.EmptySituation;

/**
 * Branch trace situation.
 *  
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class BranchTraceSituation extends EmptySituation
{
    /** Order number of branch instruction. */
    protected int branchNumber;
    
    /** Index of branch instruction in test template. */
    protected int branchIndex;
    
    /** Index of target instruction in test template. */
    protected int branchLabel;
    
    /** Branch execution trace. */
    protected BranchTrace branchTrace;
    
    /** Block coverage. */
    protected BranchTraceSegment blockCoverage;

    /** Slot coverage. */
    protected BranchTraceSegment slotCoverage;    
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of situation.
     * 
     * @param <code>text</code> the description of situation.
     * 
     * @param <code>branchLabel</code> the branch label (index of target
     *        instruction in test template). 
     *        
     * @param <code>branchTrace</code> the branch execution trace.
     * 
     * @param <code>blockCoverage</code> the block coverage.
     * 
     * @param <code>slotCoverage</code> the slot coverage.
     */
    public BranchTraceSituation(String name, String text, int branchNumber, int branchIndex, int branchLabel, BranchTrace branchTrace, BranchTraceSegment blockCoverage, BranchTraceSegment slotCoverage)
    {
        super(name, text);
        
        this.branchNumber  = branchNumber;
        this.branchIndex   = branchIndex;
        this.branchLabel   = branchLabel;
        this.branchTrace   = branchTrace;
        this.blockCoverage = blockCoverage;
        this.slotCoverage  = slotCoverage;
    }

    /**
     * Constructor.
     * 
     * @param <code>branchLabel</code> the branch label (index of target
     *        instruction in test template). 
     *        
     * @param <code>branchTrace</code> the branch execution trace.
     * 
     * @param <code>blockCoverage</code> the block coverage.
     * 
     * @param <code>slotCoverage</code> the slot coverage.
     */
    public BranchTraceSituation(int branchNumber, int branchIndex, int branchLabel, BranchTrace branchTrace, BranchTraceSegment blockCoverage, BranchTraceSegment slotCoverage)
    {
        this.branchNumber  = branchNumber;
        this.branchIndex   = branchIndex;
        this.branchLabel   = branchLabel;
        this.branchTrace   = branchTrace;
        this.blockCoverage = blockCoverage;
        this.slotCoverage  = slotCoverage;
    }
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of situation.
     * 
     * @param <code>text</code> the description of situation.
     */
    public BranchTraceSituation(String name, String text)
    {
        this(name, text, 0, 0, 0, new BranchTrace(), null, null);
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of situation.
     */
    public BranchTraceSituation(String name)
    {
        this(name, "");
    }
    
    /** Default constructor. */
    public BranchTraceSituation()
    {
        this(0, 0, 0, new BranchTrace(), null, null);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to branch trace situation object.
     */
    protected BranchTraceSituation(BranchTraceSituation r)
    {
        super(r);

        branchNumber  = r.branchNumber;
        branchIndex   = r.branchIndex;
        branchLabel   = r.branchLabel;
        branchTrace   = r.branchTrace.clone();
        blockCoverage = r.blockCoverage != null ? r.blockCoverage.clone() : null;
        slotCoverage  = r.slotCoverage  != null ? r.slotCoverage.clone()  : null;
    }

    /**
     * Returns branch number.
     * 
     * @return branch number.
     */
    public int getBranchNumber()
    {
        return branchNumber;
    }
    
    /**
     * Sets branch number.
     * 
     * @param <code>branchNumber</code> branch number.
     */
    public void setBranchNumber(int branchNumber)
    {
        this.branchNumber = branchNumber;
    }
    
    /**
     * Returns branch index.
     * 
     * @return branch index.
     */
    public int getBranchIndex()
    {
        return branchIndex;
    }
    
    /**
     * Sets branch index.
     * 
     * @param <code>branchIndex</code> branch index.
     */
    public void setBranchIndex(int branchIndex)
    {
        this.branchIndex = branchIndex;
    }
    
    /**
     * Returns branch label (index of target instruction in test template).
     * 
     * @return branch label.
     */
    public int getBranchLabel()
    {
        return branchLabel;
    }
    
    /**
     * Sets branch label (index of target instruction in test template).
     * 
     * @param <code>branchLabel</code> branch label
     */
    public void setBranchLabel(int branchLabel)
    {
        this.branchLabel = branchLabel;
    }
    
    /**
     * Checks if the branch is forward or not.
     * 
     * @return <code>true</code> if branch is forward;
     *         <code>false</code> otherwise.
     */
    public boolean isForwardBranch()
    {
        return getBranchIndex() < getBranchLabel();
    }
    
    /**
     * Checks if the branch is backward or not.
     * 
     * @return <code>true</code> of branch is backward;
     *         <code>false</code> otherwise.
     */
    public boolean isBackwardBranch()
    {
        return getBranchIndex() >= getBranchLabel();
    }
    
    /**
     * Returns branch execution trace.
     * 
     * @return branch execution trace.
     */
    public BranchTrace getBranchTrace()
    {
        return branchTrace;
    }
    
    /**
     * Sets branch execution trace.
     * 
     * @param <code>branchTrace</code> branch execution trace.
     */
    public void setBranchTrace(BranchTrace branchTrace)
    {
        this.branchTrace = branchTrace;
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
     * Constructs operands values that satisfy branch condition.
     */
    public abstract void satisfyCondition();
    
    /**
     * Constructs operands values that violate branch condition.
     */
    public abstract void violateCondition();
    
    /**
     * Construct operands values that satisfy or violate branch condition
     * depending on the parameter value.
     * 
     * @param <code>condition</code> truth value of the condition.
     */
    public void satisfyCondition(boolean condition)
    {
        if(condition)
            { satisfyCondition(); }
        else
            { violateCondition(); }
    }
    
    /**
     * Performs some initialization for method <code>step()</code>. This method
     * is invoked before calling method <code>step()</code>. It is always
     * invoked by generator regardless of calling method <code>step()</code>.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     */
    public abstract void init(Processor processor, GeneratorContext context);
    
    /**
     * Returns the step program for the branch instruction. This method is
     * invoked before calling method <code>construct()</code>.
     * 
     * @return the step program for branch instruction.
     */
    public abstract Program step();
    
    /**
     * Constructs the branch trace situation.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     */
    public abstract boolean construct(Processor processor, GeneratorContext context);

    /**
     * Returns the preparation program for branch instruction. This method is
     * invoked after execution of method <code>construct()</code>.
     * 
     * @param  <code>processor</code> the processor.
     * 
     * @param  <code>context</code> the context of generation.
     * 
     * @param  <code>layer</code> the preparation layer.
     * 
     * @return the preparation program for branch instruction.
     */
    public abstract Program prepare(Processor processor, GeneratorContext context, int layer);
        
    /**
     * Returns a string representation of the situation.
     * 
     * @return a string representation of the situation.
     */
    public String toString()
    {
        return "Target: " + branchLabel + ", Trace: " + branchTrace +
            (blockCoverage != null ? ", Blocks: " + blockCoverage : "") +
            (slotCoverage != null ? ", Slots: " + slotCoverage : "");
    }
}
