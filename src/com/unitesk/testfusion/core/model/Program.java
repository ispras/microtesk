/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Program.java,v 1.15 2010/01/13 11:47:10 vorobyev Exp $
 */

package com.unitesk.testfusion.core.model;

import java.util.ArrayList;

import com.unitesk.testfusion.core.dependency.Dependency;
import com.unitesk.testfusion.core.model.memory.MemoryObject;

/**
 * Class that represents a program.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Program extends MemoryObject implements Executable
{
    /** List of the instructions. */
    protected ArrayList<Instruction> instructions = new ArrayList<Instruction>();

    /** Default constructor. */
    public Program() {}

    /**
     * Creates the single-instruction program. 
     * 
     * @param <code>instruction</code> the instruction.
     */
    public Program(Instruction instruction)
    {
        this();
        
        append(instruction);
    }

    /**
     * Returns the number of instructions in the program.
     * 
     * @return the number of instructions in the program.
     */
    public int countInstruction()
    {
        return instructions.size();
    }

    /**
     * Checks if the program is empty or not.
     * 
     * @return <code>true</code> if the program is empty; <code>false</code>
     *         otherwise.
     */
    public boolean isEmpty()
    {
        return instructions.isEmpty();
    }
    
    /**
     * Returns the <code>i</code>-th instruction of the program.
     * 
     * @param  <code>i</code> the index of the instruction.
     * 
     * @return the <code>i</code>-th instruction of the program.
     */
    public Instruction getInstruction(int index)
    {
        return instructions.get(index);
    }

    /**
     * Sets the address and the position of the instruction.
     * 
     * @param <code>instruction</code> the instruction to be added to the end
     *        of the program.
     */
    protected void allocate(Instruction instruction)
    {
        instruction.setPosition(instructions.size());
        instruction.setAddress(getAddress());
        
        incrementAddress(instruction.getSize());
    }
    
    /**
     * Sets the address and the position of the instruction.
     * 
     * @param <code>instruction</code> the instruction to be added at the given
     *        position of the program.
     * 
     * @param <code>position</code> the position.
     */
    protected void allocate(Instruction instruction, int position)
    {
        int i, size;
        
        incrementAddress(instruction.getSize());
        
        if(position >= instructions.size())
            { allocate(instruction); return; }
        
        instruction.setPosition(position);
        instruction.setAddress(getInstruction(position).getAddress());
        
        size = countInstruction();
        for(i = position; i < size; i++)
        {
            Instruction shifted = getInstruction(i);
            
            shifted.setPosition(i + 1);
            shifted.incrementAddress(instruction.getSize());
        }
    }

    /**
     * Adds the instruction to the end of the program.
     * 
     * @param <code>instruction</code> the instruction to be added.
     */
    public void append(Instruction instruction)
    {   
        allocate(instruction);
        
        instructions.add(instruction);
    }

    /**
     * Adds the program to the end of this program.
     * 
     * @param <code>program</code> the program to be added.
     */
    public void append(Program program)
    {
        for(int i = 0; i < program.countInstruction(); i++)
            { append(program.getInstruction(i)); }
    }
    
    /**
     * Adds the instruction at the given position of the program.
     * 
     * @param <code>instruction</code> the instruction to be added.
     * 
     * @param <code>position</code> the position.
     */
    public void insert(Instruction instruction, int position)
    {
        allocate(instruction, position);
        
        instructions.add(position, instruction);        
    }
    
    /**
     * Adds the program at the given position of this program.
     * 
     * @param <code>program</code> the program to be added.
     * 
     * @param <code>position</code> the position.
     */
    public void insert(Program program, int position)
    {
        for(int i = 0; i < program.countInstruction(); i++)
            { insert(program.getInstruction(i), position + i); }
    }

    /**
     * Removes instruction at the given position.
     *  
     * @param <code>position</code> the position.
     */
    public void remove(int position)
    {
        int i, size, offset;
        
        Instruction removed = instructions.remove(position);
        offset = removed.getSize();
        
        size = instructions.size();
        for(i = position; i < size; i++)
        {
            Instruction instruction = instructions.get(i);
            instruction.decrementAddress(offset);
            instruction.decrementPosition(1);
        }
    }

    /**
     * Executes the program on the processor.
     * 
     * @param <code>processor</code> the processor.
     */
    public void execute(Processor processor)
    {
        int i, size;
        
        size = countInstruction();
        for(i = 0; i < size; i++)
        {
            Instruction instruction = getInstruction(i);
            
            instruction.execute(processor);
        }
    }

    /**
     * Adds the instruction to the end of the program and executes it.
     * 
     * @param <code>instruction</code> the instruction to be added and executed.
     */
    public void appendAndExecute(Processor processor, Instruction instruction)
    {
        instruction.execute(processor);
        
        append(instruction);
    }
    
    /**
     * Adds the program to the end of this program and executes it.
     * 
     * @param <code>program</code> the program to be added and executed.
     */
    public void appendAndExecute(Processor processor, Program program)
    {
        program.execute(processor);
        
        append(program);
    }
    
    /**
     * Remove the prefix of the program.
     * 
     * @param <code>size</code> the size of the prefix.
     */
    public void removePrefix(int size)
    {
        if(instructions.size() < size)
            { size = instructions.size(); }
        
        while(size-- > 0)
            { instructions.remove(0); }
    }
    
    /** Removes all instructions from the program. */
    public void clear()
    {
        instructions.clear();
    }
    
    /**
     * Returns a string representation of the program.
     * 
     * @return a string representation of the program.
     */
    public String toString()
    {
        int i, size;
        StringBuffer buffer = new StringBuffer();
        
        size = countInstruction();
        for(i = 0; i < size; i++)
        {
            buffer.append(getInstruction(i));
            buffer.append('\n');
        }
        
        return buffer.toString();
    }

    /**
     * Returns a copy of the program.
     * 
     * @return a copy of the program.
     */
    public Program clone()
    {
        int i, j, k, size1, size2, size3;
        Program program = new Program();
        
        size1 = countInstruction();
        for(i = 0; i < size1; i++)
        {
            Instruction origInstruction = getInstruction(i);
            Instruction copyInstruction = origInstruction.clone(); 

            program.append(copyInstruction);

            size2 = origInstruction.countOperand();
            for(j = 0; j < size2; j++)
            {
                Operand origDepsOperand = origInstruction.getOperand(j);
                Operand copyDepsOperand = copyInstruction.getOperand(j);
                
                size3 = origDepsOperand.countForwardDependency();
                
                for(k = 0; k < size3; k++)
                {
                    Dependency dep = origDepsOperand.getForwardDependency(k);
                        
                    Operand origFromOperand = dep.getDeterminantOperand();
                    
                    Instruction origFromInstruction = origFromOperand.getInstruction();
                    
                    Instruction copyFromInstruction = program.getInstruction(origFromInstruction.getPosition());
                    Operand copyFromOperand = copyFromInstruction.getOperand(origFromOperand.getName());
                    
                    dep.clone(copyDepsOperand, copyFromOperand);
                }
            }
        }
        
        return program;
    }
}
