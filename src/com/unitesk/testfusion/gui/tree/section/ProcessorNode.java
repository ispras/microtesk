/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ProcessorNode.java,v 1.1 2008/08/18 12:00:02 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.section;

import com.unitesk.testfusion.core.config.*;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ProcessorNode extends SectionTreeNode
{
    public static final long serialVersionUID = 0;
    
    public static final String NODE_ID = "PROCESSOR_NODE";
    
    public ProcessorNode(ProcessorConfig processor)
    {
        super(NODE_ID, processor);

        if(processor != null)
        {
            int i, size;
            
            size = processor.countGroup();
            for(i = 0; i < size; i++)
                { add(new GroupNode(processor.getGroup(i))); }
        }
    }
    
    public ProcessorConfig getProcessor()
    {
        return (ProcessorConfig)getConfig();
    }
}
