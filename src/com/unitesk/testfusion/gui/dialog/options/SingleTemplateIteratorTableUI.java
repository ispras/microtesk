/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SingleTemplateIteratorTableUI.java,v 1.3 2008/09/22 11:15:09 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputListener;
import javax.swing.table.DefaultTableModel;

import com.unitesk.testfusion.gui.panel.table.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SingleTemplateIteratorTableUI extends DragDropRowTableUI 
{
	protected DefaultTableModel model;
	
	public SingleTemplateIteratorTableUI(DefaultTableModel model) 
	{
		super();
		
		this.model = model;
	}

	protected MouseInputListener createMouseInputListener()
    {
        return new SingleTemplateIteratorMouseHandler();
    }
	
	public class SingleTemplateIteratorMouseHandler extends MouseInputHandler
	{
        public void mousePressed(MouseEvent e) 
        {
        	super.mousePressed(e);
        	startDragPoint = (int)e.getPoint().getY();
        }
        
        public void mouseDragged(MouseEvent e) 
        {
        	int fromRow = table.getSelectedRow();
        	
        	if (fromRow >= 0) 
        	{
        		// Cancel editing if row were dragged
        		if(table.isEditing())
        			{ table.getCellEditor().cancelCellEditing(); }
        			
	        	draggingRow = true;
	        	        	
	        	int rowHeight = table.getRowHeight();
	        	int middleOfSelectedRow = (rowHeight * fromRow) + (rowHeight / 2);
	        	
	        	int toRow = -1;
	        	int yMousePoint = (int)e.getPoint().getY();
	        	        	
	        	if (yMousePoint < (middleOfSelectedRow - rowHeight)) 
	        	{
	        		// Move row up
	        		toRow = fromRow - 1;
	        	} 
	        	else if (yMousePoint > (middleOfSelectedRow + rowHeight)) 
	        	{
	        		// Move row down
	        		toRow = fromRow + 1;
	        	}
	        	
	        	if (toRow >= 0 && toRow < table.getRowCount()) 
	        	{
	        		// TODO: change positions

	        		String firstRowInstruction  = (String)(model.getValueAt(toRow,   SingleTemplateIteratorDialog.INSTRUCTION_COLUMN));
	        		String secondRowInstruction = (String)(model.getValueAt(fromRow, SingleTemplateIteratorDialog.INSTRUCTION_COLUMN));
	        		
	        		// Change places of instructions
	        		model.setValueAt(secondRowInstruction, toRow,   SingleTemplateIteratorDialog.INSTRUCTION_COLUMN);
	        		model.setValueAt(firstRowInstruction,  fromRow, SingleTemplateIteratorDialog.INSTRUCTION_COLUMN);
	        		
		    		table.setRowSelectionInterval(toRow, toRow);
		    		startDragPoint = yMousePoint;
		    		
	        	}
	        	
	        	dyOffset = (startDragPoint - yMousePoint) * -1;
	        	table.repaint();
        	}
        }
        
        public void mouseReleased(MouseEvent e)
        {
        	super.mouseReleased(e);
        	
        	draggingRow = false;
        	table.repaint();
        }
	}
}
