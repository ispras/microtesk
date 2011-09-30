/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SingleTemplateIteratorTable.java,v 1.9 2008/09/29 09:30:03 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.AbstractDocument;

import com.unitesk.testfusion.core.config.InstructionConfig;
import com.unitesk.testfusion.gui.dialog.options.editor.PositionEditor;
import com.unitesk.testfusion.gui.dialog.options.editor.PositionTextField;
import com.unitesk.testfusion.gui.dialog.options.renderer.SingleTemplateIteratorRenderer;
import com.unitesk.testfusion.gui.panel.table.combobox.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SingleTemplateIteratorTable extends JTable
{
	private static final long serialVersionUID = 0L;

	protected HashMap<Integer, TableCellEditor>        instructionEditorMap   = new HashMap<Integer, TableCellEditor>();
	protected HashMap<Integer, ChangingCombobox>       comboboxMap = new HashMap<Integer, ChangingCombobox>();
	public    HashMap<Integer, ComboboxDocumentFilter> filterMap   = new HashMap<Integer, ComboboxDocumentFilter>(); 

	protected HashSet<InstructionConfig> instructionSet;

	protected SingleTemplateIteratorDialog dialog;
	
	public SingleTemplateIteratorTable(HashSet<InstructionConfig> instructionSet, SingleTemplateIteratorDialog dialog)
	{
		this.instructionSet = instructionSet;
		this.dialog = dialog;
	}
	
	public boolean isCellEditable(int row, int column)
	{
		return true; 
	}
	
    public TableCellEditor getCellEditor(int row, int column)
    {
		if(column == SingleTemplateIteratorDialog.INSTRUCTION_COLUMN)
		{
			ChangingComboboxEditor editor;

			if(!instructionEditorMap.containsKey(row))
			{
				// Create new combobox
				ChangingCombobox combobox = new ChangingCombobox();
				
				comboboxMap.put(row, combobox);
				
				combobox.setFocusable(true);
				combobox.setEditable(true);

				// Set instruction array
				combobox.setInstructionArray(instructionSet.toArray());

				editor = new ChangingComboboxEditor(combobox);

				// Put editor into hashmap  
				instructionEditorMap.put(row, editor);
				
				// Set document filter
		    	JTextField textfield = (JTextField)combobox.getEditor().getEditorComponent();
		    	ComboboxDocumentFilter filter = new ComboboxDocumentFilter(combobox, this, row, column);
				((AbstractDocument)textfield.getDocument()).setDocumentFilter(filter);
				
				// Put filter into hashmap
				filterMap.put(row, filter);
			}
			else
			{
				editor = (ChangingComboboxEditor)instructionEditorMap.get(row);
			}
			
			return editor;
		}
		else if(column == SingleTemplateIteratorDialog.POSITION_COLUMN)
		{
			return new PositionEditor(new PositionTextField());
		}
		else
		{
			return super.getCellEditor(row, column);
		}
    }
    
    public TableCellRenderer getCellRenderer(int row, int column)
    {
    	return new SingleTemplateIteratorRenderer();
    }
}