/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: DepthFirstTreeWalker.java,v 1.10 2008/08/18 14:09:58 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree;

import javax.swing.tree.TreePath;

public class DepthFirstTreeWalker implements TreeWalker
{
    public static final int VISIT_NODE_BEFORE_CHILDREN = 0;
    public static final int VISIT_NODE_AFTER_CHILDREN  = 1;
    
    protected Tree tree;
    protected TreeNodeVisitor visitor;
    protected int mode;
    
    /** Visit children of collapsed nodes */
    protected boolean visit;
    
    public DepthFirstTreeWalker(Tree tree, TreeNodeVisitor visitor, int mode, boolean visit)
    {
        this.tree = tree;
        this.visitor = visitor;
        this.mode = mode;
        this.visit = visit;
    }
    
    public DepthFirstTreeWalker(Tree tree, TreeNodeVisitor visitor, int mode)
    {
        this(tree, visitor, mode, true);
    }

    public DepthFirstTreeWalker(Tree tree, TreeNodeVisitor visitor, boolean visit)
    {
        this(tree, visitor, VISIT_NODE_BEFORE_CHILDREN, visit);
    }

    public DepthFirstTreeWalker(Tree tree, TreeNodeVisitor visitor)
    {
        this(tree, visitor, VISIT_NODE_BEFORE_CHILDREN, true);
    }

    public void process(TreeNode node)
    {
        int i, size;
        
        if(mode == VISIT_NODE_BEFORE_CHILDREN)
            { visitor.onNode(tree, node); }

        if(visit || tree.isExpanded(new TreePath(node.getPath())))
        {
            size = node.getChildCount();
            for(i = 0; i < size; i++)
            {
                TreeNode child = (TreeNode)node.getChildAt(i);
                process(child);
            }
        }
        
        if(mode == VISIT_NODE_AFTER_CHILDREN)
            { visitor.onNode(tree, node); }
    }
    
    public void process()
    {
        process(tree.getRoot());
    }
}
