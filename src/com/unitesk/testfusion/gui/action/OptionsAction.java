/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: OptionsAction.java,v 1.3 2008/12/04 12:44:28 kozlov Exp $
 */

package com.unitesk.testfusion.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.unitesk.testfusion.core.config.SectionListConfig;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.OptionsDialog;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class OptionsAction implements ActionListener
{
    protected GUI frame;
    
    public OptionsAction(GUI frame)
    {
        this.frame = frame;
    }
    
    public void actionPerformed(ActionEvent event)
    {
        SectionListConfig currentConfig = (frame.getSection() == null) ?
                frame.getConfig() : frame.getSection();
                
        execute(currentConfig);
    }
    
    public void execute(SectionListConfig config)
    {
        OptionsDialog dialog = new OptionsDialog(frame, config);
        
        dialog.setVisible(true);
        
        if (dialog.isOkPressed())
        {
            // test has been changed
            frame.setTestHasChanges();
            
            frame.updateTitle();
        }
    }
}
