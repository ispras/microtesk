/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SettingsAction.java,v 1.3 2008/12/04 12:44:28 kozlov Exp $
 */

package com.unitesk.testfusion.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.SettingsDialog;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SettingsAction implements ActionListener
{
    protected GUI frame;
    
    public SettingsAction(GUI frame)
    {
        this.frame = frame;
    }
    
    public void actionPerformed(ActionEvent event)
    {
        execute();
    }
    
    public void execute()
    {
        SettingsDialog dialog = new SettingsDialog(frame);
        
        dialog.setVisible(true);
        
        if (dialog.isOkPressed())
        {
            // test has been changed
            frame.setTestHasChanges();
            
            frame.updateTitle();
        }
    }
}
