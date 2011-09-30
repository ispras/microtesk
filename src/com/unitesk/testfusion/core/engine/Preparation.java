/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Preparation.java,v 1.2 2008/08/26 12:17:50 kamkin Exp $
 */

package com.unitesk.testfusion.core.engine;

import java.util.ArrayList;

import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.Program;

/**
 * Class that builds multi-layer preparation program.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Preparation
{
    /** Layers of preparation. */
    protected ArrayList<Program> layers = new ArrayList<Program>();

    /** Default constructor. */
    public Preparation() {}

    /**
     * Returns the number of preparation layers.
     * 
     * @return the number of preparation layers.
     */
    public int countPreparationLayer()
    {
        return layers.size();
    }
    
    /**
     * Checks if the preparation program is empty.
     * 
     * @return <code>true</code> if the preparation program is empty;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        int i, size;
        
        size = countPreparationLayer();
        for(i = 0; i < size; i++)
        {
            if(!getProgram(i).isEmpty())
                { return false; }
        }
            
        return true;
    }
    
    /**
     * Adds the instruction to the given preparation layer.
     * 
     * @param <code>instruction</code> the instruction to be added.
     * 
     * @param <code>layer</code> the number of preparation layer.
     */
    public void append(Instruction instruction, int layer)
    {
        Program program = new Program();
        
        program.append(instruction);
        
        append(program, layer);
    }
    
    /**
     * Adds the program to the given preparation layer.
     * 
     * @param <code>program</code> the program to be added.
     * 
     * @param <code>layer</code> the number of preparation layer.
     */
    public void append(Program program, int layer)
    {
        int size = countPreparationLayer();
        
        if(layer > size)
            { throw new IllegalPreparationLayerException(); }

        if(layer == size)
            { layers.add(program); }
        else
        {
            Program prog = layers.get(layer);
            
            prog.append(program);
        }
    }

    /**
     * Adds the instruction to the given preparation layer and execute it.
     * 
     * @param <code>processor</code> the processor to be added.
     * 
     * @param <code>instruction</code> the instruction to be added.
     * 
     * @param <code>layer</code> the number of preparation layer.
     */
    public void appendAndExecute(Processor processor, Instruction instruction, int layer)
    {
        append(instruction, layer);

        instruction.execute(processor);
    }

    /**
     * Adds the program to the given preparation layer and execute it.
     * 
     * @param <code>processor</code> the processor to be added.
     * 
     * @param <code>program</code> the program to be added.
     * 
     * @param <code>layer</code> the number of preparation layer.
     */
    public void appendAndExecute(Processor processor, Program program, int layer)
    {
        append(program, layer);

        program.execute(processor);
    }
    
    /**
     * Returns the prepration program belonging to the given preparation layer.
     * 
     * @param  <code>layer</code> the number of preparation layer.
     * 
     * @return the prepration program belonging to the given preparation layer.
     */
    public Program getProgram(int layer)
    {
        return layers.get(layer);
    }

    /**
     * Returns the preparation layer.
     * 
     * @return the preparation layer.
     */
    public Program getProgram()
    {
        int i, size;
        Program program = new Program();
        
        size = countPreparationLayer();
        for(i = 0; i < size; i++)
            { program.append(getProgram(i)); }
        
        return program;
    }
}
