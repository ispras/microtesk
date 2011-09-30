/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigSelector.java,v 1.6 2008/08/13 12:46:28 kamkin Exp $
 */
package com.unitesk.testfusion.core.config.walker;

import com.unitesk.testfusion.core.config.*;

/**
 * Visitor that recursively selects/deselects configuration items.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ConfigSelector extends ConfigEmptyVisitor
{
    /**
     * Selection status: <code>true</code> for selection; <code>false</code>
     * for deselection.
     */
    protected boolean selected;
    
    /**
     * Constructor.
     * 
     * @param <code>selected</code> the selection status.
     */
    public ConfigSelector(boolean selected)
    {
        this.selected = selected;
    }
    
    /**
     * Selects/deselects the configuration.
     * 
     * @param <code>config</code> the configuration to be selected/deselected.
     */
    protected void setSelected(SelectionConfig config)
    {
        if(!config.isEmpty())
            { config.setSelected(selected); }
    }
    
    /**
     * Handler of processor configuration.
     * 
     * @param <code>processor</code> the processor configuration.
     */
    public void onProcessor(ProcessorConfig processor)
    {
        setSelected(processor);
    }

    /**
     * Handler of group configuration.
     * 
     * @param <code>group</code> the group configuration.
     */
    public void onGroup(GroupConfig group)
    {
        setSelected(group);
    }

    /**
     * Handler of instruction configuration.
     * 
     * @param <code>instruction</code> the instruction configuration.
     */
    public void onInstruction(InstructionConfig instruction)
    {
        setSelected(instruction);
    }

    /**
     * Handler of test situation configuration.
     * 
     * @param <code>situation</code> the test situation configuration.
     */
    public void onSituation(SituationConfig situation)
    {
        setSelected(situation);
    }
}
