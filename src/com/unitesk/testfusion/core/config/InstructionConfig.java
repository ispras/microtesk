/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: InstructionConfig.java,v 1.20 2009/05/21 16:12:55 kamkin Exp $
 */

package com.unitesk.testfusion.core.config;

import java.util.HashSet;

import com.unitesk.testfusion.core.model.Instruction;

/**
 * Configuration of instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class InstructionConfig extends SelectionConfig
{
    /** Instruction object. */
    protected Instruction instruction;
    
    /** Equivalence class of the instruction. */
    protected String equivalenceClass;
    
    /** List of configurations for test situations. */
    protected ConfigList<SituationConfig> situations = new ConfigList<SituationConfig>();
    
    /** List of possible positions of the instruction in a test template. */
    protected HashSet<Integer> positions = new HashSet<Integer>();
    
    /**
     * Basic constructor.
     * 
     * @param <code>instruction</code> the instruction object.
     */
    public InstructionConfig(Instruction instruction)
    {
        super(instruction.getName());
        
        this.instruction = instruction;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to instruction configuration object.
     */
    protected InstructionConfig(InstructionConfig r)
    {
        super(r);
        
        // Instruction object and equivalence class are not copied deeply.
        instruction = r.instruction;
        equivalenceClass = r.equivalenceClass;
        
        situations = r.situations.clone(this);
        
        for(int position: r.positions)
            { positions.add(position); }
    }
    
    /**
     * Returns the fully qualified name of the instruction.
     * 
     * @return the fully qualified name of the instruction.
     */
    public String getFullName()
    {
        return parent.getFullName() + "." + getName();
    }
    
    /**
     * Returns the instruction object.
     * 
     * @return the instruction object.
     */
    public Instruction getInstruction()
    {
        return instruction;
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
     * @param <code>equivalenceClass</code> the new equivalence class of the
     *        instruction.
     */
    public void setEquivalenceClass(String equivalenceClass)
    {
        this.equivalenceClass = equivalenceClass;
    }
    
    /**
     * Returns the number of test situations in the instruction configuration.
     * 
     * @return the number of test situations in the instruction configuration.
     */
    public int countSituation()
    {
        return situations.size();
    }
    
    /**
     * Returns the <code>i</code>-th test situation of the instruction
     * configuration.
     * 
     * @param  <code>i</code> the index of the test situation configuration.
     * 
     * @return the <code>i</code>-th test situation of the instruction
     *         configuration.
     */
    public SituationConfig getSituation(int i)
    {
        return situations.getConfig(i);
    }

    /**
     * Finds the test situation configuration with the given name.
     * 
     * @param  <code>name</code> the name of the test situation configuration
     *         to be found.
     * 
     * @return the test situation configuration with name <code>name</code> if
     *         it exists in the instruction configuration; <code>null</code>
     *         otherwise.
     */
    public SituationConfig getSituation(String name)
    {
        return situations.getConfig(name);
    }
    
    /**
     * Adds the test situation to the instruction configuration.
     * 
     * @param <code>situation</code> the test situation configuration to be
     *        added.
     */
    public void registerSituation(SituationConfig situation)
    {
        situation.setParent(this);
        situations.addConfig(situation);
    }
    
    /**
     * Checks if the instruction configration is empty, i.e. it does not contain
     * test situations.
     * 
     * @return <code>true</code> if the instruction configuration is empty;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        return situations.isEmpty();
    }

    /**
     * Returns the set of possible positions of the instruction.
     * 
     * @return the set of possible positions of the instruction.
     */
    public HashSet<Integer> getPositions()
    {
        return positions;
    }

    /**
     * Sets the set of possible positions of the instruction.
     * 
     * @param <code>positions</code> the set of possible positions of the instruction.
     */
    public void setPositions(HashSet<Integer> positions)
    {
        this.positions = positions;
    }
    
    /**
     * Adds the position to the set of possible positions of the instruction.
     * 
     * @param <code>position</code> the position to be added. 
     */
    public void registerPosition(int position)
    {
        positions.add(position);
    }
    
    /**
     * Removes the position from the set of possible positions of the instruction.
     * 
     * @param <code>position</code> the position to be removed. 
     * 
     */
    public void removePosition(int position)
    {
        positions.remove(position);
    }
    
    /**
     * Returns a copy of the instruction configuration.
     *
     * @return a copy of the instruction configuration.
     */
    public InstructionConfig clone()
    {
        return new InstructionConfig(this);
    }
}
