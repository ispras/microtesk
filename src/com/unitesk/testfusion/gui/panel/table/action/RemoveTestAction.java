/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: RemoveTestAction.java,v 1.6 2008/11/10 12:58:13 kozlov Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import java.io.File;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.panel.table.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class RemoveTestAction extends AbstractTableAction
{

	public RemoveTestAction(TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
	}

	public void executeAction() 
	{
		Config config = panel.getConfig();
		
		if(!(config instanceof TestSuiteConfig))
			{ throw new IllegalStateException("config is not instance of TestSuiteConfig"); }
		
		TestSuiteConfig testSuiteConfig = (TestSuiteConfig)panel.getConfig();

    	String testSuitePath = testSuiteConfig.toString();
    	
        //File dir = new File(testSuitePath);

        int selectedRows[] = table.getSelectedRows();
        
		
        TestConfig configs[] = new TestConfig[selectedRows.length];
		
        // Get configs of all selected rows
		for (int i = 0; i < configs.length; i++)
		{ 
			configs[i] = (TestConfig)model.getConfigValue(selectedRows[table.getRowSorter().convertRowIndexToModel(i)]); 
		}
		
		// Remove configs from configuration and corresponding files from disc
		for(int j = 0; j < selectedRows.length; j++)
		{
			TestConfig testConfig = configs[j];
			String filename = testConfig.toString() + "." + GUI.EXTENSION;  
			File f = new File(testSuitePath + "/" + filename);

			// Remove file from disk			
			if(f.exists() && f.canWrite())
			{ 	
				// Remove Test Config from configuration
				testSuiteConfig.removeTest(testConfig);

				f.delete();
			}
			else { throw new IllegalStateException("File " + f.getAbsolutePath() + " can't be deleted"); }
			
		}
		
		// Update table
        UpdateTableAction action = new UpdateTableAction(panel, model, table);
        action.executeAction();
	}
	
	public boolean isEnabledAction()
	{
		return super.isEnabledAction() && (table.getSelectedRowCount() > 0);
	}
}