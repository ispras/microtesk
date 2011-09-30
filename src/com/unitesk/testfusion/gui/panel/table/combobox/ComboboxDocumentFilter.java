/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ComboboxDocumentFilter.java,v 1.6 2008/09/26 13:23:50 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.combobox;

import java.awt.IllegalComponentStateException;

import javax.swing.*;
import javax.swing.text.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class ComboboxDocumentFilter extends DocumentFilter
{
	protected ChangingCombobox combobox;
	protected JTable table;
	protected String[] instructionArray;
	protected int row;
	protected int column;
	
	public ComboboxDocumentFilter(ChangingCombobox combobox, JTable table, int row, int column)
	{
		this.combobox = combobox;
		this.table = table;
		this.instructionArray = combobox.instructionArray;
		this.row = row;
		this.column = column;
	}

	public void update(Document document, int offset, int length) throws BadLocationException
	{
		JTextField textfield = (JTextField)combobox.getEditor().getEditorComponent();
		
		final String text = textfield.getText();

		if(document.getLength() != 0)
			{ textfield.setCaretPosition(document.getLength()); }
		
		boolean similarInstructionExists = false;
		
		for(int i = 0; i < instructionArray.length; i++)
		{
			if(text.equals("") || (instructionArray[i].startsWith(text)))
			{
				similarInstructionExists = true;
			}		
		}		
		
		if(!similarInstructionExists)
		{
			// Set old value
			textfield.setText(combobox.popupListener.oldValue);
			
			return;
		}

		// Disable listeners
		disableListeners();

		ChangingComboboxEditor editor = (ChangingComboboxEditor)table.getCellEditor(row, column);
		
		editor.isEditing = true;
			
		// Edit cell
		table.editCellAt(row, column);

		// Refresh old Value
		combobox.popupListener.oldValue = textfield.getText();
		
		updateCombobox(text);
		
		// Enable listeners
		enableListeners();

		if(combobox.getSelectedIndex() == -1 && combobox.getItemCount() != 0) 
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run() 
				{
					try  { combobox.showPopup(); }
					catch(IllegalComponentStateException e)	{}
			    }
			}
			);
		}

		// Remove selection
		textfield.setSelectionStart(textfield.getText().length());
		textfield.setSelectionEnd(textfield.getText().length());

		// Request focus
		textfield.requestFocus();
	}
	
	public void enableListeners()
	{
		JTextField textfield = (JTextField)combobox.getEditor().getEditorComponent();
		((AbstractDocument)textfield.getDocument()).setDocumentFilter(new ComboboxDocumentFilter(combobox, table, row, column));
	}

	public void disableListeners()
	{
		JTextField textfield = (JTextField)combobox.getEditor().getEditorComponent();
		((AbstractDocument)textfield.getDocument()).setDocumentFilter(null);
	}
	
	public void updateCombobox(String text)
	{
		JTextField textfield = (JTextField)combobox.getEditor().getEditorComponent();
		String textfieldValue = textfield.getText();
		
		// Clear combobox
		int itemsCount = combobox.getItemCount();
		
		for(int i = itemsCount - 1; i >= 0; i--)
			{ combobox.removeItemAt(i);	}

		// Fill combobox
		for(int i = 0; i < instructionArray.length; i++)
		{
			if(text.equals("") || (instructionArray[i].startsWith(text) && !instructionArray[i].equals(text)))
				{ combobox.addItem(instructionArray[i]); }
		}
		
		textfield.setText(textfieldValue);
	}
	
	public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException
	{
		super.insertString(fb, offset,string, attr); 
		
		update(fb.getDocument(), offset, string.length());
	}
	
	public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException
	{
		super.remove(fb, offset, length);

		update(fb.getDocument(), offset, length);
	} 
	
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException
	{
		super.replace(fb, offset, length, text, attrs);

		update(fb.getDocument(), offset, length);
	}
}