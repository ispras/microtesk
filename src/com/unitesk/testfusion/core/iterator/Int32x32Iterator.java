/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Int32x32Iterator.java,v 1.4 2008/08/18 08:08:34 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Iterator that makes Cartesian product of two <code>Int32Iterator</code>
 * iterators and converts the result pairs into the <code>long</code> type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Int32x32Iterator extends Int64Iterator
{
    /** Product iterator. */
    protected ProductIterator iterator = new ProductIterator();
    
    /**
     * Constructor.
     * 
     * @param <code>hi</code> the iterator of the upper part.
     * @param <code>lo</code> the iterator of the lower part.
     */
    public Int32x32Iterator(Int32Iterator hi, Int32Iterator lo)
    {
        iterator.registerIterator(hi);
        iterator.registerIterator(lo);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected Int32x32Iterator(Int32x32Iterator r)
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
    public long int64Value()
    {
        long hi = ((Int32Iterator)iterator.iterator(0)).int32Value();
        long lo = ((Int32Iterator)iterator.iterator(1)).int32Value();
        
        return (hi << 32) | (lo & 0xffffffffL);
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
    public Int32x32Iterator clone()
    {
        return new Int32x32Iterator(this);
    }
}
