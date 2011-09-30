/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: NewTestAction.java,v 1.10 2008/08/29 10:18:22 kozlov Exp $
 */

package com.unitesk.testfusion.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.ConfigDialogs;

/**
 * Contains actions, which are implemented when new test is created.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class NewTestAction implements ActionListener 
{
    /** GUI frame. */
	protected GUI frame;
	
    /**
     * Base constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     */
	public NewTestAction(GUI frame)
	{
		this.frame = frame;
	}
	
	public void actionPerformed(ActionEvent event)
    {
		int returnValue = ConfigDialogs.queryToNewTest(frame);
		if (returnValue == ConfigDialogs.TEST_HAS_CREATED)
		{
            updateConfig();
		}
    }
    
    /**
     * Updates configuration and GUI.
     */
    public void updateConfig()
    {
        frame.setTestHasChanges(false);
        
        // clear history
        frame.getHistory().clear();
        
        frame.getTestTree().updateConfig();
        
        // show the first leaf of the test tree
        frame.showConfig(frame.getConfig().getFirstLeaf());
    }
}
