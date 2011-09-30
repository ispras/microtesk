/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: IndexArrayIterator.java,v 1.3 2009/08/12 15:17:51 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Iterator of <code>int[]</code> arrays of the given length containing
 * ordered indexes:
 * 
 * min <= array[0] < array[1] < ... < array[size - 1] <= max.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class IndexArrayIterator
{
    /** Minimal index. */
    protected int min;
    
    /** Maximal index. */
    protected int max;
    
    /** Array size. */
    protected int size;
    
    /** Current array. */
    protected int[] value;
    
    /** Flag that refrects availability of the value. */
    protected boolean hasValue;
    
    /**
     * Constructor.
     * 
     * @param <code>min</code> the minimal index.
     * 
     * @param <code>max</code> the maximal index.
     * 
     * @param <code>size</code> the size of the array.
     */
    public IndexArrayIterator(int min, int max, int size)
    {
        if(min > max)
            { throw new IllegalArgumentException("min is greater than max"); }
        
        if(size > max - min + 1)
            { throw new IllegalArgumentException("size is too big"); }
            
        this.min  = min;
        this.max  = max;
        this.size = size;
    }
    
    /** Default constructor. */
    public IndexArrayIterator()
    {
        this(0, 0, 0);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected IndexArrayIterator(IndexArrayIterator r)
    {
        min  = r.min;
        max  = r.max;
        size = r.size;
        
        hasValue = r.hasValue;
        
        if(r.value == null)
            { value = null; }
        else
            { System.arraycopy(r.value, 0, value = new int[r.value.length], 0, r.value.length); }
    }

    /**
     * Returns the minimal index.
     * 
     * @return the minimal index.
     */
    public int getMinIndex()
    {
        return min;
    }
    
    /**
     * Sets the minimal index.
     * 
     * @param <code>min</code> the minimal index.
     */
    public void setMinIndex(int min)
    {
        this.min = min;
    }
    
    /**
     * Returns the maximal index.
     * 
     * @return the maximal index.
     */
    public int getMaxIndex()
    {
        return max;
    }
    
    /**
     * Sets the maximal index.
     * 
     * @param <code>max</code> the maximal index.
     */
    public void setMaxIndex(int max)
    {
        if(size > max - min + 1)
            { throw new IllegalArgumentException("size is too big"); }
        
        this.max = max;
    }
    
    /**
     * Returns the array size.
     * 
     * @return the array size.
     */
    public int getArraySize()
    {
        return size;
    }
    
    /**
     * Sets the array size.
     * 
     * @param <code>size</code> the array size.
     */
    public void setArraySize(int size)
    {
        if(size > max - min + 1)
            { throw new IllegalArgumentException("size is too big"); }
        
        this.size = size;
    }
    
    /** Initializes the iterator. */
    public void init()
    {
        hasValue = true;
        
        value = new int[size];
        
        for(int i = 0; i < size; i++)
            { value[i] = min + i; }
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
    public int[] indexArrayValue()
    {
        return value;
    }

    /** Makes the iteration. */
    public void next()
    {
        if(!hasValue)
            { return; }

        int m = size - 1; 
        for(int i = m; i >=0; i--)
        {
            if(value[i] < max - (m - i))
            {
                value[i]++;
                
                for(int j = i + 1; j < size; j++)
                    { value[j] = value[i] + j - i; }
                
                return;
            }
        }
        
        hasValue = false;        
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
    public IndexArrayIterator clone()
    {
        return new IndexArrayIterator(this);
    }
}
