/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Int64SpecialIterator.java,v 1.3 2008/08/15 07:20:20 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.special;

import com.unitesk.testfusion.core.iterator.Int64ArrayIterator;

/**
 * Iterator of special values for the <code>long</code> type.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class Int64SpecialIterator extends Int64ArrayIterator
{
    /** Array of values. */
    public static final long[] values =
    {
        Long.MIN_VALUE,
        Long.MIN_VALUE + 1,

        Long.MIN_VALUE + Byte.MAX_VALUE - 1,
        Long.MIN_VALUE + Byte.MAX_VALUE,
        Long.MIN_VALUE + Byte.MAX_VALUE + 1,
        Long.MIN_VALUE + Byte.MAX_VALUE + 2,

        Long.MIN_VALUE + Short.MAX_VALUE - 1,
        Long.MIN_VALUE + Short.MAX_VALUE,
        Long.MIN_VALUE + Short.MAX_VALUE + 1,
        Long.MIN_VALUE + Short.MAX_VALUE + 2,

        Long.MIN_VALUE + Integer.MAX_VALUE - 1,
        Long.MIN_VALUE + Integer.MAX_VALUE,
        Long.MIN_VALUE + Integer.MAX_VALUE + 1,
        Long.MIN_VALUE + Integer.MAX_VALUE + 2,

       -2,
       -1, 
        0,
        1,

        Long.MAX_VALUE - Integer.MAX_VALUE - 2,
        Long.MAX_VALUE - Integer.MAX_VALUE - 1,
        Long.MAX_VALUE - Integer.MAX_VALUE,
        Long.MAX_VALUE - Integer.MAX_VALUE + 1,

        Long.MAX_VALUE - Short.MAX_VALUE - 2,
        Long.MAX_VALUE - Short.MAX_VALUE - 1,
        Long.MAX_VALUE - Short.MAX_VALUE,
        Long.MAX_VALUE - Short.MAX_VALUE + 1,

        Long.MAX_VALUE - Byte.MAX_VALUE - 2,
        Long.MAX_VALUE - Byte.MAX_VALUE - 1,
        Long.MAX_VALUE - Byte.MAX_VALUE,
        Long.MAX_VALUE - Byte.MAX_VALUE + 1,

        Long.MAX_VALUE - 1,
        Long.MAX_VALUE
    };

    /** Default constructor. */
    public Int64SpecialIterator()
    {
        super(values);
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected Int64SpecialIterator(Int64SpecialIterator r)
    {
        super(r);
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Int64SpecialIterator clone()
    {
        return new Int64SpecialIterator(this);
    }
}
