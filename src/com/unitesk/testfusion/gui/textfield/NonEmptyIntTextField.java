/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: NonEmptyIntTextField.java,v 1.2 2008/11/13 10:21:18 kozlov Exp $
 */

package com.unitesk.testfusion.gui.textfield;

import javax.swing.text.Document;

/**
 * Class for text field, which allows only string, which represents whole 
 * numbers, as it's text. Also it reports to its listeners about changing
 * state from empty to non-empty and vice versa.  
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class NonEmptyIntTextField extends NonEmptyTextField 
{
    private static final long serialVersionUID = 0;
    
    protected final int maxValue;
    protected final int minValue;
    
    public NonEmptyIntTextField(int defval, int size, int minValue, int maxValue) 
    {
        super(new IntTextDocument(minValue, maxValue), "" + defval, size);
        this.maxValue = maxValue;
        this.minValue = minValue;
    }
    
    public NonEmptyIntTextField(int defval, int size) 
    {
        this(defval, size, 0, Integer.MAX_VALUE);
    }

    public NonEmptyIntTextField(int size) 
    {
        this(0, size, 0, Integer.MAX_VALUE);
    }
    
    public int getIntValue()
    {
        if (getText().equals(""))
            return minValue;
        
        int value = Integer.parseInt(getText());
        
        return value;
    }
    
    public void setIntValue(int n)
    {
        setText(n + "");
    }
    
    protected Document createDefaultModel() 
    {
        return new IntTextDocument(minValue, maxValue);
    }
}
