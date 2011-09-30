/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SingleTemplateIteratorRenderer.java,v 1.1 2008/09/29 09:30:47 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.renderer;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.unitesk.testfusion.gui.ColorManager;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SingleTemplateIteratorRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 0L;
	
    public Component getTableCellRendererComponent(
            JTable table, Object color,
            boolean isSelected, boolean hasFocus,
            int row, int column) 
    {
    	Component cell = super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, column);
    	
    	if(!isSelected)
    		{ cell.setBackground(ColorManager.DEFAULT_INSTRUCTION_COLOR); }
    	
    	return cell;
    }
}
