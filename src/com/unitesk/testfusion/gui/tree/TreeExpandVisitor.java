/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TreeExpandVisitor.java,v 1.1 2008/12/25 12:07:11 kozlov Exp $
 */
package com.unitesk.testfusion.gui.tree;

import javax.swing.tree.TreePath;

import com.unitesk.testfusion.gui.tree.Tree;
import com.unitesk.testfusion.gui.tree.TreeNode;
import com.unitesk.testfusion.gui.tree.TreeNodeVisitor;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class TreeExpandVisitor implements TreeNodeVisitor 
{
    public void onNode(Tree tree, TreeNode node)
    {
        tree.expandPath(new TreePath(node.getPath()));
    }
}
