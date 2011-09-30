/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: PositionTextField.java,v 1.1 2008/09/29 09:30:46 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.editor;

import javax.swing.JTextField;
import javax.swing.text.*;

import com.unitesk.testfusion.gui.dialog.options.SingleTemplateIteratorDialog;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class PositionTextField extends JTextField
{
	private static final long serialVersionUID = 0L;

	/** Constructor. */
	public PositionTextField()
	{
		setDocument(new PositionDocument());
	}
	
	public class PositionDocument extends PlainDocument
	{
		private static final long serialVersionUID = 0L;

		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException 
        {
            if (str == null)
                {  return;  }
            
            String oldString = getText(0, getLength());
            String newString = oldString.substring(0, offs) + str + oldString.substring(offs);

            if(!newString.equals(SingleTemplateIteratorDialog.ALL_POSITIONS))
            {
	            try 
	            {
	               // Try to parse integer number
	               Integer.parseInt(newString);
	               
	               super.insertString(offs, str, a);
	            } 
	            catch (NumberFormatException e) 
	            {
	                // do nothing, only restrict illegal input
	            }
            }
            else
            {
                super.insertString(offs, str, a);
            }
        }
	}
}
