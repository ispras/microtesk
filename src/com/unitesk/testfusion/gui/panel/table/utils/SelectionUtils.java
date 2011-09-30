/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SelectionUtils.java,v 1.3 2008/08/13 10:50:26 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.utils;

import java.util.Set;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.panel.table.TableModel;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SelectionUtils 
{
	// Function that define if nodes of all selected nodes are empty
	public static boolean isAllSelectedRowsEmpty(TableModel model)
	{
		JTable table = model.table;
		
    	int selectedRows[] = table.getSelectedRows();
    	
    	Config config;
    	
    	for(int i = 0; i < selectedRows.length; i++)
    	{
    		int rowNumber = table.getRowSorter().convertRowIndexToModel(selectedRows[i]);

    		// Get configuration of row
    		config = model.getConfigValue(rowNumber);
    		
    		if(!config.isEmpty()) { return false; }
    	}
    	
    	return true;
	}
	
	// Function that make selection of all rows for current panel, 
	// which has equal equivalence class as argument
	public static void selectRows_equivalenceClass(TableModel model, JTable table, String className)
	{
		boolean isFirstSelection = true; 
		ListSelectionModel selectionModel = table.getSelectionModel();
		
		for(int i = 0; i < model.getRowCount(); i++)
		{
			// Get model index from sorter 
    		int modelRow = table.getRowSorter().convertRowIndexToModel(i);  
			
			Config rowConfig = model.getConfigValue(modelRow);

			if(rowConfig instanceof GroupConfig)
			{
				GroupConfig groupConfig = (GroupConfig)rowConfig;
				
				Set<String> rowClasses = groupConfig.getEquivalenceClasses();
				
				if(rowClasses.size() == 1 && rowClasses.contains(className))
				{
					// Select this row
					if(isFirstSelection)
					{
						selectionModel.setSelectionInterval(i, i);
						isFirstSelection = false;
					}
					else
						{ selectionModel.addSelectionInterval(i, i); }
				}
			}
			else if(rowConfig instanceof InstructionConfig)
			{
				InstructionConfig instructionConfig = (InstructionConfig)rowConfig;

				String instructionClass = instructionConfig.getEquivalenceClass(); 
				
				if(	instructionClass != null && 
					instructionClass.equals(className) )
				{
					// Select this row
					if(isFirstSelection)
					{
						selectionModel.setSelectionInterval(i, i);
						isFirstSelection = false;
					}
					else
						{ selectionModel.addSelectionInterval(i, i); }
				}
			}
		}
	}
}
