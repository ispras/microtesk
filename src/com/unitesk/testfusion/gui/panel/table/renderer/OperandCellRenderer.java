/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: OperandCellRenderer.java,v 1.6 2008/07/07 10:27:01 protsenko Exp $
 */

package com.unitesk.testfusion.gui.panel.table.renderer;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.unitesk.testfusion.gui.ColorManager;
import com.unitesk.testfusion.gui.panel.table.OperandTableModel;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class OperandCellRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 0L;

    OperandTableModel model;
    
    Boolean[][] data;
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		       boolean hasFocus, int row, int column) 
	{
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        model = (OperandTableModel)table.getModel();
        
        cell.setBackground(ColorManager.DEFAULT_OPERAND_COLOR);
        
        if(data[row][0])
        {
            cell.setFont(new Font("Tahoma", Font.ITALIC, 11));
        }

		return this;
	}
    
    public OperandCellRenderer(Boolean[][] data)
    {
        super();
        
        this.data = data;
    }
}
