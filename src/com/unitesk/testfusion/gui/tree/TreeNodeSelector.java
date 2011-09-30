/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TreeNodeSelector.java,v 1.6 2008/08/18 14:09:58 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree;

import javax.swing.tree.TreePath;

import com.unitesk.testfusion.core.config.Config;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TreeNodeSelector implements TreeNodeVisitor
{
	protected Config config;
	
	public TreeNodeSelector(Config config)
	{
		this.config = config;
	}

    public void onNode(Tree tree, TreeNode node)
    {
        if(config.equals(node.getConfig()))
        {
            TreePath path = new TreePath(node.getPath());
            
            tree.setSelectionPath(path);
        }
    }
}
