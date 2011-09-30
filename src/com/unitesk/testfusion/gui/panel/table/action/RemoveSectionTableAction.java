/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: RemoveSectionTableAction.java,v 1.4 2008/09/01 12:33:20 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import java.util.Arrays;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.action.RemoveSectionAction;
import com.unitesk.testfusion.gui.panel.table.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class RemoveSectionTableAction extends AbstractTableAction
{
	public RemoveSectionTableAction(TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
	}

	public void executeAction() 
	{
		Config config = panel.getConfig();
		
        JTable table = panel.getTable();
        
        int selectedRows[] = table.getSelectedRows();

        SectionConfig removingSection;

        int sortedRows[] = new int[selectedRows.length];
        
        for(int j = 0; j < selectedRows.length; j++)
        {
        	sortedRows[j] = table.getRowSorter().convertRowIndexToModel(selectedRows[j]);
        }
        
        // Sort into ascending order
        Arrays.sort(sortedRows);
        
        // Remove Sections from configuration
        for(int i = sortedRows.length - 1; i >= 0; i--)
        {
        	int removingNumber = sortedRows[i];
        	
        	removingSection = (SectionConfig)model.getConfigValue(removingNumber);

        	if(config instanceof TestConfig || config instanceof SectionConfig)
	        {
	    		RemoveSectionAction action = new RemoveSectionAction(panel.getFrame(), removingSection);
	    		action.execute();
	        }
	        else
	        	{ throw new IllegalStateException("config is not instance of SectionConfig or TestConfig: " + removingSection.getClass().toString()); }
        }
	}
	
	public boolean isEnabledAction()
	{
		return super.isEnabledAction() && (table.getSelectedRowCount() > 0);
	}
}
