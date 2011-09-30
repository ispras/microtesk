/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestAllAction.java,v 1.8 2008/09/01 12:33:20 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.core.config.walker.ConfigWalker;
import com.unitesk.testfusion.gui.panel.table.*;
import com.unitesk.testfusion.gui.panel.table.visitor.SetTestVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TestAllAction extends AbstractTableAction 
{
	public TestAllAction(TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
	}

	public void executeAction() 
	{
		for(int i = 0; i < model.getRowCount(); i++)
    	{
    		// Get configurtion of the row
    		Config rowConfig = model.getConfigValue(i);

    		SetTestVisitor visitor = new SetTestVisitor(true);
	    		
        	ConfigWalker walker = new ConfigWalker(rowConfig, visitor, ConfigWalker.VISIT_ALL);    		
        	walker.process();
    	}
    	
    	model.updateColumnModel(TableModel.TEST_COLUMN);
    	
    	// Update objects that dependence from column test
    	panel.updateTestDependenceObjects();
	}

	public boolean isEnabledAction() 
	{
		return model.hasColumnValue(TableModel.TEST_COLUMN) && table.getRowCount() > 0;
	}
}