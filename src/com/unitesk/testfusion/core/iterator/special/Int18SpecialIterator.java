/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Int18SpecialIterator.java,v 1.3 2008/08/15 07:20:20 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.special;

import com.unitesk.testfusion.core.iterator.Int32ArrayIterator;

/**
 * Iterator of special values for the 18-bit integer type.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class Int18SpecialIterator extends Int32ArrayIterator
{
    /** Maximum value of the 18-bit integer. */
    public final static int BITS18_MAX_VALUE =  131071;
    
    /** Minimum value of the 18-bit integer. */
    public final static int BITS18_MIN_VALUE = -131072;

    /** Array of values. */
    public static final int[] values =
    {
        BITS18_MIN_VALUE, 
        BITS18_MIN_VALUE + 1,

        BITS18_MIN_VALUE + Byte.MAX_VALUE - 1,
        BITS18_MIN_VALUE + Byte.MAX_VALUE,
        BITS18_MIN_VALUE + Byte.MAX_VALUE + 1,
        BITS18_MIN_VALUE + Byte.MAX_VALUE + 2,

        BITS18_MIN_VALUE + Short.MAX_VALUE - 1,
        BITS18_MIN_VALUE + Short.MAX_VALUE,
        BITS18_MIN_VALUE + Short.MAX_VALUE + 1,
        BITS18_MIN_VALUE + Short.MAX_VALUE + 2,

       -2,
       -1, 
        0, 
        1,

        BITS18_MAX_VALUE - Short.MAX_VALUE - 2,
        BITS18_MAX_VALUE - Short.MAX_VALUE - 1,
        BITS18_MAX_VALUE - Short.MAX_VALUE,
        BITS18_MAX_VALUE - Short.MAX_VALUE + 1,

        BITS18_MAX_VALUE - Byte.MAX_VALUE - 2,
        BITS18_MAX_VALUE - Byte.MAX_VALUE - 1,
        BITS18_MAX_VALUE - Byte.MAX_VALUE,
        BITS18_MAX_VALUE - Byte.MAX_VALUE + 1,

        BITS18_MAX_VALUE - 1,
        BITS18_MAX_VALUE
    };

    /** Default constructor. */
    public Int18SpecialIterator()
    {
        super(values);
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected Int18SpecialIterator(Int18SpecialIterator r)
    {
        super(r);
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Int18SpecialIterator clone()
    {
        return new Int18SpecialIterator(this);
    }
}
