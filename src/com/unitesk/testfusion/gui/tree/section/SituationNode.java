/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SituationNode.java,v 1.1 2008/08/18 12:00:02 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.section;

import com.unitesk.testfusion.core.config.*;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SituationNode extends SectionTreeNode
{
    public static final long serialVersionUID = 0;
    
    public static final String NODE_ID = "SITUATION_NODE";
    
    public SituationNode(SituationConfig situation)
    {
        super(NODE_ID, situation);
    }
    
    public SituationConfig getSituation()
    {
        return (SituationConfig)getConfig();
    }
    
    public void updateSelection()
    {
        setSelected(getSituation().isSelected(), true);
    }
}
