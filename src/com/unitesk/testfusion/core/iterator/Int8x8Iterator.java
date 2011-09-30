/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Int8x8Iterator.java,v 1.4 2008/08/18 08:08:34 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Iterator that makes Cartesian product of two <code>Int8Iterator</code>
 * iterators and converts the result pairs into the <code>short</code> type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Int8x8Iterator extends Int16Iterator
{
    /** Product iterator. */
    protected ProductIterator iterator = new ProductIterator();
    
    /**
     * Constructor.
     * 
     * @param <code>hi</code> the iterator of the upper part.
     * @param <code>lo</code> the iterator of the lower part.
     */
    public Int8x8Iterator(Int8Iterator hi, Int8Iterator lo)
    {
        iterator.registerIterator(hi);
        iterator.registerIterator(lo);
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected Int8x8Iterator(Int8x8Iterator r)
    {
        iterator = r.iterator.clone();
    }
    
    /** Initializes the iterator. */
    public void init()
    {
        iterator.init();
    }
    
    /**
     * Checks if the iterator is not exhausted (value is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasValue()
    {
        return iterator.hasValue();
    }
    
    /**
     * Returns the current value of the iterator.
     * 
     * @return the current value of the iterator.
     */
    public short int16Value()
    {
        short hi = ((Int8Iterator)iterator.iterator(0)).int8Value();
        short lo = ((Int8Iterator)iterator.iterator(1)).int8Value();
        
        return (short)((hi << 8) | (lo & 0xff));
    }
    
    /** Makes the iteration. */
    public void next()
    {
        iterator.next();
    }
    
    /** Stops the iterator. */
    public void stop()
    {
        iterator.stop();
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Int8x8Iterator clone()
    {
        return new Int8x8Iterator(this);
    }
}
