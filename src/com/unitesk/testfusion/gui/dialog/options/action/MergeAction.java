/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: MergeAction.java,v 1.1 2008/09/20 11:43:49 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.action;

import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import com.unitesk.testfusion.gui.dialog.options.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class MergeAction extends SingleTemplateIteratorAction
{
	public MergeAction(SingleTemplateIteratorTable table, DefaultTableModel model, JPanel mainPanel) 
	{
		super(table, model, mainPanel);
	}

	public void execute() 
	{
		int selectedRows[] = table.getSelectedRows();
		
		Arrays.sort(selectedRows);

		// Get min number
		int firstRow = selectedRows[0];		
		int firstRowPosition = Integer.parseInt((String)model.getValueAt(firstRow, SingleTemplateIteratorDialog.POSITION_COLUMN) );
		
		int lastRow = selectedRows[selectedRows.length - 1]; 
		int lastRowPosition = Integer.parseInt((String)model.getValueAt(lastRow, SingleTemplateIteratorDialog.POSITION_COLUMN) );

		// Get delta positions
		int delta = lastRowPosition - firstRowPosition; 

		if(lastRow < model.getRowCount() - 1)
		{
			if(lastRowPosition == Integer.parseInt((String)model.getValueAt(lastRow + 1, SingleTemplateIteratorDialog.POSITION_COLUMN)) )
				{ delta = 0; }
		}
		
		// Update selected rows 
		// TODO: update configuration at first
		for(int i = 0; i < selectedRows.length; i++)
			{ model.setValueAt(Integer.toString(firstRowPosition), selectedRows[i], SingleTemplateIteratorDialog.POSITION_COLUMN); }

		// If last selected row is not last row in table
		if(lastRow < model.getRowCount() - 1)
		{
			for(int j = lastRow + 1; j < model.getRowCount(); j++)
			{
				int value = Integer.parseInt((String)model.getValueAt(j, SingleTemplateIteratorDialog.POSITION_COLUMN)) - delta;
				
				model.setValueAt(Integer.toString(value), j, SingleTemplateIteratorDialog.POSITION_COLUMN);
			}
		
		}
		// TODO: change configuration against
		//       update table
	}

	public boolean isEnabled() 
	{
		int selectedRows[] = table.getSelectedRows();
		
		// Flag means that rows selected serial
		if(selectedRows.length >= 2)
		{
			// Sort Array
			Arrays.sort(selectedRows);

			for(int i = 0; i < selectedRows.length - 1; i++)
			{
				String position = (String)model.getValueAt(selectedRows[i], SingleTemplateIteratorDialog.POSITION_COLUMN); 
				
				if(position.equals(SingleTemplateIteratorDialog.ALL_POSITIONS))
					{ return false;	}
				
				if(selectedRows[i] != selectedRows[i + 1] - 1)
					{ return false;	}
			}

			return true;
		}
		
		return false;
	}
}