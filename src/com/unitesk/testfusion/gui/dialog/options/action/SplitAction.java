/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SplitAction.java,v 1.2 2008/09/20 12:07:23 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.action;

import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import com.unitesk.testfusion.gui.dialog.options.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SplitAction extends SingleTemplateIteratorAction
{
	public SplitAction(SingleTemplateIteratorTable table, DefaultTableModel model, JPanel mainPanel) 
	{
		super(table, model, mainPanel);
	}

	public void execute()
	{
		int selectedRows[] = table.getSelectedRows();
		
		Arrays.sort(selectedRows);
		
		int lastRow = selectedRows[selectedRows.length - 1]; 
		int minSelectedRow = selectedRows[0];
		
		int position = Integer.parseInt((String)model.getValueAt(minSelectedRow, SingleTemplateIteratorDialog.POSITION_COLUMN));

		try
		{
			if((selectedRows[0] != 0) && (Integer.parseInt((String)model.getValueAt(selectedRows[0],     SingleTemplateIteratorDialog.POSITION_COLUMN)) == 
                                          Integer.parseInt((String)model.getValueAt(selectedRows[0] - 1, SingleTemplateIteratorDialog.POSITION_COLUMN))) )
				{ position++; }
		}
		catch(NumberFormatException e) { }
		
		
		// Split selected rows
		for(int i = 0; i < selectedRows.length; i++)
		{
			model.setValueAt(Integer.toString(position), selectedRows[i], SingleTemplateIteratorDialog.POSITION_COLUMN);
			
			position++;
		}
		
		// If last selected row is not last row in the table
		if(lastRow != table.getRowCount() - 1)
		{
			int delta = position - Integer.parseInt((String)model.getValueAt(lastRow + 1, SingleTemplateIteratorDialog.POSITION_COLUMN));

			for(int j = lastRow + 1; j < table.getRowCount(); j++)
			{
				int currentPosition = Integer.parseInt((String)model.getValueAt(j, SingleTemplateIteratorDialog.POSITION_COLUMN) );
				model.setValueAt(Integer.toString(currentPosition + delta), j, SingleTemplateIteratorDialog.POSITION_COLUMN);
			}
		}
	}

	public boolean isEnabled() 
	{	
		int selectedRows[] = table.getSelectedRows();
	
		// Flag means that rows selected serially
		if(selectedRows.length >= 2)
		{
			// Sort Array
			Arrays.sort(selectedRows);

			for(int i = 0; i < selectedRows.length - 1; i++)
			{
				// Rows should be selected serially
				if(selectedRows[i] != selectedRows[i + 1] - 1)
					{ return false;	}
				
				try
				{
					int firstRowPosition  = Integer.parseInt((String)model.getValueAt(selectedRows[i],     SingleTemplateIteratorDialog.POSITION_COLUMN));
					int secondRowPosition = Integer.parseInt((String)model.getValueAt(selectedRows[i + 1], SingleTemplateIteratorDialog.POSITION_COLUMN));
	
					// All selected rows should have equal positions
					if(firstRowPosition != secondRowPosition)
						{ return false;	}
				}
				catch(NumberFormatException exception)
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
}