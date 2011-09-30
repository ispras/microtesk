/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: IntTextField.java,v 1.1 2008/11/12 12:26:11 kozlov Exp $
 */

package com.unitesk.testfusion.gui.textfield;

import javax.swing.JTextField;
import javax.swing.text.Document;

/**
 * Class for text field, which allows only string, which represents whole 
 * numbers, as it's text. 
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class IntTextField extends JTextField
{
    private static final long serialVersionUID = 0;
    
    int emptyValue;
    
    public IntTextField(int size, int emptyValue) 
    {
        super("", size);
        this.emptyValue = emptyValue;
        createDefaultModel();
    }
    
    public IntTextField(int defval, int size, int emptyValue) 
    {
        super("" + defval, size);
        this.emptyValue = emptyValue;
        createDefaultModel();
    }
    
    public int getIntValue()
    {
        if (getText().equals(""))
            return emptyValue;
        
        int value = Integer.parseInt(getText());
        
        return value;
    }
    
    public void setIntValue(int value)
    {
        if (value == emptyValue)
            { setText(""); }
        else
            { setText(value + ""); }
    }
    
    protected Document createDefaultModel() 
    {
        return new IntTextDocument(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
}
