/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ShowTestAction.java,v 1.8 2008/08/29 08:12:49 kozlov Exp $
 */

package com.unitesk.testfusion.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.unitesk.testfusion.gui.GUI;

/**
 * Contains actions for showing current test.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class ShowTestAction implements ActionListener
{
    /** GUI frame. */
    protected GUI frame;
    
    /**
     * Base constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     */
    public ShowTestAction(GUI frame)
    {
        this.frame = frame;
    }

    public void actionPerformed(ActionEvent event)
    {
        frame.showTest(frame.getConfig(), true);
    }
}
