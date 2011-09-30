/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SingleArrayIterator.java,v 1.4 2008/08/18 08:08:34 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Iterator of <code>float[]</code> array items.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SingleArrayIterator extends SingleIterator
{
    /** Iterated array. */
    protected float[] array;

    /** Current index. */
    protected int index;
    
    /** Flag that refrects availability of the value. */
    protected boolean hasValue;
    
    /**
     * Constructor.
     * 
     * @param <code>array</code> the array to be iterated.
     */
    public SingleArrayIterator(float[] array)
    {
        if(array == null || array.length == 0)
            { throw new IllegalArgumentException("array is empty"); }
            
        this.array = array;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected SingleArrayIterator(SingleArrayIterator r)
    {
        int i, size;
        
        index = r.index;
        
        size = r.array.length;
        array = new float[size];

        for(i = 0; i < size; i++)
            { array[i] = r.array[i]; }
        
        hasValue = r.hasValue;
    }
    
    /** Initializes the iterator. */
    public void init()
    {
        index = 0;
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
    public float singleValue()
    {
        return array[index];
    }
    
    /** Makes the iteration. */
    public void next()
    {
        if(index == array.length - 1)
            { hasValue = false; }
        else
            { index++; }
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
    public SingleArrayIterator clone()
    {
        return new SingleArrayIterator(this);
    }
}
