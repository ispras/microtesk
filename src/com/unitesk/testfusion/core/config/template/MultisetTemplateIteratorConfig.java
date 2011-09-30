/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: MultisetTemplateIteratorConfig.java,v 1.8 2009/01/29 09:00:14 kozlov Exp $
 */

package com.unitesk.testfusion.core.config.template;

import com.unitesk.testfusion.core.template.iterator.MultisetTemplateIterator;

/**
 * Configuration of multiset template iterator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MultisetTemplateIteratorConfig extends TemplateIteratorConfig
{
    /** String representation of the configuration. */
    public static final String NAME = "Multiset Template Iterator";
    
    /** Default value for <code>minTemplateSize</code>. */
    public static final int DEFAULT_MIN_TEMPLATE_SIZE = 1;
    
    /** Default value for <code>maxTemplateSize</code>. */
    public static final int DEFAULT_MAX_TEMPLATE_SIZE = 1;
    
    /** Default value for <code>maxRepetition</code>. */
    public static final int DEFAULT_MAX_REPETITION    = 1;
    
    /** Minimum value of test template size. */
    protected int minTemplateSize = DEFAULT_MIN_TEMPLATE_SIZE;
    
    /** Maximum value of test template size. */
    protected int maxTemplateSize = DEFAULT_MAX_TEMPLATE_SIZE;
    
    /** Maximum number of repetitions of equal instructions. */
    protected int maxRepetition   = DEFAULT_MAX_REPETITION;
    
    /** Default constructor. */
    public MultisetTemplateIteratorConfig()
    {
        super(NAME);
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to configuration object of multiset
     *        template iterator.
     */
    protected MultisetTemplateIteratorConfig(MultisetTemplateIteratorConfig r)
    {
        super(r);
        
        minTemplateSize = r.minTemplateSize;
        maxTemplateSize = r.maxTemplateSize;
        maxRepetition   = r.maxRepetition;
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
     * Returns the maximum number of repetitions of equal instructions in test
     * template.
     * 
     * @return the maximum number of repetitions of equal instructions in test
     *         template. 
     */
    public int getMaxRepetition()
    {
        return maxRepetition;
    }
    
    /**
     * Sets the maximum number of repetitions of equal instructions in test
     * template.
     * 
     * @param <code>maxRepetition</code> the maximum number of repetitions of
     *        equal instructions in test template.
     */
    public void setMaxRepetition(int maxRepetition)
    {
        this.maxRepetition = maxRepetition;
    }
    
    /**
     * Creates multiset template iterator.
     * 
     * @return the created multiset template iterator.
     */
    public MultisetTemplateIterator createTemplateIterator()
    {
        return new MultisetTemplateIterator(0, maxRepetition, 
                minTemplateSize, maxTemplateSize);
    }
    
    /**
     * Returns a copy of the configuration.
     *
     * @return a copy of the configuration.
     */
    public MultisetTemplateIteratorConfig clone()
    {
        return new MultisetTemplateIteratorConfig(this);
    }
}
