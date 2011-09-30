/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SelectEquivalenceAction.java,v 1.6 2008/09/01 12:33:20 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import java.util.Set;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.panel.table.TableModel;
import com.unitesk.testfusion.gui.panel.table.TablePanel;
import com.unitesk.testfusion.gui.panel.table.utils.SelectionUtils;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SelectEquivalenceAction extends AbstractTableAction 
{
	public SelectEquivalenceAction(TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
	}

	public void executeAction() 
	{
		int selectedRows[] = table.getSelectedRows(); 

		if(selectedRows.length == 1)
		{
			// Get model index from sorter 
    		int modelRow = table.getRowSorter().convertRowIndexToModel(selectedRows[0]);  
			
			Config config = model.getConfigValue(modelRow);
			
			if(config instanceof GroupConfig)
			{
				GroupConfig groupConfig = (GroupConfig)config;
				
				Set<String> classes = groupConfig.getEquivalenceClasses();
				
				if(classes.size() == 1)	
				{ 
					Object[] classesArray = classes.toArray();
					String className = (String)classesArray[0]; 
					
					SelectionUtils.selectRows_equivalenceClass(model, table, className);
				}
				
				return;
			}
			else if(config instanceof InstructionConfig)
			{
				InstructionConfig instructionConfig = (InstructionConfig)config;
				
				String className = instructionConfig.getEquivalenceClass();
				
				SelectionUtils.selectRows_equivalenceClass(model, table, className);
				
				return;
			}
		}
		
		throw new IllegalStateException("Too many selected rows");
	}

	public boolean isEnabledAction() 
	{
		if(!model.hasColumnValue(TableModel.EQUIVALENCE_COLUMN))
			{ return false; }
		
		int selectedRows[] = table.getSelectedRows(); 
		
		if(selectedRows.length == 1)
		{
			// Get model index from sorter 
    		int modelRow = table.getRowSorter().convertRowIndexToModel(selectedRows[0]);  
			
			Config config = model.getConfigValue(modelRow);
			
			if(config instanceof GroupConfig)
			{
				GroupConfig groupConfig = (GroupConfig)config;
				
				Set<String> classes = groupConfig.getEquivalenceClasses();
				
				if(classes.size() == 1)	{ return true; }
			}
			else if(config instanceof InstructionConfig)
			{
				InstructionConfig instructionConfig = (InstructionConfig)config;
				
				String instructionClass = instructionConfig.getEquivalenceClass();
				
				if(instructionClass != null && !instructionClass.equals(TableModel.SINGLE_EQUIVALENCE_CLASS))
					{ return true; }
			}
		}
		
		return false;
	}
}
