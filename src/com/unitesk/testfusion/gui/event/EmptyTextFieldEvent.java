/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: EmptyTextFieldEvent.java,v 1.1 2008/11/12 12:26:08 kozlov Exp $
 */

package com.unitesk.testfusion.gui.event;

import java.util.EventObject;

/**
 * A semantic event which indicates that a state of a text field has been 
 * changed for empty to non-epmty or vice versa.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class EmptyTextFieldEvent extends EventObject 
{
    public static final long serialVersionUID = 0;
    
    public EmptyTextFieldEvent(Object source)
    {
        super(source);
    }
}
