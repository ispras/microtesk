/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Int16ArrayIterator.java,v 1.4 2008/08/18 08:08:34 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Iterator of <code>short[]</code> array items.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Int16ArrayIterator extends Int16Iterator
{
    /** Iterated array. */
    protected short[] array;

    /** Current index. */
    protected int index;
    
    /** Flag that refrects availability of the value. */
    protected boolean hasValue;
    
    /**
     * Constructor.
     * 
     * @param <code>array</code> the array to be iterated.
     */
    public Int16ArrayIterator(short[] array)
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
    protected Int16ArrayIterator(Int16ArrayIterator r)
    {
        int i, size;
        
        index = r.index;
        
        size = r.array.length;
        array = new short[size];

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
    public short int16Value()
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
    public Int16ArrayIterator clone()
    {
        return new Int16ArrayIterator(this);
    }
}
