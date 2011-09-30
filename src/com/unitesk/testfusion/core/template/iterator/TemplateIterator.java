/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: TemplateIterator.java,v 1.14 2009/08/19 16:50:52 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.iterator.Iterator;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.core.situation.Situation;
import com.unitesk.testfusion.core.util.Utils;

/**
 * Abstract class that represents interface and general functionality of
 * template section iterator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class TemplateIterator implements Iterator
{
    /** Equivalence classes of instructions. */
    protected InstructionFactorization factorization = new InstructionFactorization();
    
    /** Maps name of an instruction to the set of positions in the section. */
    protected HashMap<String, HashSet<Integer>> order = new HashMap<String, HashSet<Integer>>();    
    
    /** Default constructor. */
    public TemplateIterator() {}

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to template iterator object.
     */
    @SuppressWarnings("unchecked")
    protected TemplateIterator(TemplateIterator r)
    {
        factorization = r.factorization.clone();
        order = (HashMap<String, HashSet<Integer>>)r.order.clone();
    }

    /**
     * Returns the number of equivalence classes.
     * 
     * @return the number of equivalence classes.
     */
    public int countEquivalenceClass()
    {
        return factorization.countEquivalenceClass();
    }

    /**
     * Returns the number of instructions.
     * 
     * @return the number of instructions.
     */
    public int countInstruction()
    {
        return factorization.countInstruction();
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
        return factorization.countInstruction(equivalenceClass);
    }
    
    /**
     * Returns the equivalence class (list of instructions) by the index.
     * 
     * @param  <code>index</code> the index of equivalence class.
     * 
     * @return the equivalence class associated with the index.
     */
    public EquivalenceClass getEquivalenceClass(int index)
    {
        return factorization.getEquivalenceClass(index);
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
        return factorization.getInstruction(index);
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
        return factorization.getInstruction(equivalenceClass, instruction);
    }
    
    /**
     * Returns the instruction with the given name from the equivalence class.
     * 
     * @param <code>equivalenceClass</code> the name of equivalence class.
     * 
     * @param <code>name</code> the name of instruction.
     * 
     * @return the instruction with the given name from the equivalence class.
     */
    public Instruction getInstruction(String equivalenceClass, String name)
    {
        return factorization.getInstruction(equivalenceClass, name);
    }
    
    /**
     * Registers the instruction in the unique equivalence class.
     * 
     * @param <code>instruction</code> the instruction to be registered.
     */
    public void registerInstruction(Instruction instruction)
    {
        factorization.registerInstruction(instruction);
    }

    /**
     * Registers the instruction with the test situation in the unique
     * equivalence class.
     * 
     * @param <code>instruction</code> the instruction to be registered.
     * 
     * @param <code>situation</code> the test situation of the instruction.
     */
    public void registerInstruction(Instruction instruction, Situation situation)
    {
        instruction.setSituation(situation);
        registerInstruction(instruction);
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
        factorization.registerInstruction(equivalenceClass, instruction);
    }

    /**
     * Registers the instruction with the test situation in the given
     * equivalence class.
     * 
     * @param <code>equivalenceClass</code> the equivalence class.
     * 
     * @param <code>instruction</code> the instruction to be registered.
     * 
     * @param <code>situation</code> the test situation of the instruction.
     */
    public void registerInstruction(String equivalenceClass, Instruction instruction, Situation situation)
    {
        instruction.setSituation(situation);
        registerInstruction(equivalenceClass, instruction);
    }      
    
    /**
     * Registers the list instructions in the unique equivalence class.
     * 
     * @param <code>equivalenceClass</code> the equivalence class of
     *        instructions to be registered.
     */    
    public void registerInstructions(EquivalenceClass equivalenceClass)
    {
        factorization.registerInstructions(equivalenceClass);
    }

    /**
     * Registers the template iterator, i.e. the instructions registered in the
     * template iterator.
     * 
     * @param <code>templateIterator</code> the template iterator to be
     *        registered.
     */
    public void registerTemplateIterator(TemplateIterator templateIterator)
    {
        int i, size;
        
        size = templateIterator.countInstruction();
        for(i = 0; i < size; i++)
        {
            Instruction instruction = templateIterator.getInstruction(i);

            registerInstruction(instruction.getEquivalenceClass(), instruction);
        }
    }

    /**
     * Registers the template iterator, i.e. the instructions registered in the
     * template iterator, in the given equivalence class.
     * 
     * @param <code>equivalenceClass</code> the equivalence class.
     * 
     * @param <code>templateIterator</code> the template iterator to be
     *        registered.
     */
    public void registerTemplateIterator(String equivalenceClass, TemplateIterator templateIterator)
    {
        int i, size;
        
        size = templateIterator.countInstruction();
        for(i = 0; i < size; i++)
        {
            Instruction instruction = templateIterator.getInstruction(i);
            
            registerInstruction(equivalenceClass, instruction);
        }
    }
    
    private void addIndex(int i, Instruction instruction)
    {
        HashSet<Integer> indexes = order.get(instruction.getName());
        
        if(indexes == null)
            { order.put(instruction.getName(), indexes = new HashSet<Integer>()); }
        
        indexes.add(new Integer(i));
    }

    private void addIndex(Collection<Integer> positions, Instruction instruction)
    {
        HashSet<Integer> indexes = order.get(instruction.getName());
        
        if(indexes == null)
            { order.put(instruction.getName(), indexes = new HashSet<Integer>()); }
        
        indexes.addAll(positions);
    }
    
    private void addIndex(int i, EquivalenceClass equivalenceClass)
    {
        for(Instruction instruction: equivalenceClass)
            { addIndex(i, instruction); }
    }

    private void addIndex(Collection<Integer> positions, EquivalenceClass equivalenceClass)
    {
        for(Instruction instruction: equivalenceClass)
            { addIndex(positions, instruction); }
    }
    
    private void addIndex(int i, TemplateIterator templateIterator)
    {
        int j, size;
        
        size = templateIterator.countInstruction();
        for(j = 0; j < size; j++)
        {
            Instruction instruction = templateIterator.getInstruction(j);
            
            addIndex(i, instruction);
        }
    }

    private void addIndex(Collection<Integer> positions, TemplateIterator templateIterator)
    {
        int j, size;
        
        size = templateIterator.countInstruction();
        for(j = 0; j < size; j++)
        {
            Instruction instruction = templateIterator.getInstruction(j);
            
            addIndex(positions, instruction);
        }
    }
    
    /**
     * Registers the instruction at the specified position of template in the
     * unique equivalence class.
     * 
     * @param <code>i</code> the position of the instruction.
     * 
     * @param <code>instruction</code> the instruction to be registered.
     */
    public void registerInstruction(int i, Instruction instruction)
    {
        registerInstruction(instruction);
        
        addIndex(i, instruction);
    }
    
    /**
     * Registers the instruction with the test situation at the specified
     * position of template in the unique equivalence class.
     * 
     * @param <code>i</code> the position of the instruction.
     * 
     * @param <code>instruction</code> the instruction to be registered.
     * 
     * @param <code>situation</code> the test situation of the instruction.
     */
    public void registerInstruction(int i, Instruction instruction, Situation situation)
    {
        instruction.setSituation(situation);
        registerInstruction(i, instruction);
    }
    
    /**
     * Registers the instruction at the specified position of template in the
     * given equivalence class.
     * 
     * @param <code>i</code> the position of the instruction.
     * 
     * @param <code>equivalenceClass</code> the equivalence class.
     * 
     * @param <code>instruction</code> the instruction to be registered.
     */
    public void registerInstruction(int i, String equivalenceClass, Instruction instruction)
    {
        registerInstruction(equivalenceClass, instruction);
        
        addIndex(i, instruction);        
    }

    /**
     * Registers the instruction with the test situation at the specified
     * position of template in the given equivalence class.
     * 
     * @param <code>i</code> the position of the instruction.
     * 
     * @param <code>equivalenceClass</code> the equivalence class.
     * 
     * @param <code>instruction</code> the instruction to be registered.
     * 
     * @param <code>situation</code> the test situation of the instruction.
     */
    public void registerInstruction(int i, String equivalenceClass, Instruction instruction, Situation situation)
    {
        instruction.setSituation(situation);
        registerInstruction(i, equivalenceClass, instruction);
    }      

    /**
     * Registers the instruction with the test situation at the specified
     * position of template in the given equivalence class.
     * 
     * @param <code>positions</code> the positions of the instruction.
     * 
     * @param <code>equivalenceClass</code> the equivalence class.
     * 
     * @param <code>instruction</code> the instruction to be registered.
     * 
     * @param <code>situation</code> the test situation of the instruction.
     */
    public void registerInstruction(Collection<Integer> positions, String equivalenceClass, Instruction instruction, Situation situation)
    {
        if(Utils.isNullOrEmpty(positions))
            { registerInstruction(equivalenceClass, instruction, situation); }
        else
        {
            for(int i : positions)
                { registerInstruction(i, equivalenceClass, instruction, situation); }
        }
    }      
    
    /**
     * Registers the list of instructions at the specified position of template
     * in the unique equivalence classes.
     * 
     * @param <code>i</code> the position of the instructions.
     * 
     * @param <code>equivalenceClass</code> the equivalence class of
     *        instructions to be registered.
     */
    public void registerInstructions(int i, EquivalenceClass equivalenceClass)
    {
        registerInstructions(equivalenceClass);

        addIndex(i, equivalenceClass);
    }

    /**
     * Registers the list of instructions at the specified positions of template
     * in the unique equivalence classes.
     * 
     * @param <code>positions</code> the positions of the instructions.
     * 
     * @param <code>equivalenceClass</code> the equivalence class of
     *        instructions to be registered.
     */
    public void registerInstructions(Collection<Integer> positions, EquivalenceClass equivalenceClass)
    {
        registerInstructions(equivalenceClass);

        addIndex(positions, equivalenceClass);
    }
    
    /**
     * Registers the template iterator, i.e. the instructions registered in the
     * template iterator, at the specified position of template.
     * 
     * @param <code>i</code> the position of the instructions.
     *        
     * @param <code>templateIterator</code> the template iterator to be
     *        registered.
     */
    public void registerTemplateIterator(int i, TemplateIterator templateIterator)
    {
        registerTemplateIterator(templateIterator);

        addIndex(i, templateIterator);
    }
    
    /**
     * Registers the template iterator, i.e. the instructions registered in the
     * template iterator, at the specified positions of template.
     * 
     * @param <code>positions</code> the positions of the instructions.
     *        
     * @param <code>templateIterator</code> the template iterator to be
     *        registered.
     */
    public void registerTemplateIterator(Collection<Integer> positions, TemplateIterator templateIterator)
    {
        registerTemplateIterator(templateIterator);

        addIndex(positions, templateIterator);
    }
    
    /**
     * Registers the template iterator, i.e. the instructions registered in the
     * template iterator, at the specified position of template in the given
     * equivalence class.
     * 
     * @param <code>i</code> the position of the instructions.
     * 
     * @param <code>equivalenceClass</class> the equivalence class.
     *        
     * @param <code>templateIterator</code> the template iterator to be
     *        registered.
     */
    public void registerTemplateIterator(int i, String equivalenceClass, TemplateIterator templateIterator)
    {
        registerTemplateIterator(equivalenceClass, templateIterator);

        addIndex(i, templateIterator);
    }

    /**
     * Registers the template iterator, i.e. the instructions registered in the
     * template iterator, at the specified positions of template in the given
     * equivalence class.
     * 
     * @param <code>positions</code> the positions of the instructions.
     * 
     * @param <code>equivalenceClass</class> the equivalence class.
     *        
     * @param <code>templateIterator</code> the template iterator to be
     *        registered.
     */
    public void registerTemplateIterator(Collection<Integer> positions, String equivalenceClass, TemplateIterator templateIterator)
    {
        registerTemplateIterator(equivalenceClass, templateIterator);

        addIndex(positions, templateIterator);
    }
    
    /** Removes all registered instructions. */
    public void clear()
    {
        factorization.clear();
        order.clear();
    }
    
    /** Initializes the iterator. */
    public abstract void init();
    
    /**
     * Checks if the iterator is not exhausted (template is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public abstract boolean hasValue();
    
    /**
     * Returns the current template.
     * 
     * @return the current template.
     */
    public abstract Program value();
    
    /** Randomizes test template within one iteration. */
    public abstract void randomize();
    
    /** Makes iteration. */
    public abstract void next();
    
    /**
     * Performs auxiliary construction omitted by situations' constructors.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     * 
     * @param <code>template</code> the test template.
     * 
     * @return <code>true</code> if construction successfull;
     *         <code>false</code> otherwise.
     */
    public boolean construct(Processor processor, GeneratorContext context, Program template)
    {
        int i, size;
        
        size = template.countInstruction();
        for(i = 0; i < size; i++)
        {
            Instruction instruction = template.getInstruction(i);
            Situation situation = instruction.getSituation();
            
            if(situation != null)
                { situation.init(processor, context); }
        }
        
        return true;
    }
    
    /**
     * Removes auxiliary registers from the context.  
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     */
    public void useRegisters(Processor processor, GeneratorContext context, Program template)
    {
        int i, size;
        
        size = template.countInstruction();
        for(i = 0; i < size; i++)
        {
            Instruction instruction = template.getInstruction(i);
            Situation situation = instruction.getSituation();
            
            if(situation != null)
                { situation.useRegisters(processor, context); }
        }
    }

    /**
     * Returns a copy of the template iterator.
     * 
     * @return a copy of the template iterator.
     */
    public abstract TemplateIterator clone();
}
