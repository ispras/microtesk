/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: CustomizableTemplateIteratorPanel.java,v 1.1 2008/11/20 13:18:08 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel.iterator;

import javax.swing.JButton;

import com.unitesk.testfusion.core.config.template.TemplateIteratorConfig;
import com.unitesk.testfusion.core.config.walker.ConfigWalker;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.OptionsDialog;
import com.unitesk.testfusion.gui.dialog.options.visitor.GetSpecificPositionVisitor;
import com.unitesk.testfusion.gui.textfield.NonEmptyIntTextField;

/**
 * Class for a panel with customizable template iterator parameters. 
 * Customizable means iterator specify position of instructions in 
 * a test template.    
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class CustomizableTemplateIteratorPanel extends 
        AbstractTemplateIteratorPanel 
{
    public static final long serialVersionUID = 0;
    
    /** Text field for template size. */ 
    protected NonEmptyIntTextField sizeField;
    
    /** Customize button. */
    protected JButton customizeButton;
    
    /** GUI frame. */
    protected GUI frame;
    
    /** Options dialog. */
    protected OptionsDialog optionsDialog;
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     * 
     * @param <code>dialog</code> the options dialog.
     */
    protected CustomizableTemplateIteratorPanel(GUI frame, 
            OptionsDialog dialog)
    {
        super(dialog);
        
        this.frame = frame;
        this.optionsDialog = dialog;
        
        customizeButton = new JButton("Customize");
        
        sizeField = new NonEmptyIntTextField(1, TEXT_FIELD_SIZE, 
                1, TemplateIteratorConfig.MAX_TEMPLATE_SIZE);
        sizeField.addEmptyTextFieldListener(dialog);
        
        // add elements on panel
        addLabelAndTextField("Template Size:", sizeField, true);
        addButton(customizeButton, true);
    }

    /**
     * Returns the template size.
     * 
     * @return the template size.
     */
    public int getSizeValue()
    {
        return sizeField.getIntValue();
    }
    
    public int getMaxPosition()
    {
        GetSpecificPositionVisitor visitor = new GetSpecificPositionVisitor();
        ConfigWalker walker = new ConfigWalker(frame.getSection(), visitor);
        walker.process();
        
        int maxPosition = visitor.getSpecificPosition();

        return maxPosition;
    }
}
