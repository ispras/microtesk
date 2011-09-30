/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestNothingAction.java,v 1.7 2008/09/01 12:33:20 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import javax.swing.JTable;

import com.unitesk.testfusion.gui.panel.table.TableModel;
import com.unitesk.testfusion.gui.panel.table.TablePanel;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TestNothingAction extends AbstractTableAction 
{
	public TestNothingAction(TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
	}

	public void executeAction() 
	{
		for(int i = 0; i < model.getRowCount(); i++)
			{ model.setRealValueAt(false, i, TableModel.TEST_COLUMN ); }
		
		updateColumnConfig(TableModel.TEST_COLUMN);
		
    	// Update objects that dependence from column test
    	panel.updateTestDependenceObjects();
	}

	public boolean isEnabledAction() 
	{
		if(model == null || table == null)
			{ throw new IllegalStateException("model or table is null"); }
		
		return model.hasColumnValue(TableModel.TEST_COLUMN) && table.getRowCount() > 0;
	}
}
