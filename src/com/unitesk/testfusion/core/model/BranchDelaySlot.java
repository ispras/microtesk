/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchDelaySlot.java,v 1.2 2008/08/26 10:00:35 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;


/**
 * Class <code>BranchDelaySlot</code> implements logic that handles delay slot
 * of branch instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BranchDelaySlot implements ResetableObject
{
    private int cycle;
    
    /** Flag that indicates change of control flow. */
    protected boolean jump;
    
    /** Flag that indicates delay slot of branch instruction. */
    protected boolean slot;
    
    /** Flag that indicates nullification of instruction. */
    protected boolean like;
    
    /** Default constructor. */
    public BranchDelaySlot()
    {
        reset();
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to object.
     */
    protected BranchDelaySlot(BranchDelaySlot r)
    {
        cycle = r.cycle;
        jump = r.jump;
        slot = r.slot;
        like = r.like;
    }
    
    /**
     * Processes the instruction.
     * 
     * @param  <code>instruction</code> the instruction to be processed.
     * 
     * @return <code>true</code> if the instruction to be executed;
     *         <code>false</code> otherwise.
     */
    public boolean process(Instruction instruction)
    {
        boolean execute = !(slot && !jump && like || !slot && jump);
        
        if(!jump)
        {
            if(!slot)
            {
                if(slot = (instruction.isBranchInstruction() && instruction.hasDelaySlot()))
                {
                    if(!(jump = !instruction.isUnsatisfiedCondition()))
                        { like = instruction.doesNullifyDelaySlot(); }
                }
            }
            else
                { slot = like = false; }
        }
        else
            { slot = like = false; }
     
        // For the debug purpose.
        cycle++;
        
        return execute;
    }
    
    /**
     * Returns a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        if(jump)
            { return "Control flow has changed"; }
        
        return "Branch delay slot";
    }
    
    /** Resets the state of the object. */
    public void reset()
    {
        cycle = 0;
        
        jump = false;
        slot = false;
        like = false;
    }
    
    /**
     * Returns a copy of the object.
     * 
     * @return a copy of the object.
     */
    public BranchDelaySlot clone()
    {
        return new BranchDelaySlot(this);
    }
}
