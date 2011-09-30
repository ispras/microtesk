/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Int8RangeIterator.java,v 1.8 2008/09/03 09:38:55 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Iterator of <code>byte</code> values from the given range.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Int8RangeIterator extends Int8Iterator
{
    protected byte min;
    protected byte max;
    protected byte inc;
    
    protected byte value;
    
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
    public Int8RangeIterator(byte min, byte max, byte inc)
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
    public Int8RangeIterator(byte min, byte max)
    {
        this(min, max, (byte)1);
    }
    
    /** Default constructor. The minimum and maximum values are zero. */
    public Int8RangeIterator()
    {
        this((byte)0, (byte)0);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected Int8RangeIterator(Int8RangeIterator r)
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
    public byte getMinValue()
    {
        return min;
    }
    
    /**
     * Sets the minimum value.
     * 
     * @param <code>min</code> the minimum value.
     */
    public void setMinValue(byte min)
    {
        this.min = min;
    }
    
    /**
     * Returns the maximum value.
     * 
     * @return the maximum value.
     */
    public byte getMaxValue()
    {
        return max;
    }
    
    /**
     * Sets the maximum value.
     * 
     * @param <code>max</code> the maximum value.
     */
    public void setMaxValue(byte max)
    {
        this.max = max;
    }
    
    /**
     * Returns the increment value.
     * 
     * @return the increment value.
     */
    public byte getInrementValue()
    {
        return inc;
    }
    
    /**
     * Sets the increment value.
     * 
     * @param <code>inc</code> the increment value.
     */
    public void setIncrementValue(byte inc)
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
     * Returns the current value.
     * 
     * @return the current value.
     */
    public byte int8Value()
    {
        return value;
    }
    
    /**
     * Sets the current value.
     * 
     * @param <code>value</code> new value.
     */
    public void setValue(byte value)
    {
        if(value < min || value > max)
            { throw new IllegalArgumentException("value is out of range"); }
        
        this.value = value;
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
    public Int8RangeIterator clone()
    {
        return new Int8RangeIterator(this);
    }
}
