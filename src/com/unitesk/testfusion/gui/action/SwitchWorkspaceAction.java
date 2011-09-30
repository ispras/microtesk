/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SwitchWorkspaceAction.java,v 1.10 2008/08/29 11:27:48 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.ConfigDialogs;
import com.unitesk.testfusion.gui.panel.table.utils.InputOutputUtils;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SwitchWorkspaceAction implements ActionListener 
{
	protected GUI frame;
	protected boolean isUndefinedWorkspace; 
    
    public SwitchWorkspaceAction(GUI frame)
    {
        this.frame = frame;
        isUndefinedWorkspace = frame.getTestSuite().isUndefined();
    }
    
    public void actionPerformed(ActionEvent event)
    {
        int returnValue = ConfigDialogs.switchWorkspace(frame);
        
        if (returnValue == ConfigDialogs.WORKSPACE_SELECTED)
        {
        	updateSelectedWorkspace(frame);
        	
        	if (isUndefinedWorkspace)
    			{ frame.enableOpenTestAction(true); }
        }
    }
    
    public static void updateWorkspaceConfiguration(GUI frame)
    {
    	TestSuiteConfig testSuiteConfig = frame.getTestSuite();
    	
    	if(testSuiteConfig == null) { return; }
    	
    	String childrens[] = InputOutputUtils.getWorkspaceTests(frame);
        
        // Clear TestSuiteConfig
        for(int k = testSuiteConfig.countTest() - 1; k >= 0; k--)
        {
        	TestConfig testConfig = testSuiteConfig.getTest(k);
        	testSuiteConfig.removeTest(testConfig);
        }
        
        if(testSuiteConfig.countTest() != 0)
        	{ throw new IllegalStateException("Test count is not 0 after clear testSuiteConfig"); }
        
        if(childrens != null) 
        {
        	// Sort in lexicographical order
        	Arrays.sort(childrens);
        	
            for (int i = 0; i < childrens.length; i++) 
            {
                String filename = childrens[i];
               	testSuiteConfig.registerTest(new TestConfig(filename));
            }
        }    	
    } 
    
    public static void updateSelectedWorkspace(GUI frame)
    {
    	TestSuiteConfig testSuiteConfig = frame.getTestSuite();
    
    	updateWorkspaceConfiguration(frame);
        
        // Show TestSuite
    	frame.getPanel().showPanel(testSuiteConfig);
    }
}