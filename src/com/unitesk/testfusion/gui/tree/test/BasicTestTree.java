/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: BasicTestTree.java,v 1.2 2009/05/21 17:29:13 kamkin Exp $
 */

package com.unitesk.testfusion.gui.tree.test;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.tree.Tree;

/**
 * Class for test tree, wich represents structure of test's sections.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class BasicTestTree extends Tree
{
    public static final long serialVersionUID = 0;
    
    /**
     * Base constructor.
     * 
     * @param <code>frame</code> the parent frame.
     */
    public BasicTestTree(GUI frame)
    {
        super(frame);
    }

    /**
     * Expands new path recursively same as old path.
     * 
     * @param <code>newPath</code> the new path for expansion.
     * 
     * @param <code>oldPath</code> the old path.
     */
    protected void expandPathRecursivelyDown(TreePath newPath, 
            TreePath oldPath)
    {
        if (isExpanded(oldPath))
        {
            this.expandPath(newPath);
        }
        
        TestTreeNode oldNode = (TestTreeNode)oldPath.getLastPathComponent();
        TestTreeNode newNode = (TestTreeNode)newPath.getLastPathComponent();
        
        for (int i = 0; i < oldNode.getChildCount(); i++)
        {
            TestTreeNode oldChild = (TestTreeNode)oldNode.getChildAt(i);
            TestTreeNode newChild = (TestTreeNode)newNode.getChildAt(i);
            
            expandPathRecursivelyDown(new TreePath(newChild.getPath()), 
                    new TreePath(oldChild.getPath()));
        }
    }
    
    /**
     * Updates tree model, uses current test configuration.
     */
    public void updateConfig()
    {
        DefaultTreeModel model = 
            new DefaultTreeModel(new TestNode(frame.getConfig()));
        
        setModel(model);
        
        //expand();
        selectRoot();
    }
}
