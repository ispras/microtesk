/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: AbstractTableAction.java,v 1.11 2009/05/15 16:14:26 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import java.util.HashSet;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.panel.table.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public abstract class AbstractTableAction implements TableAction 
{
	public TableModel model; 
	public JTable table;
	protected TablePanel panel;
	public HashSet<Integer> updateColumnsModel;
	public HashSet<Integer> updateColumnsConfig;

	public void executeAction()
	{
		throw new IllegalStateException("Method executeAction() should be implemented");
	}
	
	public AbstractTableAction(TablePanel panel, TableModel model, JTable table)
	{
		updateColumnsModel  = new HashSet<Integer>();
		updateColumnsConfig = new HashSet<Integer>();
		
		this.panel = panel;
		this.model = model;
		this.table = table;
	}
	
	// Add column to be updated from Configuration
	public void updateColumnModel(int column)
	{
		updateColumnsModel.add(column);
	}
	
	// Add column that update Configuration
	public void updateColumnConfig(int column)
	{
		updateColumnsConfig.add(column);
	}

	public HashSet<Integer> getUpdateColumnsModel()
	{
		return updateColumnsModel;
	}
	
	public HashSet<Integer> getUpdateColumnsConfig()
	{
		return updateColumnsConfig;
	}
	
	public boolean isEnabledAction()
	{
		Config config = panel.getConfig();
		
		if(config == null)
			{ throw new IllegalStateException("Configuration is null"); }

		return true;
	}
	
	public void executeAndUpdate()
	{
		executeAction();
		
		Object[] updateModelColumns = updateColumnsModel.toArray();
		// First handle correspond columns from configuration 
		for(int i = 0; i < updateModelColumns.length; i++)
		{
			int columnNumber = (Integer)updateModelColumns[i];
			// Update columns from configuration
			model.updateColumnModel(columnNumber);
			
			// Update dependence objects from configuration
			panel.updateColumnDependenceObjects(i);
		}
		
		Object[] configColumns = updateColumnsConfig.toArray();
		
		for(int j = 0; j < configColumns.length; j++)
		{
			int columnNumber = (Integer)configColumns[j];
			
			if(columnNumber == TableModel.TEST_COLUMN)
			{
				// Update Configuration from columns
				model.updateColumnConfig();
			}
			
			// Update dependence objects from configuration
			panel.updateColumnDependenceObjects(j);
		}
	}
}
