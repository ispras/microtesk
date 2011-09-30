/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Sqrt64Adapter.java,v 1.1 2008/08/20 17:19:02 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.sqrt;

import com.unitesk.testfusion.core.generator.Random;
import com.unitesk.testfusion.core.iterator.DoubleIterator;
import com.unitesk.testfusion.core.iterator.Int64Iterator;
import com.unitesk.testfusion.core.type.DoubleType;

/**
 * Adapts <code>FractionIterator</code> to <code>DoubleIterator</code>.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Sqrt64Adapter extends DoubleIterator
{
    /** Iterator of operand fraction. */
    protected Int64Iterator iterator;

    /**
     * Constructor.
     * 
     * @param <code>iterator</code> the iterator of operand fraction.
     */
    public Sqrt64Adapter(Int64Iterator iterator)
    {
        this.iterator = iterator;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to fraction iterator object.
     */
    public Sqrt64Adapter(Sqrt64Adapter r)
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
     * Returns the current value.
     * 
     * @return the current value.
     */
    public double doubleValue()
    {
        long sign = Random.int64_non_negative_less(2);
        long exponent = Random.int64_range(DoubleType.MIN_NORMALIZED_EXPONENT, DoubleType.MAX_NORMALIZED_EXPONENT);
        long fraction = iterator.int64Value();
        
        return DoubleType.createDouble(sign, exponent, fraction);
    }
    
    /** Makes iteration. */
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
     * Returns a copy of the adapter.
     * 
     * @return a copy of the adapter.
     */
    public Sqrt64Adapter clone()
    {
        return new Sqrt64Adapter(this);
    }

}
