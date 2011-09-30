/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TableSuitePanelModelListener.java,v 1.8 2008/11/10 12:58:15 kozlov Exp $
 */

package com.unitesk.testfusion.gui.panel.table.listener;

import java.io.File;

import javax.swing.event.*;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.util.Utils;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.action.OpenTestAction;
import com.unitesk.testfusion.gui.panel.table.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TableSuitePanelModelListener extends TablePanelModelListener
{
	public void tableChanged(TableModelEvent e) 
	{
		int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel)e.getSource();
        Object data = model.getValueAt(row, column);

        // Update Equivalence column if it exists
    	if(!model.hasColumnValue(TableModel.NAME_COLUMN) )
    		{ throw new IllegalStateException("Model has no column NAME_COLUMN"); }

    	if(column == model.getColumnKey(TableModel.NAME_COLUMN) )
    	{
        	Config rowConfig = model.getConfigValue(row);
        	
        	if(!(rowConfig instanceof TestConfig))
        		{ throw new IllegalStateException("Row config should be TestConfig"); }
        	
        	TestSuiteConfig testSuiteConfig = (TestSuiteConfig)panel.getConfig();
        	testSuiteConfig.removeTest((TestConfig)rowConfig);
        	
        	String testSuitePath = testSuiteConfig.toString();
        	
        	String newFileName = testSuitePath + "/" + (String)data + "." + GUI.EXTENSION;
        	String oldFileName = testSuitePath + "/" + rowConfig.getName() + "." + GUI.EXTENSION;
        	
        	File newFile = new File(newFileName);

        	File oldFile = new File(oldFileName);

    		GUI frame = panel.getFrame();
    		
        	if(newFile.exists())
        	{
        		// Filename were not changed 
        		if(newFileName.equals(oldFileName)) { return; }

        		String message = "File " + newFileName + " already exists in the workspace";
        		String title = "error";

        		frame.showWarningMessage(message, title);

        		// Set old filename value
            	panel.enableListeners(false, TableModelListener.class);
            	model.setValueAt(rowConfig.getName(), row, column);
            	panel.enableListeners(true, TableModelListener.class);
        	}
        	else
        	{
        		String reason;
        		
        		if(oldFile.exists()) 
        		{
	        		if(oldFile.canWrite())
	        		{
	        			// Rename file
	        			oldFile.renameTo(newFile);
	        			
	        			// Get current test name
	        			String currentTestName = frame.getConfig().toString();
	        			currentTestName = currentTestName.substring(0, currentTestName.length() - 3);;
	        			
	        			// If current test renamed
	        			if(currentTestName.equals(rowConfig.getName()))
	        			{
	        				// Rename config
	        				frame.getConfig().setName(
                                    Utils.removeFileExtention((String)data));
	        		    	
	        				OpenTestAction openTestAction = new OpenTestAction(frame);
	        		    	
	        				// Update configuration for tree
	        		    	openTestAction.updateConfig();
	        			}
	        				
	        			// Rename config in row
	        			rowConfig.setName((String)data);
	        			
	        			return;
	        		}
	        		else
	        			{ reason = " file can't be write"; }
        		}
        		else
        			{ reason = " file not exists"; }

        		String message = "Can't rename " + oldFileName + ":" + reason;
        		String title = "error";
        		
          		frame.showWarningMessage(message, title);
        	}
    	}
	}
}
