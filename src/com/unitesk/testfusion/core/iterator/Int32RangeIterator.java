/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Int32RangeIterator.java,v 1.8 2008/09/03 09:38:55 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Iterator of <code>int</code> values from the given range.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Int32RangeIterator extends Int32Iterator
{
	protected int min;
	protected int max;
    protected int inc;
	
	protected int value;
	
    /** Flag that refrects availability of the value. */
	protected boolean hasValue;
	
    /**
     * Constructor.
     * 
     * @param <code>min</code> the minimum value.
     * 
     * @param <code>max</code> the maximum value.
     * 
     * @param <code>inc</code> the increment value.
     */
	public Int32RangeIterator(int min, int max, int inc)
	{
		if(min > max)
			{ throw new IllegalArgumentException("min is greater than max"); }
        
        if(inc <= 0)
            { throw new IllegalArgumentException("inc is non-positive"); }
			
		this.min = min;
		this.max = max;
        this.inc = inc;
	}
	
    /**
     * Constructor. The increment value is supposed to be one.
     * 
     * @param <code>min</code> the minimum value.
     * 
     * @param <code>max</code> the maximum value.
     */
    public Int32RangeIterator(int min, int max)
    {
        this(min, max, 1);
    }
    
    /** Default constructor. The minimum and maximum values are zero. */
    public Int32RangeIterator()
    {
        this(0, 0);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
	protected Int32RangeIterator(Int32RangeIterator r)
    {
        min = r.min;
        max = r.max;
        inc = r.inc;
        
        value = r.value;
        hasValue = r.hasValue;
    }

    /**
     * Returns the minimum value.
     * 
     * @return the minimum value.
     */
    public int getMinValue()
    {
        return min;
    }
    
    /**
     * Sets the minimum value.
     * 
     * @param <code>min</code> the minimum value.
     */
    public void setMinValue(int min)
    {
        this.min = min;
    }
    
    /**
     * Returns the maximum value.
     * 
     * @return the maximum value.
     */
    public int getMaxValue()
    {
        return max;
    }
    
    /**
     * Sets the maximum value.
     * 
     * @param <code>max</code> the maximum value.
     */
    public void setMaxValue(int max)
    {
        this.max = max;
    }
    
    /**
     * Returns the increment value.
     * 
     * @return the increment value.
     */
    public int getInrementValue()
    {
        return inc;
    }
    
    /**
     * Sets the increment value.
     * 
     * @param <code>inc</code> the increment value.
     */
    public void setIncrementValue(int inc)
    {
        this.inc = inc;
    }
    
    /** Initializes the iterator. */
	public void init()
	{
		value = min;
		hasValue = true;
	}
    
    /**
     * Checks if the iterator is not exhausted (value is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasValue()
    {
        return hasValue;
    }
    
    /**
     * Sets the current value.
     * 
     * @param <code>value</code> new value.
     */
    public void setValue(int value)
    {
        if(value < min || value > max)
            { throw new IllegalArgumentException("value is out of range"); }
        
        this.value = value;
    }
    
    /**
     * Returns the current value.
     * 
     * @return the current value.
     */
	public int int32Value()
	{
		return value;
	}

    /** Makes the iteration. */
	public void next()
	{
        if(value > max - inc)
            { hasValue = false; }
        else
            { value += inc; } 
	}

    /** Stops the iterator. */
    public void stop()
    {
        hasValue = false;
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Int32RangeIterator clone()
    {
        return new Int32RangeIterator(this);
    }
}
