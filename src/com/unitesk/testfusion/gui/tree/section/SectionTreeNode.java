/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SectionTreeNode.java,v 1.2 2008/12/18 14:01:31 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.section;

import com.unitesk.testfusion.core.config.SelectionConfig;
import com.unitesk.testfusion.gui.tree.TreeNode;
import com.unitesk.testfusion.gui.tree.section.GroupNode;
import com.unitesk.testfusion.gui.tree.section.InstructionNode;
import com.unitesk.testfusion.gui.tree.section.ProcessorNode;
import com.unitesk.testfusion.gui.tree.section.SituationNode;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class SectionTreeNode extends TreeNode 
{
    public static final long serialVersionUID = 0;
    
    public final static String PROCESSOR_NODE_ID   = ProcessorNode.NODE_ID;
    public final static String GROUP_NODE_ID       = GroupNode.NODE_ID;
    public final static String INSTRUCTION_NODE_ID = InstructionNode.NODE_ID;
    public final static String SITUATION_NODE_ID   = SituationNode.NODE_ID;

    public SectionTreeNode(String nodeID)
    {
        this(nodeID, null);
    }
    
    public SectionTreeNode(String nodeID, SelectionConfig config) 
    {
        this(nodeID, config, true);
    }
    
    public SectionTreeNode(String nodeID, SelectionConfig config, 
            boolean allowsChildren) 
    {
        this(nodeID, config, allowsChildren, 
                (config != null) ? config.isSelected() : false);
    }

    public SectionTreeNode(String nodeID, SelectionConfig config,
            boolean allowsChildren, boolean isSelected) 
    {
        super(nodeID, config, allowsChildren);
        
        setSelected(isSelected);
    }

    public boolean isProcessorNode()
    {
        return nodeID.equals(PROCESSOR_NODE_ID);
    }
    
    public boolean isGroupNode()
    {
        return nodeID.equals(GROUP_NODE_ID);
    }
    
    public boolean isInstructionNode()
    {
        return nodeID.equals(INSTRUCTION_NODE_ID);
    }
    
    public boolean isSituationNode()
    {
        return nodeID.equals(SITUATION_NODE_ID);
    }
    
    public SelectionConfig getConfig()
    {
        return (SelectionConfig)config;
    }
    
    public boolean isSelected() 
    {
        return ((SelectionConfig)config).isSelected();
    }

    public void setSelected()
    {
        if(!isEnabled())
            { return; }
        
        ((SelectionConfig)config).setSelected();
    }
    
    public void setSelected(boolean selected)
    {
        if(!isEnabled())
            { return; }
        
        ((SelectionConfig)config).setSelected(selected);
    }
    
    public void setSelected(boolean selected, boolean digDown) 
    {
        if(!isEnabled())
            { return; }
        
        if(digDown)
        {
            ((SelectionConfig)config).setSelectedWithPropagation(selected);
        }
        else
            { ((SelectionConfig)config).setSelected(selected); }
    }

    public boolean isEnabled()
    {
        return config != null && !config.isEmpty();
    }
}