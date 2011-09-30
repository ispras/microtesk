/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: MoveSectionAction.java,v 1.9 2008/09/01 12:33:12 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.action;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.tree.test.*;

/**
 * Contains actions, which are implemented when section is moved
 * in sequence of sibling.
 *  
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class MoveSectionAction
{
    /** GUI frame. */
    protected GUI frame;
    
    /** Section to be moved. */
    protected SectionConfig section; 
    
    /** New parent section. */
    protected SectionListConfig newParent;
    
    /** Index, where section should be moved */
    protected int index;
    
    /** Node in test tree to be moved */
    protected TestTreeNode node;
    
    /** New parent node in test tree */
    protected TestTreeNode newParentNode;
    
    /** Is moving nodes in test known. */  
    protected boolean isNodesKnown;
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     *  
     * @param <code>section</code> the section to be moved.
     * 
     * @param <code>index</code> the index, where section should be moved. 
     * 
     */
    public MoveSectionAction(GUI frame, SectionConfig section, int index)
    {
        this.frame = frame;
        this.section = section;
        this.index = index;
        
        newParent = (SectionListConfig)section.getParent();
        
        isNodesKnown = false;
        
        TestTree tree = frame.getTestTree();
        
        this.node = (TestTreeNode)tree.getNode(section);
        this.newParentNode = (TestTreeNode)node.getParent();
    }
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     *  
     * @param <code>node</code> the node to be moved.
     * 
     * @param <code>newParent<code> new parent node. 
     * 
     * @param <code>index</code> the index, where section should be moved. 
     * 
     */
    public MoveSectionAction(GUI frame, TestTreeNode node,
            TestTreeNode newParentNode, int index)
    {
        this.frame = frame;
        this.node = node;
        this.newParentNode = newParentNode;
        this.index = index;
        
        isNodesKnown = true;
        
        section = (SectionConfig)node.getConfig();
        newParent = newParentNode.getConfig();
    }
    
    /**
     * Executes actions.
     */
    public void execute()
    {
        SectionListConfig oldParent = (SectionListConfig)section.getParent();
        
        int oldIndex = oldParent.getIndex(section);
        
        if (oldIndex < index)
        {
            newParent.registerSection(index, section);
            oldParent.removeSection(section);
        }
        else
        {
            oldParent.removeSection(section);
            newParent.registerSection(index, section);
        }
        
        // current test has been chaned
        frame.setTestHasChanges();
        
        frame.updateTitle();
        
        // updates tree
        updateTree();
        
        // updates table
        updateTable();
        
        // updates GUI if moving on test tree
        if (isNodesKnown)
            { frame.showConfig(section); }
    }
    
    /**
     * Updates test tree.
     */
    protected void updateTree()
    {
        TestTree tree = frame.getTestTree();
        tree.moveSectionNode(node, newParentNode, index);
    }
    
    /**
     * Updates table.
     */
    protected void updateTable()
    {
        // Do nothing
    }
}
