/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: InstructionFactorization.java,v 1.3 2009/08/13 15:54:22 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator;

import java.util.ArrayList;
import java.util.HashMap;

import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.util.Utils;

/**
 * Class <code>InstructionFactorization</code> contains set of
 * equivalence classes of instructions.
 *  
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class InstructionFactorization
{
    /** List of the equivalence classes. */
    protected ArrayList<EquivalenceClass> factorization = new ArrayList<EquivalenceClass>();
    
    /**
     * Maps name of an equivalence class to the index in the
     * <code>instructions</code> list.
     */
    protected HashMap<String, Integer> name2index = new HashMap<String, Integer>();
    
    /** Default constructor. */
    public InstructionFactorization() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to instruction factorization object.
     */
    @SuppressWarnings("unchecked")
    protected InstructionFactorization(InstructionFactorization r)
    {
        factorization = (ArrayList<EquivalenceClass>)r.factorization.clone();
        name2index = (HashMap<String, Integer>)r.name2index.clone();
    }
    
    /**
     * Returns the number of equivalence classes.
     * 
     * @return the number of equivalence classes.
     */
    public int countEquivalenceClass()
    {
        return factorization.size();
    }

    /**
     * Returns the number of instructions.
     * 
     * @return the number of instructions.
     */
    public int countInstruction()
    {
        int i, size, count;
        
        size = factorization.size();
        for(i = count = 0; i < size; i++)
        {
            EquivalenceClass array = factorization.get(i);

            count += array.size();
        }
        
        return count;
    }

    /**
     * Returns the number of instructions in the given equivalence class.
     * 
     * @param  <code>index</code> the index of the equivalence class.
     * 
     * @return the number of instructions.
     */
    public int countInstruction(int equivalenceClass)
    {
        EquivalenceClass array = factorization.get(equivalenceClass);
        
        return array.size();
    }
    
    /**
     * Returns the equivalence class (list of instructions) by the index.
     * 
     * @param  <code>index</code> the index of equivalence class.
     * 
     * @return the equivalence class (list of instructions) associated with the
     *         index.
     */
    public EquivalenceClass getEquivalenceClass(int index)
    {
        return factorization.get(index);
    }

    /**
     * Returns the equivalence class (list of instructions) by the name.
     * 
     * @param  <code>equivalenceClass</code> the name of equivalence class.
     * 
     * @return the equivalence class (list of instructions) associated with the
     *         given name.
     */
    public EquivalenceClass getEquivalenceClass(String equivalenceClass)
    {
        Integer index = name2index.get(equivalenceClass);
        
        if(index == null)
            { return null; }
        
        return factorization.get(index);
    }
    
    /**
     * Returns the instructions by the index.
     * 
     * @param  <code>index</code> the index of instruction.
     * 
     * @return the instruction associated with the index.
     */
    public Instruction getInstruction(int index)
    {
        int i, size;
        
        size = factorization.size();
        for(i = 0; i < size; i++)
        {
            EquivalenceClass array = factorization.get(i);

            if(index < array.size())
                { return array.get(index); }

            index -= array.size();
        }
        
        return null;
    }

    /**
     * Returns the instruction by the index of equivalence class and the index
     * within the equivalence class.
     * 
     * @param  <code>equivalenceClass</code> the index of equivalence class.
     * 
     * @param  <code>instruction</code> the index of instruction within the
     *         equivalence class.
     *         
     * @return the instruction corresponding to the given indexes.
     */
    public Instruction getInstruction(int equivalenceClass, int instruction)
    {
        EquivalenceClass array = factorization.get(equivalenceClass);
        
        return array.get(instruction);
    }
    
    /**
     * Returns the instruction with the given name from the equivalence class.
     * 
     * @param <code>equivalenceClass</code> the name of the equivalence class.

     * @param <code>name</code> the name of the instruction.
     * 
     * @return the instruction with the given name.
     */
    public Instruction getInstruction(String equivalenceClass, String name)
    {
       EquivalenceClass array = getEquivalenceClass(equivalenceClass);
       
       if(array == null)
           { return null; }
       
       return array.getInstruction(name);
    }
    
    /**
     * Registers the instruction in the unique equivalence class.
     * 
     * @param <code>instruction</code> the instruction to be registered.
     */
    public void registerInstruction(Instruction instruction)
    {
        EquivalenceClass array = new EquivalenceClass(instruction);

        factorization.add(array);
    }

    /**
     * Registers the instruction in the given equivalence class.
     * 
     * @param <code>equivalenceClass</code> the equivalence class.
     * 
     * @param <code>instruction</code> the instruction to be registered.
     */
    public void registerInstruction(String equivalenceClass, Instruction instruction)
    {
        if(Utils.isNullOrEmpty(equivalenceClass))
            { registerInstruction(instruction); return; }
        
        Integer index = name2index.get(equivalenceClass);

        instruction.setEquivalenceClass(equivalenceClass);
            
        if(index == null)
        {
            name2index.put(equivalenceClass, index = new Integer(factorization.size()));
            factorization.add(new EquivalenceClass());
        }
        
        EquivalenceClass array = factorization.get(index.intValue());
        array.add(instruction);
    }

    /**
     * Registers the equivalence class.
     * 
     * @param <code>equivalenceClass</code> the equivalence class of
     *        instructions to be registered.
     */    
    public void registerInstructions(EquivalenceClass equivalenceClass)
    {
        if(equivalenceClass.isAnonymous())
            { factorization.add(equivalenceClass); return; }

        Integer index = name2index.get(equivalenceClass.getName());
        
        if(index == null)
        {
            name2index.put(equivalenceClass.getName(), index = new Integer(factorization.size()));
            factorization.add(new EquivalenceClass());
        }
        
        EquivalenceClass array = factorization.get(index.intValue());
        array.addAll(equivalenceClass);
    }
    
    /** Removes all registered instructions. */
    public void clear()
    {
        factorization.clear();
        name2index.clear();
    }
   
    /**
     * Returns a copy of the instruction factorization object.
     * 
     * @return a copy of the instruction factorization object.
     */
    public InstructionFactorization clone()
    {
        return new InstructionFactorization(this);
    }
}
