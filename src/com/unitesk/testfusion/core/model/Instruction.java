/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Instruction.java,v 1.18 2009/08/13 15:54:15 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

import java.util.ArrayList;

import com.unitesk.testfusion.core.engine.TestProgramTemplate;
import com.unitesk.testfusion.core.exception.*;
import com.unitesk.testfusion.core.model.memory.MemoryObject;
import com.unitesk.testfusion.core.situation.Situation;

/**
 * Instruction of microprocessor.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Instruction extends MemoryObject implements Executable
{
    /** Name of the instruction. */
    protected String name;
    
    /** Subsystem (group) of the instruction. */
    protected String subsystem;
    
    /** Equivalence class of the instruction. */
    protected String equivalenceClass;
    
    /** Single-line comment attached to the instruction. */
    protected String comment;

    /** Position of the instruction in a program. */
    protected int position;
    
    /** Exception caused by the instruction. */
    protected ProcessorException exception;

    /** Operands of the instruction. */
	protected ArrayList<Operand> operands = new ArrayList<Operand>();
    
    /** List of exceptions which this instruction can cause. */
    protected ArrayList<ProcessorException> exceptions = new ArrayList<ProcessorException>();

    /** Test situation attached to the instruction. */
    protected Situation situation; 

    /**
     * Constructor.
     * 
     * @param <code>name</code> the instruction name.
     * 
     * @param <code>subsystem</code> the instruction subsystem.
     */
    public Instruction(String name, String subsystem)
    {
        this.name = name;
        this.equivalenceClass = name;
        this.subsystem = subsystem;
    
        // If instruction is not executed, then output values are unpredictable
        setException(new UnpredictableBehavior());
    }
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the instruction name.
     */
    public Instruction(String name)
    {
        this(name, name);
    }

    /**
     * Creates an instance of the instruction.
     * 
     * @return an instance of the instruction.
     */
    abstract public Instruction createInstruction();
    
    /**
     * Returns the name of the instruction.
     * 
     * @return the name of the instruction.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns the subsystem of the instruction.
     * 
     * @return the subsystem of the instruction.
     */
    public String getSubsystem()
    {
        return subsystem;
    }
    
    /**
     * Returns the equivalence class of the instruction.
     * 
     * @return the equivalence class of the instruction.
     */
    public String getEquivalenceClass()
    {
        return equivalenceClass;
    }
    
    /**
     * Sets the equivalence class of the instruction.
     * 
     * @param <code>equivalenceClass</code> the equivalence class.
     */
    public void setEquivalenceClass(String equivalenceClass)
    {
        this.equivalenceClass = equivalenceClass;
    }
    
    /**
     * Returns the single-line comment attached to the instruction.
     * 
     * @return the single-line comment attached to the instruction.
     */
    public String getComment()
    {
        return comment;
    }
    
    /**
     * Attaches the single-line comment to the instruction.
     * 
     * @param <code>comment</code> the single-line comment.
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }

    /**
     * Returns the position of the instruction in a program.
     * 
     * @return the position of the instruction in a program.
     */
    public int getPosition()
    {
        return position;
    }

    /**
     * Sets the position of the instruction in a program.
     * 
     * @param <code>position</code> the position of the instruction in a
     *        program.
     */
    public void setPosition(int position)
    {
        this.position = position;
    }

    /**
     * Inrements the position of the instruction.
     * 
     * @param <code>offset</code> the increment value.
     */
    public void incrementPosition(int offset)
    {
        position += offset;
    }

    /**
     * Decrements the position of the instruction.
     * 
     * @param <code>offset</code> the decrement value.
     */
    public void decrementPosition(int offset)
    {
        position -= offset;
    }
    
    /**
     * Returns the exception caused by the instruction execution.
     * 
     * @return the exception caused by the instruction execution.
     */
    public ProcessorException getException()
    {
        return exception;
    }
    
    /**
     * Checks if an exception was caused by the instruction execution.
     * 
     * @return <code>true</code> if an exception was caused by the instruction
     *         execution; <code>false</code> otherwise.
     */
    public boolean isException()
    {
        return exception != null;
    }
    
    /**
     * Checks if a pre-exception was caused by the instruction execution.
     * 
     * @return <code>true</code> if a pre-exception was caused by the
     *         instruction execution; <code>false</code> otherwise.
     */
    public boolean isPreException()
    {
        return exception != null && !exception.isPostException();
    }
    
    /**
     * Checks if a post-exception was caused by the instruction execution.
     * 
     * @return <code>true</code> if a post-exception was caused by the
     *         instruction execution; <code>false</code> otherwise.
     */
    public boolean isPostException()
    {
        return exception != null && exception.isPostException();
    }
    
    /**
     * Checks if a behavior of the instruction is unpredictable.
     * 
     * @return <code>true</code> if a behavior of the instruction is
     *         unpredictable; <code>false</code> otherwise.
     */
    public boolean isUnpredictable()
    {
        return exception != null && exception instanceof UnpredictableBehavior;
    }
    
    /**
     * Checks if a condition was unsafisfied in the conditional instruction.
     * 
     * @return <code>true</code> if a condition was unsafisfied in the
     *         conditional instruction; <code>false</code> otherwise.
     */
    public boolean isUnsatisfiedCondition()
    {
        return exception != null && exception instanceof UnsatisfiedCondition;
    }
    
    /**
     * Sets the exception.
     * 
     * @param <code>exception</code> the exception.
     */
    protected void setException(ProcessorException exception)
    {
        this.exception = exception;
    }
    
    /** Clears the exception. */
    public void clearException()
    {
        exception = null;
    }

    /**
     * Returns the test situation attached to the instruction.
     * 
     * @return the test situation attached to the instruction.
     */
    public Situation getSituation()
    {
        return situation;
    }

    /**
     * Attaches the test situation to the instruction.
     * 
     * @param <code>situation</code> the test situation for the instruction.
     */
    public void setSituation(Situation situation)
    {
        this.situation = situation;
        
        if(situation != null)
            { situation.setInstruction(this); }
    }
    
    /**
     * Returns the number of operands.
     * 
     * @return the number of operands.
     */
    public int countOperand()
    {
        return operands.size();
    }
    
    /**
     * Returns the <code>i</code>-th operand of the instruction.
     * 
     * @param  <code>i</code> the index of the operand.
     * 
     * @return the <code>i</code>-th operand of the instruction.
     */
    public Operand getOperand(int i)
    {
        return operands.get(i);
    }

    /**
     * Finds the operand of the instruction with the given name.
     * 
     * @param  <code>name</code> the name of the operand to be found.
     * 
     * @return the operand of the instruction with name <code>name</code> if it
     *         exists; <code>null</code> otherwise.
     */
    public Operand getOperand(String name)
    {
    	int i, size;
    	
    	size = operands.size();
    	for(i = 0; i < size; i++)
    	{
    		Operand operand = operands.get(i);
    		
    		if(name.equals(operand.getName()))
    		    { return operand; }
    	}
    	
    	return null;
    }
    
    /**
     * Adds the operand of the instruction.
     * 
     * @param <code>operand</code> the operand of the instruction.
     */
    public void registerOperand(Operand operand)
    {
        operand.setNumber(operands.size());
        operands.add(operand);
    }
    
    /**
     * Checks if the instruction can throw an exception.
     * 
     * @return <code>true</code> if the instruction can throw an exception;
     *         <code>false</code> otherwise.
     */
    public boolean canThrowException()
    {
        return !exceptions.isEmpty();
    }
    
    /**
     * Returns the number of exceptions.
     * 
     * @return the number of exceptions.
     */
    public int countException()
    {
        return exceptions.size();
    }
    
    /**
     * Returns the <code>i</code>-th exception of the instruction.
     * 
     * @param  <code>i</code> the index of the exception.
     * 
     * @return the <code>i</code>-th exception of the instruction.
     */
    public ProcessorException getException(int i)
    {
        return exceptions.get(i);
    }

    /**
     * Adds the exception of the instruction.
     * 
     * @param <code>exception</code> the exception of the instruction.
     */
    public void registerException(ProcessorException exception)
    {
        exceptions.add(exception);
    }
    
    /**
     * Calculates values of the output operands. This method should be
     * overloaded in descedant classes. Descendant method should call
     * <code>super.calculate(processor)</code> at the beginning. It should not
     * change the state of the <code>processor<code>.
     * 
     * @param <code>processor</code> the processor object.
     */
    public void calculate(Processor processor)
    {
        clearException();
    }

    /**
     * Executes the instruction. This method should be overloaded in descendant
     * classes. Descendant method should call <code>super.execute(processor)</code>
     * at the beginning. It should change the state of the
     * <code>processor<code>.
     * 
     * @param <code>processor</code> the processor object.
     */
    public void execute(Processor processor)
    {
        clearException();
    }

    /**
     * Checks if the instruction can change control flow of the execution.
     * 
     * @return <code>true</code> if the instruction can change control flow of
     *         the execution; <code>false</code> otherwise.
     */
    public abstract boolean isBranchInstruction();
    
    /**
     * Checks if the branch instruction is conditional or not.
     * 
     * @return <code>true</code> if the instruction is conditional branch;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isConditionalBranch();

    /**
     * Checks if the branch instruction has delay slot.
     * 
     * @return <code>true</code> if the branch instruction has delay slot;
     *         <code>false</code> otherwise.
     */
    public abstract boolean hasDelaySlot();
    
    /**
     * Checks if the branch instruction nullifies the delay slot if a
     * conditition is unsatisfied.
     * 
     * @return <code>true</code> if the branch instruction nullifies the delay
     *         slot if a condition is unsatisfied; <code>false</code>
     *         otherwise.
     */
    public abstract boolean doesNullifyDelaySlot();

    /**
     * Returns the latency time of the instruction. 
     * 
     * @return the latency time of the instructions.
     */
    public abstract int getLatency();
    
    /**
     * Returns the repeat time of the instruction.
     * 
     * @return the repeat time of the instruction.
     */
    public abstract int getRepeat();

    /**
     * Returns the name of the instruction.
     * 
     * @return the name of the instruction.
     */
    public String toString()
    {
        return getName();
    }
    
    /**
     * Return an assembler representation of the instruction including the
     * single-line comment attached to it.
     * 
     * @param  <code>template</code> the test program template.
     * 
     * @return an assembler representation of the instruction.
     */
    public String text(TestProgramTemplate template)
    {
    	StringBuffer rem = new StringBuffer();
    	
    	if(situation != null)
        {
            if(!situation.isConstructed())
                { rem.append("Not constructed: "); }
            
            rem.append(situation);
        }
    	
    	if(comment != null)
    	{
    		if(rem.length() != 0)
                { rem.append(". "); }
            
            rem.append(comment);
    	}
    	
        return toString() + (rem.length() != 0 ? " " + template.singleLineComment() + " " + rem : "");
    }
    
    /**
     * Returns a description of the instruction.
     * 
     * @return a description of the instruction. 
     */
    public String getDescription()
    {
        int i, j, size;
        
        StringBuffer res = new StringBuffer();
        
        size = countOperand();
        for(i = j = 0; i < size; i++)
        {
            Operand operand = getOperand(i);

            if(operand.isInput())
            {
                if(j != 0)
                    { res.append(", "); }
                
                res.append(operand.getDescription());

                j = -1;
            }
        }
        
        return res.toString();
    }
    
    /**
     * Compares two instructions.
     * 
     * @param <code>o</other> the instruction to be compared with this one.
     */
    public boolean equals(Object o)
    {
    	if(!(o instanceof Instruction))
    		{ return false; }
    	
    	Instruction r = (Instruction)o;
    	
    	return name.equals(r.name);
    }
    
    /**
     * Returns a hash code value for the object.
     */
    public int hashCode()
    {
        return name.hashCode();
    }

    /**
     * Returns a copy of the instruction.
     * 
     * @return a copy of the instruction.
     */
    public Instruction clone()
    {
        int i, j, size1, size2;
        
        Instruction instruction = createInstruction();

        // Clone list of exceptions.
        instruction.exceptions = exceptions;
        
        // Clone operands.
        size1 = instruction.countOperand();
        for(i = 0; i < size1; i++)
        {
            Operand srcOperand = getOperand(i);
            Operand dstOperand = instruction.getOperand(i);
            
            dstOperand.setRegister(srcOperand.getRegister());
            
            size2 = srcOperand.isBlock() ? srcOperand.getBlockSize() : 1;
            for(j = 0; j < size2; j++)
                { dstOperand.setLongValue(j, srcOperand.getLongValue(j)); }
            
            dstOperand.setDescriptor(srcOperand.getDescriptor());
        }
        
        // Clone test situation.
        if(situation != null)
            { instruction.setSituation(situation.clone()); }
        
        // Clone comment.
        instruction.comment = comment;

        // Do not copy address and exception of the instruction!
        
        return instruction; 
    }
}
