/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ProductTemplateIteratorPanel.java,v 1.1 2008/11/20 13:05:33 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel.iterator;

import com.unitesk.testfusion.core.config.OptionsConfig;
import com.unitesk.testfusion.core.config.template.ProductTemplateIteratorConfig;
import com.unitesk.testfusion.core.config.template.TemplateIteratorConfig;
import com.unitesk.testfusion.gui.dialog.OptionsDialog;
import com.unitesk.testfusion.gui.textfield.NonEmptyIntTextField;

/**
 * Class for a panel with product template iterator parameters.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class ProductTemplateIteratorPanel extends AbstractTemplateIteratorPanel
{
    public static final long serialVersionUID = 0;
    
    /** Text field for template size. */ 
    protected NonEmptyIntTextField sizeField;
    
    /**
     * Constructor.
     * 
     * @param <code>dialog</code> the options dialog.
     */
    public ProductTemplateIteratorPanel(OptionsDialog dialog)
    {
        super(dialog);
        
        ProductTemplateIteratorConfig iter = (ProductTemplateIteratorConfig)
            currentOptions.getTemplateIterator(
                    OptionsConfig.PRODUCT_TEMPLATE_ITERATOR);
        
        sizeField = new NonEmptyIntTextField(iter.getTemplateSize(), 
                TEXT_FIELD_SIZE, 1, 
                TemplateIteratorConfig.MAX_TEMPLATE_SIZE);
        sizeField.addEmptyTextFieldListener(dialog);
        
        // add elements on panel
        addLabelAndTextField("Template Size:", sizeField, true);
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
}
