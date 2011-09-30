/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestAction.java,v 1.21 2009/05/12 11:22:06 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.SelectionConfig;
import com.unitesk.testfusion.gui.action.TestSelectionConfigAction;
import com.unitesk.testfusion.gui.panel.table.*;
import com.unitesk.testfusion.gui.panel.table.utils.SelectionUtils;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TestAction extends AbstractTableAction
{
    public TestAction(TablePanel panel, TableModel model, JTable table) 
    {
		super(panel, model, table);
	}

    public void executeAction()
    {
    	int selectedRows[] = table.getSelectedRows();
    	
    	boolean allRowsSelected = true;
    	
    	for(int i = 0; i < selectedRows.length; ++i)
    	{
    		// Get model index from sorter 
    		int modelRow = table.getRowSorter().convertRowIndexToModel(selectedRows[i]);

    		Boolean checked = (Boolean) model.getRealValueAt(modelRow, TableModel.TEST_COLUMN) ;
    	
    		if(!checked) { allRowsSelected = false; break; }
    	}
    	
    	for(int j = 0; j < selectedRows.length; ++j)
    	{
    		// Get model index from sorter 
    		int modelRow = table.getRowSorter().convertRowIndexToModel(selectedRows[j]);
    	
    		// Get configurtion of the row
    		SelectionConfig rowConfig = (SelectionConfig)model.getConfigValue(modelRow);

    		if((!allRowsSelected && !rowConfig.isSelected()) || allRowsSelected)
    		{
	    		TestSelectionConfigAction testSelectionAction = new TestSelectionConfigAction(panel.getFrame(), (SelectionConfig)rowConfig); 
	    		testSelectionAction.execute();
    		}
    	}
    }
    
    public boolean isEnabledAction()
    {
    	return model.hasColumnValue(TableModel.TEST_COLUMN) && 
    	      (table.getSelectedRowCount() > 0) && 
    	      !SelectionUtils.isAllSelectedRowsEmpty(model);
    }
}