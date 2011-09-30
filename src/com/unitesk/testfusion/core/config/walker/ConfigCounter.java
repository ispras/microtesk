/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigCounter.java,v 1.9 2008/08/13 12:46:28 kamkin Exp $
 */
package com.unitesk.testfusion.core.config.walker;

import com.unitesk.testfusion.core.config.*;

/**
 * Visitor that counts selected items in the configuration.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ConfigCounter extends ConfigEmptyVisitor
{
    /** Number of groups. */
    protected int group_total;
    
    /** Number of selected groups. */
    protected int group_select;
    
    /** Number of top groups. */
    protected int top_group_total;
    
    /** Number of selected groups. */
    protected int top_group_select;
    
    /** Number of leaf groups. */
    protected int leaf_group_total;
    
    /** Number of selected leaf groups. */
    protected int leaf_group_select;
    
    /** Number of instructions. */
    protected int instruction_total;
    
    /** Number of selected instructions. */
    protected int instruction_select;
    
    /** Number of test situations. */
    protected int situation_total;
    
    /** Number of selected test situations. */
    protected int situation_select;
    
    /** Target configuration. */
    protected Config config;
    
    /**
     * Checks if at least one item is selected in the configuration.
     * 
     * @return <code>true</code> if there is selected item in the configuration;
     *         <code>false</code> otherwise.
     */
    public boolean isSelected()
    {
        return group_select > 0
            || instruction_select > 0
            || situation_select > 0;
    }
    
    /**
     * Returns the number of groups.
     * 
     * @return the number of groups.
     */
    public int countGroup()
    {
        return group_total;
    }
    
    /**
     * Returns the number of selected groups.
     * 
     * @return the number of selected groups.
     */
    public int countSelectedGroup()
    {
        return group_select;
    }
    
    /**
     * Returns the number of top groups.
     * 
     * @return the number of top groups.
     */
    public int countTopGroup()
    {
        return top_group_total;
    }
    
    /**
     * Returns the number of selected top groups.
     * 
     * @return the number of selected top groups.
     */
    public int countSelectedTopGroup()
    {
        return top_group_select;
    }
    
    /**
     * Returns the number of leaf groups.
     * 
     * @return the number of leaf groups.
     */
    public int countLeafGroup()
    {
        return leaf_group_total;
    }
    
    /**
     * Returns the number of selected leaf groups.
     * 
     * @return the number of selected leaf groups.
     */
    public int countSelectedLeafGroup()
    {
        return leaf_group_select;
    }
    
    /**
     * Returns the number of instructions.
     * 
     * @return the number of instructions.
     */
    public int countInstruction()
    {
        return instruction_total;
    }
    
    /**
     * Returns the number of selected instructions.
     * 
     * @return the number of selected instructions.
     */
    public int countSelectedInstruction()
    {
        return instruction_select;
    }
    
    /**
     * Returns the number of test situations.
     * 
     * @return the number of test situations.
     */
    public int countSituation()
    {
        return situation_total;
    }
    
    /**
     * Returns the number of selected test situations.
     * 
     * @return the number of selected test situations.
     */
    public int countSelectedSituation()
    {
        return situation_select;
    }
    
    /**
     * Resets all counts.
     * 
     * @param <code>config</code> the configuration.
     */
    public void onStart(Config config)
    {
        this.config = config;

        group_total = 0;
        group_select = 0;
        
        top_group_total = 0;
        top_group_select = 0;
        
        leaf_group_total = 0;
        leaf_group_select = 0;
        
        instruction_total = 0;
        instruction_select = 0;
        
        situation_total = 0;
        situation_select = 0;
    }
    
    /**
     * Handler of group configuration.
     * 
     * @param <code>group</code> the group configuration.
     */
    public void onGroup(GroupConfig group)
    {
        if(config.equals(group))
            { return; }
        
        group_total++;
        
        if(group.isSelected())
            { group_select++; }

        if(config.equals(group.getParent()))
        {
            top_group_total++;
            
            if(group.isSelected())
                { top_group_select++; }
        }
        
        if(group.isLeaf())
        {
            leaf_group_total++;
            
            if(group.isSelected())
                { leaf_group_select++; }
        }
    }

    /**
     * Handler of instruction configuration.
     * 
     * @param <code>instruction</code> the instruction configuration.
     */
    public void onInstruction(InstructionConfig instruction)
    {
        if(config.equals(instruction))
            { return; }
    
        instruction_total++;
        
        if(instruction.isSelected())
            { instruction_select++; }
    }

    /**
     * Handler of test situation configuration.
     * 
     * @param <code>situation</code> the test situation configuration.
     */
    public void onSituation(SituationConfig situation)
    {
        if(config.equals(situation))
            { return; }
        
        situation_total++;
        
        if(situation.isSelected())
            { situation_select++; }
    }
}
