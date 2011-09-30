/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ProductTemplateIteratorConfig.java,v 1.7 2008/11/20 13:06:28 kozlov Exp $
 */

package com.unitesk.testfusion.core.config.template;

import com.unitesk.testfusion.core.template.iterator.ProductTemplateIterator;

/**
 * Configuration of product template iterator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ProductTemplateIteratorConfig extends TemplateIteratorConfig
{
    /** String representation of the configuration. */
    public static final String NAME = "Product Template Iterator";
    
    /** Default value for <code>templateSize</code>. */
    public static final int DEFAULT_TEMPLATE_SIZE = 1;
    
    /** Test template size. */
    protected int templateSize = DEFAULT_TEMPLATE_SIZE;
    
    /** Default constructor. */
    public ProductTemplateIteratorConfig()
    {
        super(NAME);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to configuration object of product
     *        template iterator.
     */
    protected ProductTemplateIteratorConfig(ProductTemplateIteratorConfig r)
    {
        super(r);
        
        templateSize = r.templateSize;
    }
    
    /**
     * Returns the test template size.
     * 
     * @return the test template size.
     */
    public int getTemplateSize()
    {
        return templateSize;
    }
    
    /**
     * Sets the test template size.
     * 
     * @param <code>templateSize</code> the test template size.
     */
    public void setTemplateSize(int templateSize)
    {
        this.templateSize = templateSize;
    }
    
    /**
     * Creates the product template iterator.
     * 
     * @return the created product template iterator. 
     */
    public ProductTemplateIterator createTemplateIterator()
    {
        return new ProductTemplateIterator(templateSize);
    }
    
    /**
     * Returns a copy of the configuration.
     *
     * @return a copy of the configuration.
     */
    public ProductTemplateIteratorConfig clone()
    {
        return new ProductTemplateIteratorConfig(this);
    }
}
