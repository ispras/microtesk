/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: AddTestAction.java,v 1.13 2009/05/21 17:29:11 kamkin Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import com.unitesk.testfusion.core.config.TestSuiteConfig;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.action.OpenTestAction;
import com.unitesk.testfusion.gui.action.SwitchWorkspaceAction;
import com.unitesk.testfusion.gui.dialog.*;
import com.unitesk.testfusion.gui.panel.table.*;
import com.unitesk.testfusion.gui.panel.table.utils.InputOutputUtils;
import com.unitesk.testfusion.gui.textfield.NonEmptyTextField;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class AddTestAction extends AbstractTableAction
{
	public final static String SET_TEST_TITLE   = "set name for new test";
	public final static String SET_TEST_MESSAGE = "Test name";
	
	public final static String CONFIRM_DIALOG_TITLE   = "confirm test rewrite";
	public final static String CONFIRM_DIALOG_MESSAGE = "Test with equal name exists, rewrite?";
	
	public final static String WORKSPACE_UNDEFINED_TITLE   = "workspace is undefined";
	public final static String WORKSPACE_UNDEFINED_MESSAGE = "Workspace is undefined. To create test should to define it.";
	
	protected EnterNameDialog testNameDialog;
	
	public AddTestAction(TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
	}

	public void executeAction() 
	{
		GUI frame = panel.getFrame();
		TestSuiteConfig testSuite = frame.getTestSuite();

		// Workspace not defined
		if(testSuite.isUndefined())
		{
			frame.showWarningMessage(WORKSPACE_UNDEFINED_MESSAGE, WORKSPACE_UNDEFINED_TITLE);
			
			return;
		}
			
		testNameDialog = new EnterNameDialog(panel.getFrame(), SET_TEST_TITLE, SET_TEST_MESSAGE)
		{
			private static final long serialVersionUID = 0L;

			public void actionsInOkButtonListener()
			{
				handleOkButton();
				
				dispose();
			}
		};
		
		// Set dialog visible
		testNameDialog.setVisible(true);
	}
	
	public void handleOkButton()
	{
		GUI frame = panel.getFrame();
		
		NonEmptyTextField textfield = testNameDialog.getTextField();
		
		String testName = textfield.getText();

		String testNames[] = InputOutputUtils.getWorkspaceTests(frame);
		
		for(int i = 0; i < testNames.length; i++)
		{
			// That name already exists
			if(testName.equals(testNames[i]))
			{
				testNameDialog.dispose();
				
				int confirm = frame.showConfirmYesNoWarningDialog(CONFIRM_DIALOG_MESSAGE, CONFIRM_DIALOG_TITLE);
				
				// Rename not confirmed
				if(confirm == JOptionPane.NO_OPTION)
					{ return; }
			}
		}

		// Change config and settings for frame
		ConfigDialogs.createNewTest(frame);

		TestSuiteConfig testSuite = frame.getTestSuite();
		String testSuitePath = testSuite.toString();
		
		String filename = testSuitePath + "/" + testName + "." + GUI.EXTENSION;
		File file = new File(filename);
		
		// Write config into file
		ConfigDialogs.writeFile(frame, file, !testSuite.isDefined());

		// Update workspace
		SwitchWorkspaceAction.updateSelectedWorkspace(panel.getFrame());
		
		testNameDialog.dispose();
		
    	OpenTestAction openTestAction = new OpenTestAction(frame);
    	// Update configuration for tree
    	openTestAction.updateConfig();
	}
}