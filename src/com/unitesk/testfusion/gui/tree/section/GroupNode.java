/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: GroupNode.java,v 1.1 2008/08/18 12:00:02 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.section;

import com.unitesk.testfusion.core.config.GroupConfig;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class GroupNode extends SectionTreeNode
{
    public static final long serialVersionUID = 0;

    public static final String NODE_ID = "GROUP_NODE";
    
    public GroupNode(GroupConfig group)
    {
        super(NODE_ID, group);
        
        int i, size;
        
        size = group.countGroup();
        for(i = 0; i < size; i++)
            { add(new GroupNode(group.getGroup(i))); }
        
        size = group.countInstruction();
        for(i = 0; i < size; i++)
            { add(new InstructionNode(group.getInstruction(i))); }
    }
    
    public GroupConfig getGroup()
    {
        return (GroupConfig)getConfig();
    }
}
