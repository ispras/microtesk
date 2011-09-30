/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ChangingCombobox.java,v 1.9 2008/09/29 09:30:04 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.combobox;

import java.awt.Component;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class ChangingCombobox extends JComboBox
{
	private static final long serialVersionUID = 0L;
	
	public PopupListener popupListener;
	public String[] instructionArray;

	public ChangingCombobox()
	{
		this(null);
	}
	
	public ChangingCombobox(String[] instructionArray)
	{
		this.instructionArray = instructionArray;
		popupListener = new PopupListener(this);
		
		// Set renderer
		setRenderer(new PopupListCellRenderer(this));
		
		// Add listeners
		addPopupMenuListener(popupListener);
		setFocusable(true);
	}
	
    public boolean isTextfieldItemAvailable()
	{
		JTextField textfield = (JTextField)this.getEditor().getEditorComponent();
		
		String text = "";
		
		try 
		{
			text = textfield.getDocument().getText(0, textfield.getDocument().getLength());
		} 
		catch (BadLocationException e) 
		{
			e.printStackTrace();
		}
		
		for(int i = 0; i < instructionArray.length; i++)
		{
			if(text.equals(instructionArray[i]))
				{ return true; }
		}
		
		return false;
	}

	public void setInstructionArray(Object[] array)
	{
		String instructionArray[] = new String[array.length];
		
		for(int i = 0; i < array.length; i++)
		{
			String name = array[i].toString();
			instructionArray[i] = name;
		}
		
		this.instructionArray = instructionArray.clone();
	}
	
	public class PopupListener implements PopupMenuListener
	{
		public ChangingCombobox combobox;
		public String oldValue;
		
		public PopupListener(ChangingCombobox combobox)
		{
			this.combobox = combobox;
		}
		
		public void popupMenuCanceled(PopupMenuEvent e) 
		{
		}

		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) 
		{
			JTextField textfield = (JTextField)combobox.getEditor().getEditorComponent();
	    	oldValue = textfield.getText();
		}

		public void popupMenuWillBecomeVisible(PopupMenuEvent e) 
		{
		}
	}
	
	public class PopupListCellRenderer extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = 0L;

		protected ChangingCombobox combobox;
		
		public PopupListCellRenderer(ChangingCombobox combobox)
		{
			this.combobox = combobox;
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
		{
			Component cell = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			if(isSelected)
			{
				JTextField textfield = (JTextField)combobox.getEditor().getEditorComponent();
				
				ComboboxDocumentFilter filter = (ComboboxDocumentFilter)((AbstractDocument)textfield.getDocument()).getDocumentFilter();
				String oldValue = combobox.popupListener.oldValue;
				
				int oldLength;
				if(oldValue == null)
					{ oldLength = 0; }
				else
					{ oldLength = oldValue.length(); }
				
				filter.disableListeners();
				
				textfield.setText((String)value);
				
				filter.enableListeners();
				
				textfield.setSelectionStart(oldLength);
				textfield.setSelectionEnd(textfield.getText().length());
			}
			
			return cell; 
		}
	}
}