/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SingleTemplateIteratorConfig.java,v 1.7 2008/11/20 13:06:28 kozlov Exp $
 */

package com.unitesk.testfusion.core.config.template;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.core.template.iterator.SingleTemplateIterator;
import com.unitesk.testfusion.core.template.iterator.TemplateIterator;

/**
 * Configuration of single template iterator.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class SingleTemplateIteratorConfig extends TemplateIteratorConfig
{
    /** String representation of the configuration. */
    public static final String NAME = "Single Template Iterator";

    /** Default value for <code>templateSize</code>. */
    public static final int DEFAULT_TEMPLATE_SIZE = 1;
    
    /** Test template size. */
    protected int templateSize = DEFAULT_TEMPLATE_SIZE;
    
    /** Default constructor. */
    public SingleTemplateIteratorConfig()
    {
        super(NAME);
    }
    
    /**
     * Copy construtor.
     * 
     * @param <code>r</code> the reference to configuration object of single
     *        template iterator.
     */
    protected SingleTemplateIteratorConfig(SingleTemplateIteratorConfig r)
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
     * Creates the single template iterator.
     * 
     * @return the created single templte iterator.
     */
    public TemplateIterator createTemplateIterator() 
    {
        return new SingleTemplateIterator(templateSize);
    }
    
    /**
     * Returns a string representation of the configuration.
     * 
     * @return a string representation of the configuration.
     */
    public String toString()
    {
        return NAME;
    }
    
    /**
     * Returns a copy of the configuration.
     * 
     * @return a copy of the configuration.
     */
    public Config clone() 
    {
    	return new SingleTemplateIteratorConfig(this);
    }

}
