/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: EmptyTextFieldListener.java,v 1.1 2008/11/12 12:26:08 kozlov Exp $
 */

package com.unitesk.testfusion.gui.event;

import java.util.EventListener;

/**
 * The listener interface for receiving events about changing state of a text 
 * field from empty to non-empty or vice versa.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public interface EmptyTextFieldListener extends EventListener
{
    /** A text field has emptied. */ 
    void fieldEmptied(EmptyTextFieldEvent e);
    
    /** A text field has fulled, or has become non-empty. */
    void fieldFulled(EmptyTextFieldEvent e);
}
