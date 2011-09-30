/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: GroupConfig.java,v 1.18 2008/08/15 07:20:02 kamkin Exp $
 */

package com.unitesk.testfusion.core.config;

import java.util.HashSet;
import java.util.Set;

import com.unitesk.testfusion.core.config.walker.ConfigEmptyVisitor;
import com.unitesk.testfusion.core.config.walker.ConfigWalker;

/**
 * Configuration of group of instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class GroupConfig extends GroupListConfig
{
    /**
     * Visitor that collects information on equivalence classes of the groups's
     * instructions.
     * 
     * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
     */
    protected static class EquivalenceClassVisitor extends ConfigEmptyVisitor
    {
        /**
         * Class for representing set of equivalence classes.
         * 
         * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
         */
        protected static class EquivalenceClassSet extends HashSet<String>
        {
            public static final long serialVersionUID = 0;
        }
        
        /** Set of equivalence classes of the group's instructions. */
        protected Set<String> set = new EquivalenceClassSet();

        /**
         * Returns the set of equivalence classes of the group's instructions.
         *  
         * @return the set of equivalence classes of the group's instructions.
         */
        public Set<String> getEquivalenceClasses()
        {
            return set;
        }

        /**
         * Visitor that handles the instruction.
         * 
         * @param <code>instruction</code> the instruction to be handled.
         */
        public void onInstruction(InstructionConfig instruction)
        {
            String equivalenceClass = instruction.getEquivalenceClass();
            
            if(equivalenceClass != null && !equivalenceClass.isEmpty())
                { set.add(equivalenceClass); }
        }
    }
    
    /** List of instruction configurations. */
    protected ConfigList<InstructionConfig> instructions = new ConfigList<InstructionConfig>();

    /**
     * Basic constructor.
     * 
     * @param <code>name</code> the name of the group configuration.
     */
    public GroupConfig(String name)
    {
        super(name);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to group configuration object. 
     */
    protected GroupConfig(GroupConfig r)
    {
        super(r);
        
        instructions = r.instructions.clone(this);
    }
    
    /**
     * Returns the fully qualified name of the group.
     * 
     * @return the fully qualified name of the group.
     */
    public String getFullName()
    {
        if(isTop())
            { return getName(); }
        
        return parent.getFullName() + "." + getName();
    }
    
    /**
     * Checks if the group configuration is on top of the group hierarchy,
     * i.e. it has no parent or its parent is not a group configuration.
     * 
     * @return <code>true</code> if the group configuration is top;
     * <code>false</code> otherwise.
     */
    public boolean isTop()
    {
        return parent == null || !(parent instanceof GroupConfig);
    }
    
    /**
     * Checks if the group configuration is a leaf of the group hierarchy,
     * i.e. it has no subgroups.
     * 
     * @return <code>true</code> if the group configuration is leaf;
     * <code>false</code> otherwise.
     */
    public boolean isLeaf()
    {
        return groups.isEmpty();
    }
        
    /**
     * Returns the number of instructions in the group configuration.
     * 
     * @return the number of instructions in the group configuration.
     */
    public int countInstruction()
    {
        return instructions.size();
    }
    
    /**
     * Returns the <code>i</code>-th instruction of the group configuration.
     * 
     * @param  <code>i</code> the index of the instruction configuration.
     * 
     * @return the <code>i</code>-th instruction of the group configuration.
     */
    public InstructionConfig getInstruction(int i)
    {
        return instructions.getConfig(i);
    }
    
    /**
     * Finds the instruction configuration with the given name.
     * 
     * @param  <code>name</code> the name of the instruction configuration to be
     *         found.
     * 
     * @return the subgroup configuration with name <code>name</code> if it
     *         exists in the group configuration; <code>null</code> otherwise.
     */
    public InstructionConfig getInstruction(String name)
    {
        return instructions.getConfig(name);
    }

    /**
     * Adds the instruction to the group configuration.
     * 
     * @param <code>instruction</code> the instruction configuration to be
     *        added.
     */
    public void registerInstruction(InstructionConfig instruction)
    {
        instruction.setParent(this);
        instructions.addConfig(instruction);
    }
    
    /**
     * Returns the set of equivalence classes of the group's instructions.
     *  
     * @return the set of equivalence classes of the group's instructions.
     */
    public Set<String> getEquivalenceClasses()
    {
        EquivalenceClassVisitor visitor = new EquivalenceClassVisitor();
        ConfigWalker walker = new ConfigWalker(this, visitor);
        
        walker.process();
        
        return visitor.getEquivalenceClasses();
    }
    
    /**
     * Checks if the group configration is empty, i.e. it does not contain
     * test situations.
     * 
     * @return <code>true</code> if the group configuration is empty;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        int i, size;
        
        if(!super.isEmpty())
            { return false; }
        
        size = countInstruction();
        for(i = 0; i < size; i++)
        {
            InstructionConfig instruction = getInstruction(i);

            if(!instruction.isEmpty())
                { return false; }
        }
        
        return true;
    }
    
    /**
     * Returns a copy of the group configuration.
     *
     * @return a copy of the group configuration.
     */
    public GroupConfig clone()
    {
        return new GroupConfig(this);
    }
}
