/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SingleTemplateIteratorPanel.java,v 1.2 2008/11/20 13:18:08 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel.iterator;

import java.awt.event.*;

import javax.swing.*;

import com.unitesk.testfusion.core.config.OptionsConfig;
import com.unitesk.testfusion.core.config.template.SingleTemplateIteratorConfig;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.OptionsDialog;

import com.unitesk.testfusion.gui.dialog.options.SingleTemplateIteratorDialog;

/**
 * Class for a panel with single template iterator parameters.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SingleTemplateIteratorPanel extends 
        CustomizableTemplateIteratorPanel 
{
    public static final long serialVersionUID = 0;
    
    protected JPanel panel = this;

    protected SingleTemplateIteratorConfig iter; 
    
    public SingleTemplateIteratorPanel(GUI frame, OptionsDialog dialog)
    {
        super(frame, dialog);
        
        iter = (SingleTemplateIteratorConfig)
                currentOptions.getTemplateIterator(
                        OptionsConfig.SINGLE_TEMPLATE_ITERATOR);
        
        sizeField.setIntValue(iter.getTemplateSize());
        
        customizeButton.addActionListener(new CustomizeListener());
    }
    
    public void setSizeValue(int templateSize)
    {
    	// Set size into iterator
    	iter.setTemplateSize(templateSize);
        
    	sizeField.setIntValue(templateSize);
    }
    
    protected class CustomizeListener implements ActionListener
    {
		public void actionPerformed(ActionEvent event) 
		{
			SingleTemplateIteratorDialog dialog = 
                new SingleTemplateIteratorDialog(frame, optionsDialog, panel);
            
			dialog.setTitle(GUI.APPLICATION_NAME + 
                    " - Options - Single Template Iterator");
			
			// Order is important
			// Set size
			dialog.setSize(optionsDialog.getSize());
			
			// Set location
			dialog.setLocation();
			
			dialog.setVisible(true);
		}
    }
}