/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ShowSectionAction.java,v 1.7 2008/08/29 08:12:49 kozlov Exp $
 */

package com.unitesk.testfusion.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.unitesk.testfusion.gui.GUI;

/**
 * Contains actions for showing current section.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class ShowSectionAction implements ActionListener
{
    /** GUI frame. */
    protected GUI frame;
    
    /**
     * Base constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     */
    public ShowSectionAction(GUI frame)
    {
        this.frame = frame;
    }
    
    public void actionPerformed(ActionEvent event)
    {
        frame.showSection(frame.getSection(), true);
    }
}
