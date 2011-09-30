/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: AddSectionAction.java,v 1.10 2008/10/30 11:18:12 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.action;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.Panel;
import com.unitesk.testfusion.gui.tree.test.TestTree;
import com.unitesk.testfusion.gui.tree.test.TestTreeNode;

/**
 * Contains actions, which are implemented when new section is added.
 *   
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class AddSectionAction
{
    /** GUI frame. */
    protected GUI frame;
    
    /** Parent configuration for new added section. */
    protected SectionListConfig parentConfig;
    
    /** Node in test tree with parent configuration. */
    protected TestTreeNode parentNode;
    
    /** New section to be added. */
    protected SectionConfig newSection; 
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     *  
     * @param <code>parentConfig</code> the parent configuration
     *        of the new added section.
     */
    public AddSectionAction(GUI frame, SectionListConfig parentConfig)
    {
        this.frame = frame;
        this.parentConfig = parentConfig;
        
        TestTree tree = frame.getTestTree();
        this.parentNode = (TestTreeNode)tree.getNode(parentConfig);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     * 
     * @param <code>parentNode</code> the parent node of the new section
     *        in test tree.
     */
    public AddSectionAction(GUI frame, TestTreeNode parentNode)
    {
        this.frame = frame;
        this.parentConfig = parentNode.getConfig();
        this.parentNode = parentNode;
    }
    
    /**
     * Executes actions.
     */
    public void execute()
    {
        newSection = frame.getConfig().createSection();
        
        boolean isOkPressed = 
            frame.showSectionNameDialog(newSection, parentConfig);
        
        if (isOkPressed)
        {
            boolean isLeafSection = (parentConfig instanceof SectionConfig) 
                    && ((SectionConfig)parentConfig).isLeaf();
            
            parentConfig.registerSection(newSection);
            
            // current test has been chaned
            frame.setTestHasChanges();
            
            frame.updateTitle();
            
            updateTree();
            
            updateTable();
            
            /* if add to leaf section, then update GUI */
            if (isLeafSection)
                { frame.showConfig(parentConfig); }
        }
    }
    
    /**
     * Updates test tree.
     */
    protected void updateTree()
    {
        frame.getTestTree().addSectionNode(newSection, parentNode);
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
