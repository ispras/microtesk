/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SectionTreeVisitorAdapter.java,v 1.1 2008/08/18 14:09:59 kozlov Exp $
 */
package com.unitesk.testfusion.gui.tree.section;

import com.unitesk.testfusion.gui.tree.Tree;
import com.unitesk.testfusion.gui.tree.TreeNode;
import com.unitesk.testfusion.gui.tree.TreeNodeVisitor;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class SectionTreeVisitorAdapter implements TreeNodeVisitor 
{
    SectionTreeNodeVisitor visitor;
    
    public SectionTreeVisitorAdapter(SectionTreeNodeVisitor visitor)
    {
        this.visitor = visitor;
    }
    
    public void onNode(Tree tree, TreeNode node)
    {
        SectionTreeNode sectionNode = (SectionTreeNode)node;
        SectionTree sectionTree = (SectionTree)tree;
        
        if(sectionNode.isProcessorNode())
            visitor.onProcessor(sectionTree, (ProcessorNode)node);
        else if(sectionNode.isGroupNode())
            visitor.onGroup(sectionTree, (GroupNode)node);
        else if(sectionNode.isInstructionNode())
            visitor.onInstruction(sectionTree, (InstructionNode)node);
        else if(sectionNode.isSituationNode())
            visitor.onSituation(sectionTree, (SituationNode)node);
    }
}
