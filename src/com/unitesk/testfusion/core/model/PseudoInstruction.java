/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: PseudoInstruction.java,v 1.6 2009/03/26 17:23:06 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

/**
 * Class that represents a pseudo-instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class PseudoInstruction extends Instruction
{
    /** Text of the preudo-instruction. */
    protected String text;

    /**
     * Constructor.
     * 
     * @param <code>text</code> the text of the preudo-instruction.
     */
    public PseudoInstruction(String text)
    {
        super("", "");
        
        this.text = text;
    }
    
    /** Default constructor. */
    public PseudoInstruction()
    {
        this("");
    }

    /**
     * Returns an instance of the pseudo-instruction.
     * 
     * @return an instance of the pseudo-instruction.
     */
    public Instruction createInstruction()
    {
        return new PseudoInstruction(text);
    }
    
    /**
     * Returns the text of the pseudo-instruction.
     * 
     * @return the text of the pseudo-instruction.
     */
    public String getText()
    {
        return text;
    }
    
    /**
     * Sets the text of the pseudo-instruction.
     * 
     * @param <code>text</code> the text of the pseudo-instruction.
     */
    public void setText(String text)
    {
        this.text = text;
    }
    
    /**
     * Calculates values of the output operands. The method does nothing.
     * 
     * @param <code>processor</code> the processor object.
     */
    public void calculate(Processor processor)
    {
        super.calculate(processor);
        
        // Do nothing
    }

    /**
     * Executes the instruction. The method does nothing.
     * 
     * @param <code>processor</code> the processor object.
     */
    public void execute(Processor processor)
    {
        super.execute(processor);
        
        // Do nothing
    }

    /**
     * Checks if the instruction can change control flow of the execution.
     * Always returns <code>false</code>.
     * 
     * @return <code>false</code>.
     */
    public boolean isBranchInstruction()
    {
        return false;
    }
    
    /**
     * Checks if the branch instruction is conditional or not.
     * 
     * @return <code>true</code> if the instruction is conditional branch;
     *         <code>false</code> otherwise.
     */
    public boolean isConditionalBranch()
    {
        return false;
    }
    
    /**
     * Checks if the branch instruction has delay slot. Always returns
     * <code>false</code>
     * 
     * @return <code>false</code>.
     */
    public boolean hasDelaySlot()
    {
        return false;
    }
    
    /**
     * Checks if the branch instruction nullifies the delay slot if a
     * conditition is unsatisfied. Always returns <code>false</code>.
     * 
     * @return <code>false</code>.
     */
    public boolean doesNullifyDelaySlot()
    {
        return false;
    }
    
    /**
     * Returns the latency time of the instruction. Always return zero.
     * 
     * @return <code>0</code>
     */
    public int getLatency()
    {
        return 0;
    }
    
    /**
     * Returns the repeat time of the instruction.  Always return zero.
     * 
     * @return <code>0</code>
     */
    public int getRepeat()
    {
        return 0;
    }
    
    /**
     * Returns the execution time of the instruction. Always return zero.
     * 
     * @return <code>0</code>
     */
    public int getExecutionTime()
    {
        return 0;
    }
       
    /**
     * Returns a string representation of the pseudo-instruction.
     * 
     * @return a string representation of the pseudo-instruction. 
     */
    public String toString()
    {
        return text;
    }
}
