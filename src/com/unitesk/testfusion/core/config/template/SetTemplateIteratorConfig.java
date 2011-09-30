/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SetTemplateIteratorConfig.java,v 1.7 2008/11/20 13:06:28 kozlov Exp $
 */

package com.unitesk.testfusion.core.config.template;

import com.unitesk.testfusion.core.template.iterator.SetTemplateIterator;

/**
 * Configuration of set template iterator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SetTemplateIteratorConfig extends TemplateIteratorConfig
{
    /** String representation of the configuration. */
    public static final String NAME = "Set Template Iterator";
    
    /** Default value for <code>minTemplateSize</code>. */
    public static final int DEFAULT_MIN_TEMPLATE_ITERATOR = 1;
    
    /** Default value for <code>maxTemplateSize</code>. */
    public static final int DEFAULT_MAX_TEMPLATE_ITERATOR = 1;
    
    /** Minimum value of test template size. */
    protected int minTemplateSize = DEFAULT_MIN_TEMPLATE_ITERATOR;
    
    /** Maximum value of test template size. */
    protected int maxTemplateSize = DEFAULT_MAX_TEMPLATE_ITERATOR;
    
    /** Default constructor. */
    public SetTemplateIteratorConfig()
    {
        super(NAME);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to configuration object of set
     *        template iterator.
     */
    protected SetTemplateIteratorConfig(SetTemplateIteratorConfig r)
    {
        super(r);
        
        minTemplateSize = r.minTemplateSize;
        maxTemplateSize = r.maxTemplateSize;
    }
    
    /**
     * Returns the minimum value of test template size.
     * 
     * @return the minimum value of test template size.
     */
    public int getMinTemplateSize()
    {
        return minTemplateSize;
    }
    
    /**
     * Sets the minimum value of test template size.
     * 
     * @param <code>minTemplateSize</code> the minimum value of test template
     *        size.
     */
    public void setMinTemplateSize(int minTemplateSize)
    {
        this.minTemplateSize = minTemplateSize;
    }
    
    /**
     * Returns the maximum value of test template size.
     * 
     * @return the maximum value of test template size.
     */
    public int getMaxTemplateSize()
    {
        return maxTemplateSize;
    }
    
    /**
     * Sets the maximum value of test template size.
     * 
     * @param <code>maxTemplateSize</code> the maximum value of test template
     *        size.
     */
    public void setMaxTemplateSize(int maxTemplateSize)
    {
        this.maxTemplateSize = maxTemplateSize;
    }
    
    /**
     * Creates the set template iterator.
     * 
     * @return the created set template iterator.
     */
    public SetTemplateIterator createTemplateIterator()
    {
        return new SetTemplateIterator(minTemplateSize, maxTemplateSize);
    }
    
    /**
     * Returns a copy of the configuration.
     * 
     * @return a copy of the configuration.
     */
    public SetTemplateIteratorConfig clone()
    {
        return new SetTemplateIteratorConfig(this);
    }
}
