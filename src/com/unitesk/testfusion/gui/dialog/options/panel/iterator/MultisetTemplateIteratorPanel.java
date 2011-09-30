/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: MultisetTemplateIteratorPanel.java,v 1.1 2008/11/20 13:05:33 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel.iterator;

import com.unitesk.testfusion.core.config.OptionsConfig;
import com.unitesk.testfusion.core.config.template.MultisetTemplateIteratorConfig;
import com.unitesk.testfusion.core.config.template.TemplateIteratorConfig;
import com.unitesk.testfusion.gui.dialog.OptionsDialog;
import com.unitesk.testfusion.gui.textfield.NonEmptyIntTextField;

/**
 * Class for a panel with multiset template iterator parameters.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class MultisetTemplateIteratorPanel extends AbstractTemplateIteratorPanel
{
    public static final long serialVersionUID = 0;
    
    /** Text field for minimum template size. */ 
    protected NonEmptyIntTextField minField;
    
    /** Text field for maximum template size. */
    protected NonEmptyIntTextField maxField;
    
    /** Text field for maximum number of repetitions of equal instructions. */
    protected NonEmptyIntTextField maxRepetitionField;
    
    /**
     * Constructor.
     * 
     * @param <code>dialog</code> the options dialog.
     */
    public MultisetTemplateIteratorPanel(OptionsDialog dialog)
    {
        super(dialog);
        
        MultisetTemplateIteratorConfig iter = (MultisetTemplateIteratorConfig)
            currentOptions.getTemplateIterator(
                    OptionsConfig.MULTISET_TEMPLATE_ITERATOR);
        
        minField = new NonEmptyIntTextField(iter.getMinTemplateSize(),
                TEXT_FIELD_SIZE, 0, 
                TemplateIteratorConfig.MAX_TEMPLATE_SIZE);
        minField.addEmptyTextFieldListener(dialog);
        
        maxField = new NonEmptyIntTextField(iter.getMaxTemplateSize(),
                TEXT_FIELD_SIZE, 1, 
                TemplateIteratorConfig.MAX_TEMPLATE_SIZE);
        maxField.addEmptyTextFieldListener(dialog);
        
        maxRepetitionField = new NonEmptyIntTextField(iter.getMaxRepetition(), 
                TEXT_FIELD_SIZE, 1, 
                TemplateIteratorConfig.MAX_TEMPLATE_SIZE);
        maxRepetitionField.addEmptyTextFieldListener(dialog);
        
        // add elements on panel 
        addLabelAndTextField("Min Template Size:", minField, false);
        addLabelAndTextField("Max Template Size:", maxField, false);
        addLabelAndTextField("Max Repetition:", maxRepetitionField, true);
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
    
    /**
     * Returns the maximum number of repetitions of equal instructions.
     * 
     * @return the maximum number of repetitions of equal instructions.
     */
    public int getMaxRepetitionSizeValue()
    {
        return maxRepetitionField.getIntValue();
    }
}
