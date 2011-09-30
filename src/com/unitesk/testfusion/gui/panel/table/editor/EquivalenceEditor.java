/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: EquivalenceEditor.java,v 1.4 2008/08/25 13:41:55 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.editor;

import java.awt.Component;

import javax.swing.*;

import com.unitesk.testfusion.gui.panel.table.TablePanel;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class EquivalenceEditor extends DefaultCellEditor 
{
	private static final long serialVersionUID = 0L;

	protected JTextField textField;
	protected TablePanel panel;
	
	public EquivalenceEditor(TablePanel panel, JTextField textField) 
	{
		super(textField);
		
		this.textField = textField;
		this.panel = panel;
	}
    
    // This method is called when editing is started
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
    {
    	// Set editing flag
    	panel.setEditingByTextField(true);
    	
    	// Update objects enabling that depends of actions
    	panel.updateActionDependenceObjects();
    	
    	return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
    
    // This method is called when editing is completed
    public Object getCellEditorValue() 
    {
    	panel.setEditingByTextField(false);
    	
    	// Update objects enabling that depends of actions
    	panel.updateActionDependenceObjects();
    	
        return textField.getText();
    }
}
