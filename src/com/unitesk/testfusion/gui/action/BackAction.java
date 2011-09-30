/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: BackAction.java,v 1.11 2008/08/27 14:09:41 kozlov Exp $
 */

package com.unitesk.testfusion.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.HistoryManager;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class BackAction implements ActionListener
{
    protected GUI frame;
    
    public BackAction(GUI frame)
    {
        this.frame = frame;
    }
    
    public void actionPerformed(ActionEvent event)
    {
        HistoryManager history = frame.getHistory();

        history.back();
        
        Config config = history.get();
        
        frame.showConfig(config, false);
    }
}
