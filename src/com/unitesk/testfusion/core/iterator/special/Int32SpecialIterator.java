/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Int32SpecialIterator.java,v 1.2 2008/08/14 12:29:45 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.special;

import com.unitesk.testfusion.core.iterator.Int32ArrayIterator;

/**
 * Iterator of special values for the <code>int</code> type.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class Int32SpecialIterator extends Int32ArrayIterator
{
    /** Array of values. */
    protected static final int[] values =
    {
        Integer.MIN_VALUE, 
        Integer.MIN_VALUE + 1,

        Integer.MIN_VALUE + Byte.MAX_VALUE - 1,
        Integer.MIN_VALUE + Byte.MAX_VALUE,
        Integer.MIN_VALUE + Byte.MAX_VALUE + 1,
        Integer.MIN_VALUE + Byte.MAX_VALUE + 2,

        Integer.MIN_VALUE + Short.MAX_VALUE - 1,
        Integer.MIN_VALUE + Short.MAX_VALUE,
        Integer.MIN_VALUE + Short.MAX_VALUE + 1,
        Integer.MIN_VALUE + Short.MAX_VALUE + 2,

       -2,
       -1, 
        0, 
        1,

        Integer.MAX_VALUE - Short.MAX_VALUE - 2,
        Integer.MAX_VALUE - Short.MAX_VALUE - 1,
        Integer.MAX_VALUE - Short.MAX_VALUE,
        Integer.MAX_VALUE - Short.MAX_VALUE + 1,

        Integer.MAX_VALUE - Byte.MAX_VALUE - 2,
        Integer.MAX_VALUE - Byte.MAX_VALUE - 1,
        Integer.MAX_VALUE - Byte.MAX_VALUE,
        Integer.MAX_VALUE - Byte.MAX_VALUE + 1,

        Integer.MAX_VALUE - 1,
        Integer.MAX_VALUE
    };

    /** Default constructor. */
    public Int32SpecialIterator()
    {
        super(values);
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected Int32SpecialIterator(Int32SpecialIterator r)
    {
        super(r);
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Int32SpecialIterator clone()
    {
        return new Int32SpecialIterator(this);
    }
}
