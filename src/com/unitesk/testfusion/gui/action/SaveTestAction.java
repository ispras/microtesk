/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SaveTestAction.java,v 1.5 2008/08/29 10:18:22 kozlov Exp $
 */

package com.unitesk.testfusion.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.ConfigDialogs;
import com.unitesk.testfusion.gui.tree.test.TestTree;
import com.unitesk.testfusion.gui.tree.test.TestTreeNode;

/**
 * Contains actions, which are implemented when test is saved.
 *  
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class SaveTestAction implements ActionListener 
{
    /** GUI frame. */
	protected GUI frame;
    
    /** Is "Save As" action. */
	protected boolean isSaveAs;
	
    /** Is cureent workspace undefined */
	protected boolean isWorkspaceUndefined;
	
    /**
     * Base construcor.
     * 
     * @param <code>frame</code> GUI frame.
     * 
     * @param <code>isSaveAs</code> is "Save As" action. 
     */
	public SaveTestAction(GUI frame, boolean isSaveAs)
	{
		this.frame = frame;
		this.isSaveAs = isSaveAs;
		isWorkspaceUndefined = frame.getTestSuite().isUndefined();
	}
	
	public void actionPerformed(ActionEvent event)
    {
		int returnValue = ConfigDialogs.queryToWriteFile(frame, isSaveAs);
		if (returnValue == ConfigDialogs.FILE_HAS_WRITTEN)
		{
            updateConfig();
		}
    }
    
    /**
     * Updates configuration and GUI.
     */
    public void updateConfig()
    {
        // changes have been saved
        frame.setTestHasChanges(false);
        frame.updateTitle();
        
        if (isWorkspaceUndefined)
            { frame.enableOpenTestAction(true); }
        
        TestTree testTree = frame.getTestTree();
        
        testTree.renameSectionNode((TestTreeNode)testTree.getRoot());
            
        frame.showConfig(frame.getSection() == null ? 
                frame.getConfig() : frame.getSection());
    }
}
