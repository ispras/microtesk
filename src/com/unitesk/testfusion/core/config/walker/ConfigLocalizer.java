/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigLocalizer.java,v 1.4 2008/08/13 12:46:28 kamkin Exp $
 */
package com.unitesk.testfusion.core.config.walker;

import com.unitesk.testfusion.core.config.*;

/**
 * Visitor that localizes a selection of the items in the test configuration.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ConfigLocalizer extends ConfigEmptyVisitor
{
    /** Test configuration. */
    protected TestConfig test;
    
    /** Flag for multiple groups selection. */
    protected boolean multi_group;
    
    /** Reference to group configuration if there is single group selection. */
    protected GroupConfig group;

    /** Internal flag for handling group configurations. */
    private boolean fixed_group;

    /** Flag for multiple instructions selection. */
    protected boolean multi_instruction;
    
    /**
     * Reference to instruction configuration if there is single instruction
     * selection.
     */
    protected InstructionConfig instruction;
    
    /** Flag for multiple situation selection. */
    protected boolean multi_situation;
    
    /**
     * Reference to situation configuration if there is single situation
     * configuration.
     */
    protected SituationConfig situation;

    /**
     * Checks if group configuration is localized.
     * 
     * @return <code>true</code> if group configuration is localized;
     *         <code>false</code> otherwise.
     */
    public boolean isGroupLocalized()
    {
        return !multi_group && group != null;
    }
    
    /**
     * Return the configuration of the localized group.
     * 
     * @return the configuration of the localized group.
     */
    public GroupConfig getGroup()
    {
        return group;
    }
    
    /**
     * Checks if instruction configuration is localized.
     * 
     * @return <code>true</code> if instruction configuration is localized;
     *         <code>false</code> otherwise.
     */
    public boolean isInstructionLocalized()
    {
        return !multi_instruction && instruction != null;
    }
    
    /**
     * Return the configuration of the localized instruction.
     * 
     * @return the configuration of the localized instruction.
     */
    public InstructionConfig getInstruction()
    {
        return instruction;
    }
    
    /**
     * Checks if test situation is localized.
     * 
     * @return <code>true</code> if test situation configuration is localized;
     *         <code>false</code> otherwise.
     */
    public boolean isSituationLocalized()
    {
        return !multi_situation && situation != null;
    }

    /**
     * Returns the configuration of the localized test situation.
     * 
     * @return the configuration of the localized test situation.
     */
    public SituationConfig getSituation()
    {
        return situation;
    }
    
    /**
     * Handler of test configuration.
     * 
     * @param <code>test</code> the test configuration.
     */
    public void onTest(TestConfig test)
    {
        this.test = test;
    }
    
    /**
     * Handler of group configuration.
     * 
     * @param <code>group</code> the group configuration.
     */
    public void onGroup(GroupConfig group)
    {
        if(!group.isSelected() || multi_group)
            { return; }
        
        if(this.group == null || this.group.isDescendant(group))
        {
            if(fixed_group)
                { return; }

            this.group = group;
        }
        else
        {
            Config config = this.group.getClosestCommonAncestor(group);
            
            if(!(config instanceof GroupConfig))
                { multi_group = true; return; }

            fixed_group = true;
            
            this.group = (GroupConfig)config;
        }
    }

    /**
     * Handler of instruction configuration.
     * 
     * @param <code>instruction</code> the instruction configuration.
     */
    public void onInstruction(InstructionConfig instruction)
    {
        if(!instruction.isSelected() || multi_instruction)
            { return; }
        
        if(this.instruction != null)
            { multi_instruction = true; }
        
        this.instruction = instruction;
    }

    /**
     * Handler of test situation configuration.
     * 
     * @param <code>situation</code> the test situation configuration.
     */
    public void onSituation(SituationConfig situation)
    {
        if(!situation.isSelected() || multi_situation)
            { return; }
        
        if(this.situation != null)
            { multi_situation = true; }
        
        this.situation = situation;
    }
}
