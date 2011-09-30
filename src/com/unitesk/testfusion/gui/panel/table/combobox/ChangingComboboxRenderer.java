/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ChangingComboboxRenderer.java,v 1.3 2008/09/26 13:23:50 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.combobox;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class ChangingComboboxRenderer extends DefaultTableCellRenderer
{
	public final Color acceptColor = Color.WHITE;
	public final Color rejectColor = Color.PINK;
	
	private static final long serialVersionUID = 1L;
	
	protected ChangingCombobox combobox;
	
	public ChangingComboboxRenderer(ChangingCombobox combobox)
	{
		this.combobox = combobox;
	}
	
    public Component getTableCellRendererComponent(JTable table, Object value,
    		                                       boolean isSelected, boolean hasFocus,
    		                                       int row, int column) 
    {
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    
		if(!combobox.isTextfieldItemAvailable())
			{ cell.setBackground(rejectColor); }
		
    	return cell;
    }
}