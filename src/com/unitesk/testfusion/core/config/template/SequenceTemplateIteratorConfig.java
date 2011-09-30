/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SequenceTemplateIteratorConfig.java,v 1.7 2008/11/20 13:06:28 kozlov Exp $
 */

package com.unitesk.testfusion.core.config.template;

import com.unitesk.testfusion.core.template.iterator.SequenceTemplateIterator;

/**
 * Configuration of sequence template iterator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SequenceTemplateIteratorConfig extends TemplateIteratorConfig
{
    /** String representation of the configuration. */
    public static final String NAME = "Sequence Template Iterator";
    
    /** Default value for <code>templateSize</code>. */
    public static final int DEFAULT_TEMPLATE_SIZE = 1;
    
    /** Test template size. */
    protected int templateSize = DEFAULT_TEMPLATE_SIZE;
    
    /** Default constructor. */
    public SequenceTemplateIteratorConfig()
    {
        super(NAME);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to configuration object of sequence
     *        template iterator.
     */
    protected SequenceTemplateIteratorConfig(SequenceTemplateIteratorConfig r)
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
     * Creates the sequence template iterator.
     * 
     * @return the created sequence template iterator.
     */
    public SequenceTemplateIterator createTemplateIterator()
    {
        return new SequenceTemplateIterator(templateSize);
    }
    
    /**
     * Returns a copy of the configuration.
     *
     * @return a copy of the configuration.
     */
    public SequenceTemplateIteratorConfig clone()
    {
        return new SequenceTemplateIteratorConfig(this);
    }
}
