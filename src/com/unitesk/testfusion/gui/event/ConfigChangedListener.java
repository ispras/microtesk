/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigChangedListener.java,v 1.1 2008/12/18 14:01:28 kozlov Exp $
 */

package com.unitesk.testfusion.gui.event;

public interface ConfigChangedListener 
{
    void configChanged(ConfigChangedEvent event);
}
