/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: RemoveAction.java,v 1.4 2008/09/29 14:22:31 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.action;

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import com.unitesk.testfusion.gui.dialog.options.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class RemoveAction extends SingleTemplateIteratorAction
{
	protected SingleTemplateIteratorDialog dialog;
	
	public RemoveAction(SingleTemplateIteratorTable table, 
			                       DefaultTableModel model, 
			                       JPanel mainPanel, 
			                       SingleTemplateIteratorDialog dialog) 
	{
		super(table, model, mainPanel);
		
		this.dialog = dialog;
	}

	public void execute() 
	{
		int selectedRows[] = table.getSelectedRows();
		
		// Remove rows
		for(int i = selectedRows.length - 1; i >= 0; i--)
		{ 
			String position = (String)model.getValueAt(i, SingleTemplateIteratorDialog.POSITION_COLUMN); 

			if(!position.equals(SingleTemplateIteratorDialog.ALL_POSITIONS))
			{
				// Row is not last
				if(selectedRows[i] != model.getRowCount() - 1)
				{
					int nextPosition = Integer.parseInt((String)(model.getValueAt(selectedRows[i] + 1, SingleTemplateIteratorDialog.POSITION_COLUMN)) );
	
					int prevPosition;
	
					// Row is not first
					if(selectedRows[i] != 0 && ((String)model.getValueAt(selectedRows[i] - 1, SingleTemplateIteratorDialog.POSITION_COLUMN) != "*") )
						{ prevPosition = Integer.parseInt((String)(model.getValueAt(selectedRows[i] - 1, SingleTemplateIteratorDialog.POSITION_COLUMN)) ); }
					else
						{ prevPosition = 0;	}
	
					if(prevPosition == nextPosition - 2)
					{
						for(int j = selectedRows[i] + 1; j < model.getRowCount(); j++)
						{
							try
							{
								int tempPosition = Integer.parseInt((String)model.getValueAt(j, SingleTemplateIteratorDialog.POSITION_COLUMN));
								model.setValueAt(Integer.toString(tempPosition - 1), j, SingleTemplateIteratorDialog.POSITION_COLUMN);
							}
							catch(NumberFormatException exception)
							{
								// Do nothing
							}
						}
					}
				}
			}
			
			// Remove row
			model.removeRow(selectedRows[i]);
		}

		table.revalidate();
		table.repaint();
	}

	public boolean isEnabled() 
	{
		int selectedRows[] = table.getSelectedRows();
		
		// Flag means that rows selected serial
		return selectedRows.length > 0;
	}
}