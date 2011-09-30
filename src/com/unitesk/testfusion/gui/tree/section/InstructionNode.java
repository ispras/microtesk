/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: InstructionNode.java,v 1.1 2008/08/18 12:00:02 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.section;

import com.unitesk.testfusion.core.config.InstructionConfig;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class InstructionNode extends SectionTreeNode
{
    public static final long serialVersionUID = 0;
    
    public static final String NODE_ID = "INSTRUCTION_NODE";
    
    public InstructionNode(InstructionConfig instruction)
    {
        super(NODE_ID, instruction);

        int i, size;
        
        size = instruction.countSituation();
        for(i = 0; i < size; i++)
            { add(new SituationNode(instruction.getSituation(i))); }
    }
    
    public InstructionConfig getInstruction()
    {
        return (InstructionConfig)getConfig();
    }
}
