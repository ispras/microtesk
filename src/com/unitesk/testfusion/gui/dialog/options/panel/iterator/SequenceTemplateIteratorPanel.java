/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SequenceTemplateIteratorPanel.java,v 1.2 2008/11/20 13:18:08 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel.iterator;

import java.awt.event.*;

import javax.swing.*;

import com.unitesk.testfusion.core.config.OptionsConfig;
import com.unitesk.testfusion.core.config.template.SequenceTemplateIteratorConfig;
import com.unitesk.testfusion.gui.GUI;

import com.unitesk.testfusion.gui.dialog.OptionsDialog;
import com.unitesk.testfusion.gui.dialog.options.SequenceTemplateIteratorDialog;

/**
 * Class for a panel with sequence template iterator parameters.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SequenceTemplateIteratorPanel extends 
        CustomizableTemplateIteratorPanel
{
    public static final long serialVersionUID = 0;
    
    protected JPanel panel = this;
    
    protected SequenceTemplateIteratorConfig iter;
    
    public SequenceTemplateIteratorPanel(GUI frame, OptionsDialog dialog)
    {
        super(frame, dialog);
        
        iter = (SequenceTemplateIteratorConfig)currentOptions.getTemplateIterator(
                OptionsConfig.SEQUENCE_TEMPLATE_ITERATOR);
        
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
			SequenceTemplateIteratorDialog dialog = 
                new SequenceTemplateIteratorDialog(frame, optionsDialog, panel);

			dialog.setTitle(GUI.APPLICATION_NAME 
                    + " - Options - Sequence Template Iterator");
			
			// Order is important
			// Set size
			dialog.setSize(optionsDialog.getSize());
			
			// Set location
			dialog.setLocation();
			
			dialog.setVisible(true);
		}
    }
}