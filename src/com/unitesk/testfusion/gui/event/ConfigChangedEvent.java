/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigChangedEvent.java,v 1.1 2008/12/18 14:01:27 kozlov Exp $
 */

package com.unitesk.testfusion.gui.event;

import java.util.EventObject;

public class ConfigChangedEvent extends EventObject 
{
    public static final long serialVersionUID = 0;
    
    public ConfigChangedEvent(Object source)
    {
        super(source);
    }
}
