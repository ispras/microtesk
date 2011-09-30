/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ChangingComboboxEditor.java,v 1.8 2008/09/26 13:23:50 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.combobox;

import java.awt.Component;

import javax.swing.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class ChangingComboboxEditor extends DefaultCellEditor
{
	private static final long serialVersionUID = 0L;

	protected ChangingCombobox combobox;

	public boolean isEditing;

	public ChangingComboboxEditor(ChangingCombobox combobox) 
	{
		super(combobox);
		this.combobox = combobox;
	}

    // This method is called when editing is started
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
    {
    	Component cell = super.getTableCellEditorComponent(table, value, isSelected, row, column);

    	return cell;
    }
    
    // This method is called when editing is completed
    public Object getCellEditorValue() 
    {
		JTextField textfield = (JTextField)combobox.getEditor().getEditorComponent();    	

    	if(isEditing)
    	{
	    	isEditing = false;
	    	
	    	return textfield.getText();
    	}

		// When editCellAt calls from update
		// it need to avoid from setting of new value 
		return combobox.popupListener.oldValue; 
    }
}