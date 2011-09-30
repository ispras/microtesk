/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: AddAction.java,v 1.2 2008/10/01 14:17:43 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.action;

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import com.unitesk.testfusion.gui.dialog.options.*;
import com.unitesk.testfusion.gui.panel.table.combobox.ComboboxDocumentFilter;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class AddAction extends SingleTemplateIteratorAction
{
	public AddAction(SingleTemplateIteratorTable table, 
			                    DefaultTableModel model, 
			                    JPanel mainPanel) 
	{
		super(table, model, mainPanel);
	}

	public void execute() 
	{
		int maxPosition = 0;
		
		// Get max position
		for(int i = 0; i < table.getRowCount(); i++)
		{
			int position = 0;
			
			try
			{
				position = Integer.parseInt((String)model.getValueAt(i, SingleTemplateIteratorDialog.POSITION_COLUMN));
			}
			catch(NumberFormatException exception)
			{
				continue;
			}
			
			if(position > maxPosition) { maxPosition = position; }
		}

		Object row[] = new Object[2];
		
		// Set position of new row
		row[0] = Integer.toString(maxPosition + 1);

		// Add row
		model.addRow(row);
		
		// Create filter for new row
		table.getCellEditor(table.getRowCount() - 1, SingleTemplateIteratorDialog.INSTRUCTION_COLUMN);					
		
		ComboboxDocumentFilter filter = table.filterMap.get(table.getRowCount() - 1);

		// Disable document listeners
		filter.disableListeners();
		
		// Update combobox
		filter.updateCombobox("");

		// Enable document listeners
		filter.enableListeners();
		
		table.repaint();
		table.revalidate();
	}

	public boolean isEnabled() 
	{
		return true;
	}
}