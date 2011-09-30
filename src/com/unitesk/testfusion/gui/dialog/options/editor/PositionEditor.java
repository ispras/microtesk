/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: PositionEditor.java,v 1.1 2008/09/29 09:30:46 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.editor;

import java.awt.Component;

import javax.swing.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class PositionEditor extends DefaultCellEditor
{
	private static final long serialVersionUID = 0L;
	
	protected PositionTextField textfield;

	public PositionEditor(PositionTextField textfield) 
	{
		super(textfield);
	
		this.textfield = textfield;
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
    	return textfield.getText();
    }
}