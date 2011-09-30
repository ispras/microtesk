/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TreeNodeVisitor.java,v 1.7 2008/08/18 14:09:58 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface TreeNodeVisitor
{
    public void onNode(Tree tree, TreeNode node);
}
