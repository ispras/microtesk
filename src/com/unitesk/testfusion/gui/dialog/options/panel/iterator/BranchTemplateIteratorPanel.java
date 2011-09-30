/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: BranchTemplateIteratorPanel.java,v 1.1 2009/07/09 14:48:16 kamkin Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel.iterator;

import com.unitesk.testfusion.core.config.OptionsConfig;
import com.unitesk.testfusion.core.config.template.BranchTemplateIteratorConfig;
import com.unitesk.testfusion.core.config.template.TemplateIteratorConfig;
import com.unitesk.testfusion.gui.dialog.OptionsDialog;
import com.unitesk.testfusion.gui.textfield.NonEmptyIntTextField;

/**
 * Class for a panel with branch template iterator parameters.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Alexander Kamkin</a>
 */
public class BranchTemplateIteratorPanel extends AbstractTemplateIteratorPanel
{
    public static final long serialVersionUID = 0;
    
    /** Text field for minimum template size. */ 
    protected NonEmptyIntTextField minField;
    
    /** Text field for maximum template size. */
    protected NonEmptyIntTextField maxField;

    /** Text field for minimum branch number. */ 
    protected NonEmptyIntTextField minBranchNumberField;
    
    /** Text field for maximum branch number. */
    protected NonEmptyIntTextField maxBranchNumberField;
    
    /** Text field for maximum number of repetitions of equal instructions. */
    protected NonEmptyIntTextField maxBranchExecutionField;
    
    /**
     * Constructor.
     * 
     * @param <code>dialog</code> the options dialog.
     */
    public BranchTemplateIteratorPanel(OptionsDialog dialog)
    {
        super(dialog);
        
        BranchTemplateIteratorConfig iter = (BranchTemplateIteratorConfig)
            currentOptions.getTemplateIterator(
                    OptionsConfig.BRANCH_TEMPLATE_ITERATOR);
        
        minField = new NonEmptyIntTextField(iter.getMinTemplateSize(),
                TEXT_FIELD_SIZE, 1, 
                TemplateIteratorConfig.MAX_TEMPLATE_SIZE);
        minField.addEmptyTextFieldListener(dialog);
        
        maxField = new NonEmptyIntTextField(iter.getMaxTemplateSize(),
                TEXT_FIELD_SIZE, 1, 
                TemplateIteratorConfig.MAX_TEMPLATE_SIZE);
        maxField.addEmptyTextFieldListener(dialog);

        minBranchNumberField = new NonEmptyIntTextField(iter.getMinBranchNumber(),
                TEXT_FIELD_SIZE, 1, 
                TemplateIteratorConfig.MAX_TEMPLATE_SIZE);
        minBranchNumberField.addEmptyTextFieldListener(dialog);
        
        maxBranchNumberField = new NonEmptyIntTextField(iter.getMaxBranchNumber(),
                TEXT_FIELD_SIZE, 1, 
                TemplateIteratorConfig.MAX_TEMPLATE_SIZE);
        maxBranchNumberField.addEmptyTextFieldListener(dialog);
        
        maxBranchExecutionField = new NonEmptyIntTextField(iter.getMaxBranchExecution(), 
                TEXT_FIELD_SIZE, 1, 
                TemplateIteratorConfig.MAX_TEMPLATE_SIZE);
        maxBranchExecutionField.addEmptyTextFieldListener(dialog);
        
        // add elements on panel 
        addLabelAndTextField("Min Template Size:",    minField,                false);
        addLabelAndTextField("Max Template Size:",    maxField,                false);
        addLabelAndTextField("Min Branch Number:",    minBranchNumberField,    false);
        addLabelAndTextField("Max Branch Number:",    maxBranchNumberField,    false);
        addLabelAndTextField("Max Branch Execution:", maxBranchExecutionField, true);
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
     * Returns the maximum template size.
     * 
     * @return the maximum template size.
     */
    public int getMaxValue()
    {
        return maxField.getIntValue();
    }

    /**
     * Returns the minimum branch number.
     * 
     * @return the minimum branch number.
     */
    public int getMinBranchNumberValue()
    {
        return minBranchNumberField.getIntValue();
    }
    
    /**
     * Returns the maximum branch number.
     * 
     * @return the maximum branch number.
     */
    public int getMaxBranchNumberValue()
    {
        return maxBranchNumberField.getIntValue();
    }
    
    /**
     * Returns the minimum branch number.
     * 
     * @return the minimum branch number.
     */
    public int getMaxBranchExecutionValue()
    {
        return maxBranchExecutionField.getIntValue();
    }
}
