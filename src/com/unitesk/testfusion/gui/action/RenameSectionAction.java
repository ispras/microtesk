/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: RenameSectionAction.java,v 1.3 2008/08/29 13:37:49 kozlov Exp $
 */

package com.unitesk.testfusion.gui.action;

import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.core.config.SectionListConfig;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.Panel;
import com.unitesk.testfusion.gui.tree.test.TestTree;
import com.unitesk.testfusion.gui.tree.test.TestTreeNode;

/**
 * Contains actions, which are implemented when section is renamed.
 *   
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class RenameSectionAction
{
    /** GUI frame. */
    protected GUI frame;
    
    /** Removed section. */
    protected SectionConfig section; 
    
    /** Node in test tree with renamed section configuration. */
    protected TestTreeNode node;
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     *  
     * @param <code>section</code> the renamed section.
     */
    public RenameSectionAction(GUI frame, SectionConfig section)
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
     * @param <code>node</code> the node with the renamed
     *        section configuration.
     */
    public RenameSectionAction(GUI frame, TestTreeNode node)
    {
        this.frame = frame;
        this.section = (SectionConfig)node.getConfig();
        this.node = node;
    }
    
    /**
     * Executes actions.
     */
    public void execute()
    {
        frame.showSectionNameDialog(section, 
                (SectionListConfig)section.getParent());
        
        // current test has been chaned
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
        frame.getTestTree().renameSectionNode(node);
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
