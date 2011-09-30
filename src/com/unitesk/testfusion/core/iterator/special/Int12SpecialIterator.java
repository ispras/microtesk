/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Int12SpecialIterator.java,v 1.3 2008/08/15 07:20:20 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.special;

import com.unitesk.testfusion.core.iterator.Int16ArrayIterator;

/**
 * Iterator of special values for the 12-bit integer type.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class Int12SpecialIterator extends Int16ArrayIterator
{
    /** Maximum value of the 12-bit integer. */
    public final static short BITS12_MAX_VALUE =  2047;

    /** Minimum value of the 12-bit integer. */
    public final static short BITS12_MIN_VALUE = -2048;
    
    /** Array of values. */
    protected static final short[] values =
    {
        BITS12_MIN_VALUE, 
        BITS12_MIN_VALUE + 1,

        BITS12_MIN_VALUE + Byte.MAX_VALUE - 1,
        BITS12_MIN_VALUE + Byte.MAX_VALUE,
        BITS12_MIN_VALUE + Byte.MAX_VALUE + 1,
        BITS12_MIN_VALUE + Byte.MAX_VALUE + 2,

       -2, 
       -1, 
        0, 
        1,

        BITS12_MAX_VALUE - Byte.MAX_VALUE - 2,
        BITS12_MAX_VALUE - Byte.MAX_VALUE - 1,
        BITS12_MAX_VALUE - Byte.MAX_VALUE,
        BITS12_MAX_VALUE - Byte.MAX_VALUE + 1,

        BITS12_MAX_VALUE - 1, 
        BITS12_MAX_VALUE
    };

    /** Default constructor. */
    public Int12SpecialIterator()
    {
        super(values);
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected Int12SpecialIterator(Int12SpecialIterator r)
    {
        super(r);
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Int12SpecialIterator clone()
    {
        return new Int12SpecialIterator(this);
    }
}
