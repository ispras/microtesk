/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestTree.java,v 1.18 2008/12/18 14:01:32 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.test;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.DropMode;

import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.tree.SelectionListener;

/**
 * Class for test tree, wich represents structure of sections configurations.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class TestTree extends BasicTestTree
{
    public static final long serialVersionUID = 0;
    
    /**
     * Base constructor.
     * 
     * @param <code>frame</code> the parent frame.
     */
    public TestTree(GUI frame)
    {
        super(frame);
        
        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);
        setTransferHandler(new TestTreeTransferHandler(frame));
        
        updateConfig();

        setCellRenderer(new TestTreeNodeRenderer());
        addMouseListener(new TestTreeNodeListener(frame));
        
        addKeyListener(new SelectionListener(frame, this));
    }

    //***************************************************************
    // Operations with tree nodes.
    //***************************************************************
    
    /**
     * Renames a tree node.
     * 
     * @param <code>node</code> the tree node with renamed section
     *        configuration.
     */
    public void renameSectionNode(TestTreeNode node)
    {
        DefaultTreeModel model = (DefaultTreeModel)getModel();
        model.nodeChanged(node);
        update();
    }
    
    /**
     * Selects tree node.
     * 
     * @param <code>node</code> the node for selection.
     */
    public void selectNode(TestTreeNode node)
    {
        frame.showConfig(node.getConfig());
    }
    
    /**
     * Removes section node from the tree.
     * 
     * @param <code>node</code> the node for removing.
     */
    public void removeSectionNode(TestTreeNode node)
    {
        DefaultTreeModel model = (DefaultTreeModel)getModel();
        
        model.removeNodeFromParent(node);
        
        update();
    }
    
    /**
     * Adds new section node to the tree.
     * 
     * @param <code>newSection</code> the new section configuration.
     * 
     * @param <code>parentNode</code> the parent node to inserting new node.
     */
    public void addSectionNode(SectionConfig newSection,
            TestTreeNode parentNode)
    {
        DefaultTreeModel model = (DefaultTreeModel)getModel();
        
        SectionNode newNode = new SectionNode(newSection); 
        
        model.insertNodeInto(newNode, parentNode,
                parentNode.getChildCount());
        
        // expand path with new node
        
        if (parentNode.getChildCount() == 1)
            expandPath(new TreePath(parentNode.getPath()));
        
        update();
    }
    
    /**
     * Moves tree node.
     * 
     * @param <code>section</code> the section configuration for moving.
     * 
     * @parem <code>newParent</code> new parent node.
     *  
     * @param <code>index</code> the index of position, where node
     *        should be moved.
     */
    public void moveSectionNode(TestTreeNode node,
            TestTreeNode newParent, int index)
    {
        DefaultTreeModel model = (DefaultTreeModel)getModel();
        
        TreePath oldPath = new TreePath(node.getPath());
        
        SectionNode newNode = 
            new SectionNode((SectionConfig)node.getConfig());
        
        model.insertNodeInto(newNode, newParent, index);
        
        TreePath newPath = new TreePath(newNode.getPath());
        
        makeVisible(newPath.pathByAddingChild(node));
        
        // expand new parent
        expandPath(new TreePath(newParent.getPath()));
        
        // restore configuration of expansion of removed node
        expandPathRecursivelyDown(newPath, oldPath);
        
        model.removeNodeFromParent(node);
    }
}
