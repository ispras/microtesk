/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: BranchTemplateIteratorConfig.java,v 1.1 2009/07/09 14:48:06 kamkin Exp $
 */

package com.unitesk.testfusion.core.config.template;

import com.unitesk.testfusion.core.template.iterator.branch.BranchTemplateIterator;

/**
 * Configuration of branch template iterator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BranchTemplateIteratorConfig extends TemplateIteratorConfig
{
    /** String representation of the configuration. */
    public static final String NAME = "Branch Template Iterator";
    
    /** Default value for <code>delaySlot</code>. */
    public static final boolean DEFAULT_DELAY_SLOT = true;

    /** Default value for <code>minTemplateSize</code>. */
    public static final int DEFAULT_MIN_TEMPLATE_SIZE = 1;

    /** Default value for <code>maxTemplateSize</code>. */
    public static final int DEFAULT_MAX_TEMPLATE_SIZE = 1;

    /** Default value for <code>minBranchNumber</code>. */
    public static final int DEFAULT_MIN_BRANCH_NUMBER = 1;

    /** Default value for <code>maxBranchNumber</code>. */
    public static final int DEFAULT_MAX_BRANCH_NUMBER = 1;

    /** Default value for <code>maxBranchExecution</code>. */
    public static final int DEFAULT_MAX_BRANCH_EXECUTION = 1;    

    /** Flag that shows whether branch delay slots exist or not. */
    protected boolean delaySlot = DEFAULT_DELAY_SLOT;
    
    /** Minimal test template size (branch delay slots are not taken into account). */
    protected int minTemplateSize = DEFAULT_MIN_TEMPLATE_SIZE;

    /** Maximal test template size (branch delay slots are not taken into account). */
    protected int maxTemplateSize = DEFAULT_MAX_TEMPLATE_SIZE;
    
    /** Minimal number of branch instructions in a test action. */
    protected int minBranchNumber = DEFAULT_MIN_BRANCH_NUMBER;

    /** Maximal number of branch instructions in a test action. */
    protected int maxBranchNumber = DEFAULT_MAX_BRANCH_NUMBER;
    
    /** Maximal number of branch execution . */
    protected int maxBranchExecution = DEFAULT_MAX_BRANCH_EXECUTION;    
    
    /** Default constructor. */
    public BranchTemplateIteratorConfig()
    {
        super(NAME);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to configuration object of branch
     *        template iterator.
     */
    protected BranchTemplateIteratorConfig(BranchTemplateIteratorConfig r)
    {
        super(r);

        delaySlot = r.delaySlot;
        minTemplateSize = r.minTemplateSize;
        maxTemplateSize = r.maxTemplateSize;
        minBranchNumber = r.minBranchNumber;
        maxBranchNumber = r.maxBranchNumber;
        maxBranchExecution = r.maxBranchExecution;
    }
    
    /**
     * Returns the delay slot flag.
     * 
     * @return the delay slot flag.
     */
    public boolean getDelaySlot()
    {
        return delaySlot;
    }
    
    /**
     * Sets the delay slot flag.
     * 
     * @param <code>delaySlot</code> the delay slot flag.
     */
    public void setDelaySlot(boolean delaySlot)
    {
        this.delaySlot = delaySlot;
    }
    
    /**
     * Returns the minimal test template size.
     * 
     * @return the minimal test template size.
     */
    public int getMinTemplateSize()
    {
        return minTemplateSize;
    }

    /**
     * Sets the minimal test template size.
     * 
     * @param <code>minTemplateSize</code> the minimal test template size.
     */
    public void setMinTemplateSize(int minTemplateSize)
    {
        this.minTemplateSize = minTemplateSize;
    }
    
    /**
     * Returns the maximal test template size.
     * 
     * @return the maximal test template size.
     */
    public int getMaxTemplateSize()
    {
        return maxTemplateSize;
    }
    
    /**
     * Sets the maximal test template size.
     * 
     * @param <code>maxTemplateSize</code> the maximal test template size.
     */
    public void setMaxTemplateSize(int maxTemplateSize)
    {
        this.maxTemplateSize = maxTemplateSize;
    }
    
    /**
     * Returns the minimal branch number.
     * 
     * @return the minimal branch number.
     */
    public int getMinBranchNumber()
    {
        return minBranchNumber;
    }
    
    /**
     * Sets the minimal branch number.
     * 
     * @param <code>minBranchNumber</code> the minimal branch number;
     */
    public void setMinBranchNumber(int minBranchNumber)
    {
        this.minBranchNumber = minBranchNumber;
    }
    
    /**
     * Returns the maximal branch number.
     * 
     * @return the maximal branch number.
     */
    public int getMaxBranchNumber()
    {
        return maxBranchNumber;
    }
    
    /**
     * Sets the maximal branch number.
     * 
     * @param <code>maxBranchNumber</code> the maximal branch number.
     */
    public void setMaxBranchNumber(int maxBranchNumber)
    {
        this.maxBranchNumber = maxBranchNumber;
    }
    
    /**
     * Returns the maximal branch execution number.
     * 
     * @return the maximal branch execution number.
     */
    public int getMaxBranchExecution()
    {
        return maxBranchExecution;
    }
    
    /**
     * Sets the maximal branch execution number.
     * 
     * @param <code>maxBranchExecution</code> the maximal branch execution number.
     */
    public void setMaxBranchExecution(int maxBranchExecution)
    {
        this.maxBranchExecution = maxBranchExecution;
    }
    
    /**
     * Creates the product template iterator.
     * 
     * @return the created product template iterator. 
     */
    public BranchTemplateIterator createTemplateIterator()
    {
        return new BranchTemplateIterator(delaySlot, minTemplateSize, maxTemplateSize, minBranchNumber, maxBranchNumber, maxBranchExecution);
    }
    
    /**
     * Returns a copy of the configuration.
     *
     * @return a copy of the configuration.
     */
    public BranchTemplateIteratorConfig clone()
    {
        return new BranchTemplateIteratorConfig(this);
    }
}
