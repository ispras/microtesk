/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: EquivalenceClass.java,v 1.4 2009/08/13 15:54:22 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator;

import java.util.ArrayList;

import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.util.Utils;

/**
 * Equivalence class of the instructions.
 *  
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class EquivalenceClass extends ArrayList<Instruction>
{
    public static final long serialVersionUID = 0;
    
    /** Name of the equivalence class. */
    protected String name;
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> name of the equivalence class.
     */
    public EquivalenceClass(String name)
    {
        this.name = name;
    }
    
    /** Default constructor. */
    public EquivalenceClass()
    {
        this("");
    }
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> name of the equivalence class.
     * 
     * @param <code>instruction</code> instruction to be added to the
     *        equivalence class.
     */
    public EquivalenceClass(String name, Instruction instruction)
    {
        this(name);
        
        registerInstruction(instruction);
    }

    /**
     * Constructor.
     * 
     * @param <code>instruction</code> instruction to be added to the
     *        equivalence class.
     */
    public EquivalenceClass(Instruction instruction)
    {
        this(instruction.getName());
        
        registerInstruction(instruction);
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> name of the equivalence class.
     * 
     * @param <code>instructions</code> instructions to be added to the
     *        equivalence class.
     */
    public EquivalenceClass(String name, ArrayList<Instruction> instructions)
    {
        this(name);
        
        registerInstructions(instructions);
    }    

    /**
     * Constructor.
     * 
     * @param <code>instructions</code> instructions to be added to the
     *        equivalence class.
     */
    public EquivalenceClass(ArrayList<Instruction> instructions)
    {
        this();
        
        registerInstructions(instructions);
    }        
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to equivalence class object.
     */
    protected EquivalenceClass(EquivalenceClass r)
    {
        super(r);
        
        name = r.name;
    }
    
    /**
     * Checks if the equivalence class is anonymous or not.
     * 
     * @return <code>true</code> if the equivalence class is anonymous;
     *         <code>false</code> otherwise.
     */
    public boolean isAnonymous()
    {
        return Utils.isNullOrEmpty(name);
    }
    
    /**
     * Returns name of the equivalence class.
     * 
     * @return name of the equivalence class.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the instruction with the given name.
     * 
     * @param <code>name</code> the name of instruction.
     * 
     * @return the instruction with the given name.
     */
    public Instruction getInstruction(String name)
    {
        for(Instruction instruction : this)
        {
            if(instruction.getName().equals(name))
                { return instruction; }
        }
        
        return null;
    }
    
    /**
     * Adds the instruction to the equivalence class.
     * 
     * @param <code>instruction</code> instruction to be added.
     */
    public void registerInstruction(Instruction instruction)
    {
        add(instruction);
    }
    
    /**
     * Adds the instructions to the equivalence class.
     * 
     * @param <code>instructions</code> instructions to be added.
     */
    public void registerInstructions(ArrayList<Instruction> instructions)
    {
        addAll(instructions);
    }
    
    /**
     * Returns a copy of the equivalence class.
     * 
     * @return a copy of the equivalence class.
     */
    public EquivalenceClass clone()
    {
        return new EquivalenceClass(this);
    }
}
