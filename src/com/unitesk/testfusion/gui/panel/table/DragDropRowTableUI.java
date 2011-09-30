/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: DragDropRowTableUI.java,v 1.4 2008/09/13 11:16:54 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table;

import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicTableUI;

import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.gui.action.MoveSectionAction;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class DragDropRowTableUI extends BasicTableUI 
{
	protected boolean draggingRow = false;
	protected int startDragPoint;
	protected int dyOffset;
	
	private TablePanel panel;
	
	public DragDropRowTableUI()
	{
	}
	
    public DragDropRowTableUI(TablePanel panel)
    {
    	this.panel = panel;
    }
	
	protected MouseInputListener createMouseInputListener()
    {
        return new DragDropRowMouseInputHandler();
    }
    
    public void paint(Graphics g, JComponent c) 
    {
    	super.paint(g, c);
    	
    	if (draggingRow) 
    	{
    		g.setColor(table.getParent().getBackground());
			Rectangle cellRect = table.getCellRect(table.getSelectedRow(), 0, false);
    		g.copyArea(cellRect.x, cellRect.y, table.getWidth(), table.getRowHeight(), cellRect.x, dyOffset);
    		
    		if (dyOffset < 0) 
    		{
    			g.fillRect(cellRect.x, cellRect.y + (table.getRowHeight() + dyOffset), table.getWidth(), (dyOffset * -1));
    		} 
    		else 
    		{
    			g.fillRect(cellRect.x, cellRect.y, table.getWidth(), dyOffset);
    		}
    	}
    }
    
    class DragDropRowMouseInputHandler extends MouseInputHandler 
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
	        		int realFromRow;
	        		int realToRow;
	        		
	        		// Sorter is available
	        		if(table.getRowSorter() != null)
	        		{
		        		realFromRow = table.getRowSorter().convertRowIndexToModel(fromRow);		
		        		realToRow = table.getRowSorter().convertRowIndexToModel(toRow);
	        		}
	        		else
	        		{
	        			realFromRow = fromRow;
	        			realToRow = toRow;
	        		}
	        		
	        		// Change places of configurations
	        		MoveSectionAction actionUp = new MoveSectionAction(panel.getFrame(), (SectionConfig)panel.getModel().getConfigValue(realFromRow), realToRow);
	        		MoveSectionAction actionDown = new MoveSectionAction(panel.getFrame(), (SectionConfig)panel.getModel().getConfigValue(realToRow), realFromRow);
	        		
	        		// Change rows data
	        		actionUp.execute();
	        		actionDown.execute();

	        		// Update model
	        		panel.getModel().updateModel(panel.getConfig());
	        		
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
 