/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Situation.java,v 1.9 2009/11/12 12:53:29 kamkin Exp $
 */

package com.unitesk.testfusion.core.situation;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.iterator.Iterator;
import com.unitesk.testfusion.core.model.ContentType;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Operand;
import com.unitesk.testfusion.core.model.OperandType;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.Program;

/**
 * The abstract class that represents a test situation for an instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Situation implements Iterator
{
    /** The constant indicates that a situation is constructor-based. */
    public final static int CONSTRUCTOR = 0x0;
    
    /** The constant indicates that a situation is constraint-based. */
    public final static int CONSTRAINT  = 0x1;
    
    /** Kind of the test situation. */
    protected int kind;
    
    /** Test situation name. */
    protected String name;
    
    /** Textual description of the test situation. */
    protected String text;
    
    /** Reference to instruction. */
    protected Instruction instruction;
    
    /** Construction status. */
    protected boolean isConstructed;
    
    /**
     * Constructor.
     * 
     * @param <code>kind</code> the kind of the test situation.
     * @param <code>name</code> the name of the test situation.
     * @param <code>text</code> the textual description of the test situation.
     */
    public Situation(int kind, String name, String text)
    {
        this.kind = kind;
        this.name = name;
        this.text = text;
        this.isConstructed = false;
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the test situation.
     * @param <code>text</code> the textual description of the test situation.
     */
    public Situation(String name, String text)
    {
        this(CONSTRUCTOR, name, text);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the test situation.
     */
    public Situation(String name)
    {
        this(name, "");      
    }
    
    /** Default constructor. */
    public Situation()
    {
        this("");
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to test situation object.
     */
    public Situation(Situation r)
    {
        kind = r.kind;
        name = r.name;
        text = r.text;
        instruction = r.instruction;
        isConstructed = r.isConstructed;
    }
    
    /**
     * Checks whether the situation is constructor-based or not.
     *  
     * @return <code>true</code> if the situation is constructor-based;
     *         <code>false</code> otherwise.
     */
    public boolean isConstructor()
    {
        return kind == CONSTRUCTOR;
    }

    /**
     * Checks whether the situation is constraint-based or not.
     *  
     * @return <code>true</code> if the situation is constraint-based;
     *         <code>false</code> otherwise.
     */
    public boolean isConstraint()
    {
        return kind == CONSTRUCTOR;
    }
    
    /**
     * Returns the test situation name.
     * 
     * @return the test situation name.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns the textual description of the test situation.
     * 
     * @return the textual description of the test situation.
     */
    public String getText()
    {
        return text;
    }
    
    /**
     * Returns the reference to the instruction under test.
     * 
     * @return the reference to the instruction under test.
     */
    public Instruction getInstruction()
    {
        return instruction;
    }
    
    /**
     * Attaches the instruction to the test situation.
     * 
     * @param <code>instruction</code> the instruction.
     */
    public void setInstruction(Instruction instruction)
    {
        this.instruction = instruction;
    }
    
    /**
     * Returns the number of instruction's operands.
     * 
     * @return the number of instruction's operands.
     */
    public int countOperand()
    {
        return instruction.countOperand();
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
        return instruction.getOperand(i);
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
        return instruction.getOperand(name);
    }
    
    /**
     * Returns a description of the instruction.
     * 
     * @return a description of the instruction. 
     */
    public String getDescription()
    {
        return instruction.getDescription();
    }
    
    /**
     * Precondition of the instruction. By default, it checks types
     * compatibility.
     * 
     * @param  <code>processor</code> the processor.
     * 
     * @param  <code>context</code> the context of generation.
     * 
     * @return <code>true</code> if the precondition is satisfied;
     *         <code>false</code> otherwise.
     */
    public boolean precondition(Processor processor, GeneratorContext context)
    {
        int i, size;
     
        size = countOperand();
        for(i = 0; i < size; i++)
        {
            Operand operand = getOperand(i);
            OperandType operandType = operand.getOperandType();
            
            if(operandType.isRegister() && operand.isInput())
            {
                if(context.isDefinedRegister(operand.getRegister()))
                {
                    ContentType contentType = operand.getContentType();

                    if(!contentType.checkType(operand.getLongValue()))
                        { return false; }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Checks if the current test situation is constructed or not.
     * 
     * @return <code>true</code> if the test situation is constructed;
     *         <code>false</code> otherwise.
     */
    public final boolean isConstructed()
    {
        return isConstructed;
    }
    
    /**
     * Sets construction status of the current test situation.
     * 
     * @param <code>isConstructed</code> the construction status.
     */
    public final void setConstructed(boolean isConstructed)
    {
        this.isConstructed = isConstructed;
    }
    
    /**
     * Checks if the current test situation is consistent or not.
     * 
     * @return <code>true</code> if the test situation is consistent;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isConsistent();

    /**
     * Performs some initialization of the current test situation.  
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     */
    public void init(Processor processor, GeneratorContext context) {}
    
    /**
     * Removes auxiliary registers from the context.  
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     */
    public void useRegisters(Processor processor, GeneratorContext context) {}
    
    /**
     * Constructs the current test situation.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     */
    public abstract boolean construct(Processor processor, GeneratorContext context);
    
    /**
     * Checks if a hazard condition is satisfied for the instruction.
     * 
     * @return <code>true</code> if a hazard condition is satisfied;
     *         <code>false</code> otherwise.
     */
    public boolean isHazard(Program program, int position)
    {
        return false;
    }
    
    /**
     * Returns program to be allocated before the instruction that resolves a
     * hazard. Generally such program consists of a certain number of NOPs.
     *  
     * @param  <code>program</code> the program.
     * 
     * @param  <code>position</code> the position of the instruction in the
     *         program.
     * 
     * @return program that resolves a hazard.
     */
    public Program resolveHazard(Program program, int position)
    {
        return new Program();
    }
    
    /**
     * Precondition of the <code>calculate()</code> method (postcondition of
     * the <code>construct()</code> method).
     * 
     * @return <code>true</code> if the constructed values correspond to the
     *         current test situation; <code>false</code> otherwise.
     */
    public boolean calculatePrecondition()
    {
        return true;
    }

    /**
     * Postcondition of the <code>calculate()</code> method.
     * 
     * @return <code>true</code> if the calculated values correspond to the
     *         semantics of the instruction; <code>false</code> otherwise.
     */
    public boolean calculatePostcondition()
    {
        return true;
    }
    
    /**
     * Precondition of the <code>execute()</code> method (postcondition of
     * the <code>prepare()</code> method).
     * 
     * @param  <code>processor</code> the processor.
     * 
     * @return <code>true</code> if the pre-state of the processor corresponds
     *         to the current test situation; <code>false</code> otherwise.
     */
    public boolean executePrecondition(Processor processor)
    {
        return true;
    }
    
    /**
     * Postcondition of the <code>execute()</code> method.
     * 
     * @param  <code>processor</code> the processor.
     * 
     * @return <code>true</code> if the post-state of the processor corresponds
     *         to the semantics of the instruction; <code>false</code> otherwise.
     */
    public boolean executePostcondition(Processor processor)
    {
        return true;
    }

    /**
     * Returns the number of preparation layers.
     * 
     * @return the number of preparation layers.
     */
    public int countPreparationLayer()
    {
        return 1;
    }

    /**
     * Returns a program that makes the preparation of the given layer of the
     * current test situation.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     * 
     * @param <code>layer</code> the preparation layer.
     */
    public abstract Program prepare(Processor processor, GeneratorContext context, int layer);
    
    /**
     * Returns pre-action of the corresponding instruction.
     *  
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     * 
     * @return pre-action of the corresponing instruction.
     */
    public Program preAction(Processor processor, GeneratorContext context)
    {
        return new Program();
    }

    /**
     * Returns post-action of the corresponding instruction.
     *  
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     * 
     * @return post-action of the corresponing instruction.
     */
    public Program postAction(Processor processor, GeneratorContext context)
    {
        return new Program();
    }
    
    /** Initializes the iterator of test situations. */
    public abstract void init();
    
    /**
     * Checks if the iterator is not exhausted (the test situation is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public abstract boolean hasValue();

    /**
     * Returns the current test situation.
     * 
     * @return the current test situation.
     */
    public Situation value()
    {
        return this;
    }
    
    /** Makes iteration. */
    public abstract void next();
    
    /** Stops the iterator. */
    public abstract void stop();

    /**
     * Returns a string representation of the situation.
     * 
     * @return a string representation of the situation.
     */
    public String toString()
    {
        return name;
    }
    
    /**
     * Returns a copy of the test situation.
     * 
     * @return a copy of the test situation.
     */
    public abstract Situation clone();
}
