/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: IntTextDocument.java,v 1.1 2008/11/12 12:26:11 kozlov Exp $
 */

package com.unitesk.testfusion.gui.textfield;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Class for document, which allows only whole numbers as it's text.
 *  
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class IntTextDocument extends PlainDocument 
{
    private static final long serialVersionUID = 0;
    
    int minValue;
    int maxValue;
    
    public IntTextDocument(int minValue, int maxValue)
    {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException 
    {
        if (str == null)
            {  return;  }
        
        String oldString = getText(0, getLength());
        String newString = oldString.substring(0, offs) + str + oldString.substring(offs);
        
        try 
        {
            int val = Integer.parseInt(newString);
            
            if (val > maxValue || val < minValue)
                throw new NumberFormatException();
            
            super.insertString(offs, str, a);
        } 
        catch (NumberFormatException e) 
        {
            // do nothing, only restrict illegal input
        }
    }
    
    /*
    public void remove(int offs, int len) throws BadLocationException 
    {
        String oldString = getText(0, getLength());
        String newString = oldString.substring(0, offs) + oldString.substring(offs + len);
        
        try 
        {
            if (!Utils.isNullOrEmpty(newString)) {
                
                int val = Integer.parseInt(newString);
                
                if (val > maxValue || val < minValue)
                    { throw new NumberFormatException(); }
            }
            
            super.remove(offs, len);
        } 
        catch (NumberFormatException e) 
        {
            // do nothing, only restrict illegal input
        }
    }
    
    public void replace(int offset, int length, String text, 
            AttributeSet attrs) throws BadLocationException
    {
        String oldString = getText(0, getLength());
        String newString = oldString.substring(0, offset) + text +
                oldString.substring(offset + length);
        
        try 
        {
            if (!Utils.isNullOrEmpty(newString)) {
                
                int val = Integer.parseInt(newString);
                
                if (val > maxValue || val < minValue)
                    { throw new NumberFormatException(); }
            }
            
            super.replace(offset, length, text, attrs);
        } 
        catch (NumberFormatException e) 
        {
            // do nothing, only restrict illegal input
        }
    }
    */
}
