/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestSelectionConfigAction.java,v 1.3 2009/07/09 14:48:12 kamkin Exp $
 */

package com.unitesk.testfusion.gui.action;

import java.util.HashSet;

import com.unitesk.testfusion.core.config.InstructionConfig;
import com.unitesk.testfusion.core.config.SelectionConfig;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.Panel;
import com.unitesk.testfusion.gui.tree.section.SectionTree;
import com.unitesk.testfusion.gui.tree.section.SectionTreeNode;

/**
 * Contains actions, which are implemented when some selection
 * configuration was selected for testing.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class TestSelectionConfigAction 
{
    /** GUI frame. */
    protected GUI frame;
    
    /** Configuration, wich was changed. */
    protected SelectionConfig config;
    
    /** Node in section tree with specified configuration. */
    protected SectionTreeNode node;
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     * 
     * @param <code>config</code> the changed configuration.
     */
    public TestSelectionConfigAction(GUI frame, SelectionConfig config)
    {
        this.frame = frame;
        this.config = config;
        
        SectionTree tree = frame.getSectionTree();
        
        this.node = (SectionTreeNode)tree.getNode(config);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     * 
     * @param <code>node</code> the node with changed configuration.
     */
    public TestSelectionConfigAction(GUI frame, SectionTreeNode node)
    {
        this.frame = frame;
        this.node = node;
        this.config = node.getConfig();
    }
    
    /**
     * Executes actions.
     */
    public void execute()
    {
        if(!node.isEnabled())
            { return; }
            
    	// Clear position set
        if(config instanceof InstructionConfig && config.isSelected())
        {
        	InstructionConfig instructionConfig = (InstructionConfig)config; 
        	HashSet<Integer> positions = instructionConfig.getPositions();
        	
        	positions.clear();
        }

        // Inverse selection
    	config.setSelectedWithPropagation(!config.isSelected());
        
        // Test has been changed
        frame.setTestHasChanges();
        frame.updateTitle();
        
        updateTree();
        
        updateTable();
    }
    
    /**
     * Updates test tree.
     */
    protected void updateTree()
    {
        SectionTree tree = frame.getSectionTree();
        
        tree.testNode(node);
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