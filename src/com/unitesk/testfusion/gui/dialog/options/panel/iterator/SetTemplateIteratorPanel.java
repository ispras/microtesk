/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SetTemplateIteratorPanel.java,v 1.1 2008/11/20 13:05:33 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel.iterator;

import com.unitesk.testfusion.core.config.OptionsConfig;
import com.unitesk.testfusion.core.config.template.SetTemplateIteratorConfig;
import com.unitesk.testfusion.core.config.template.TemplateIteratorConfig;
import com.unitesk.testfusion.gui.dialog.OptionsDialog;
import com.unitesk.testfusion.gui.textfield.NonEmptyIntTextField;

/**
 * Class for a panel with set template iterator parameters.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class SetTemplateIteratorPanel extends AbstractTemplateIteratorPanel
{
    public static final long serialVersionUID = 0;
    
    /** Text field for minimum template size. */
    protected NonEmptyIntTextField minField;
    
    /** Text field for maximum templae size. */
    protected NonEmptyIntTextField maxField;
    
    /**
     * Constructor.
     * 
     * @param <code>dialog</code> the options dialog.
     */
    public SetTemplateIteratorPanel(OptionsDialog dialog)
    {
        super(dialog);
        
        SetTemplateIteratorConfig iter = (SetTemplateIteratorConfig)
            currentOptions.getTemplateIterator(
                    OptionsConfig.SET_TEMPLATE_ITERATOR);
        
        minField = new NonEmptyIntTextField(iter.getMinTemplateSize(), 
                TEXT_FIELD_SIZE, 0, 
                TemplateIteratorConfig.MAX_TEMPLATE_SIZE);
        minField.addEmptyTextFieldListener(dialog);
        
        maxField = new NonEmptyIntTextField(iter.getMaxTemplateSize(),
                TEXT_FIELD_SIZE, 1, 
                TemplateIteratorConfig.MAX_TEMPLATE_SIZE);
        maxField.addEmptyTextFieldListener(dialog);
        
        // add elements on panel
        addLabelAndTextField("Min Template Size:", minField, false);
        addLabelAndTextField("Max Template Size:", maxField, true);
    }
    
    /**
     * Returns the maximum template size.
     * 
     * @return the maximum template size.
     */
    public int getMaxValue()
    {
        return maxField.getIntValue();
    }
    
    /**
     * Returns the minimum template size.
     * 
     * @return the minimum template size.
     */
    public int getMinValue()
    {
        return minField.getIntValue();
    }
}
