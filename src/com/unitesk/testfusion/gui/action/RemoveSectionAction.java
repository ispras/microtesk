/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: RemoveSectionAction.java,v 1.11 2008/08/29 13:37:49 kozlov Exp $
 */

package com.unitesk.testfusion.gui.action;

import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.core.config.SectionListConfig;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.Panel;
import com.unitesk.testfusion.gui.tree.test.TestTree;
import com.unitesk.testfusion.gui.tree.test.TestTreeNode;

/**
 * Contains actions, which are implemented when section is removed.
 *   
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class RemoveSectionAction
{
    /** GUI frame. */
    protected GUI frame;
    
    /** Node in test tree with removed section configuration. */
    protected TestTreeNode node;
    
    /** Removed section. */
    protected SectionConfig section; 
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     *  
     * @param <code>section</code> the removed section.
     */
    public RemoveSectionAction(GUI frame, SectionConfig section)
    {
        this.frame = frame;
        this.section = section;
        
        TestTree tree = frame.getTestTree();
        this.node = (TestTreeNode)tree.getNode(section);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     * 
     * @param <code>node</code> the node with the removed
     *        section configuration.
     */
    public RemoveSectionAction(GUI frame, TestTreeNode node)
    {
        this.frame = frame;
        this.node = node;
        
        this.section = (SectionConfig)node.getConfig();
    }
    
    /**
     * Executes actions.
     */
    public void execute()
    {
        TestTree testTree = frame.getTestTree();
        
        TestTreeNode node = (TestTreeNode)
            testTree.getSelectionPath().getLastPathComponent();
        
        SectionListConfig parentConfig =
            (SectionListConfig)section.getParent();
        
        parentConfig.removeSection(section);
        
        // remove from history
        frame.getHistory().remove(section);

        // current test has been chaned
        frame.setTestHasChanges();
        
        frame.updateTitle();
        
        updateTree();
        
        updateTable();
        
        /* if remove current section, then show parent */
        if (node.getConfig() == section)
            frame.showConfig(parentConfig);
    }
    
    /**
     * Updates test tree.
     */
    protected void updateTree()
    {
        frame.getTestTree().removeSectionNode(node);
    }
    
    /**
     * Updates table.
     */
    protected void updateTable()
    {
    	Panel panel = frame.getPanel();
    	panel.update();
    }
}
