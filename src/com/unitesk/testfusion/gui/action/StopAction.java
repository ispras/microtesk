/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: StopAction.java,v 1.3 2008/06/05 16:10:37 kamkin Exp $
 */

package com.unitesk.testfusion.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.unitesk.testfusion.gui.GUI;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class StopAction implements ActionListener
{
    protected GUI frame;
    
    public StopAction(GUI frame)
    {
        this.frame = frame;
    }
    
    public void actionPerformed(ActionEvent event)
    {
        if(RunAction.thread != null)
        {
            RunAction.thread.interrupt();
            RunAction.thread = null;
            
            frame.enableRunAction(true);
            frame.enableStopAction(false);
        }
    }
}
