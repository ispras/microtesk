/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: RunAction.java,v 1.9 2008/08/13 12:46:32 kamkin Exp $
 */

package com.unitesk.testfusion.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.unitesk.testfusion.core.config.walker.TestBuilder;
import com.unitesk.testfusion.gui.GUI;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RunAction implements ActionListener
{
    /** Generation thread */
    public static Thread thread;
    
    protected GUI frame;
    
    protected class RunGenerationThread extends Thread
    {
        public void run()
        {
            try
            {
                TestBuilder.buildAndRun(frame.getConfig());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            catch(Throwable e) {}
        }
    }
    
    protected class JoinGenerationThread extends Thread
    {
        public void run()
        {
            try
            {
                frame.enableRunAction(false);
                frame.enableStopAction(true);
                
                thread = new RunGenerationThread();
                thread.start();

                thread.join();
                thread = null;

                frame.enableRunAction(true);
                frame.enableStopAction(false);
            }
            catch(Throwable e) {}
        }
    }

    public RunAction(GUI frame)
    {
        this.frame = frame;
    }
    
    public void actionPerformed(ActionEvent event)
    {
        new JoinGenerationThread().start();
    }
}
