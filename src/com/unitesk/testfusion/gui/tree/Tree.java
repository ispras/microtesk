/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Tree.java,v 1.22 2008/12/25 12:07:11 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree;

import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.gui.GUI;

/**
 * Abstract class for trees uses in GUI.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Tree extends JTree
{
    public static final long serialVersionUID = 0;

    /** Parent frame. */
    protected GUI frame;
    
    /**
     * Base constructor.
     * 
     * @param <code>frame</code> the parent frame.
     */
    public Tree(GUI frame)
    {
        this.frame = frame;

        updateConfig();

        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setBorder(BorderFactory.createEmptyBorder());
    }
    
    /**
     * Updates tree model's configuration.
     */
    public abstract void updateConfig();
    
    public TreeNode getRoot()
    {
        TreeModel model = getModel();
        return (TreeNode)model.getRoot();
    }
    
    public void selectRoot()
    {
        setSelectionPath(new TreePath(getRoot().getPath()));
    }
    
    public void expand()
    {
        TreeExpandVisitor visitor = new TreeExpandVisitor(); 
        DepthFirstTreeWalker walker = new DepthFirstTreeWalker(this, visitor);
        
        walker.process();
    }
    
    /**
     * Gets tree node with specified configuration.
     * 
     * @param <code>config</code> the specified configuration.
     * 
     * @return the tree node with specified configuration if such
     *         node exists, or null otherwise.
     */
    public TreeNode getNode(Config config)
    {
        TreeNode root = getRoot(); 
        if (config.equals(root.getConfig()))
            { return root; }
        else 
            { return root.getNodeFromChild(config); }
    }
    
    /**
     * Updates tree's view.
     */
    public void update()
    {
        revalidate();
        repaint();
    }
    

}
