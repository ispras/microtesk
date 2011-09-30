/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Sqrt32Adapter.java,v 1.1 2008/08/20 17:19:02 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.sqrt;

import com.unitesk.testfusion.core.generator.Random;
import com.unitesk.testfusion.core.iterator.Int64Iterator;
import com.unitesk.testfusion.core.iterator.SingleIterator;
import com.unitesk.testfusion.core.type.SingleType;

/**
 * Adapts <code>FractionIterator</code> to <code>SingleIterator</code>.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Sqrt32Adapter extends SingleIterator
{
    /** Iterator of operand fraction. */
    protected Int64Iterator iterator;

    /**
     * Constructor.
     * 
     * @param <code>iterator</code> the iterator of operand fraction.
     */
    public Sqrt32Adapter(Int64Iterator iterator)
    {
        this.iterator = iterator;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to fraction iterator object.
     */
    public Sqrt32Adapter(Sqrt32Adapter r)
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
    public float singleValue()
    {
        int sign = Random.int32_non_negative_less(2);
        int exponent = Random.int32_range(SingleType.MIN_NORMALIZED_EXPONENT, SingleType.MAX_NORMALIZED_EXPONENT);
        int fraction = (int)iterator.int64Value();
        
        return SingleType.createSingle(sign, exponent, fraction);
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
    public Sqrt32Adapter clone()
    {
        return new Sqrt32Adapter(this);
    }

}
