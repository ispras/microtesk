/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Int8SpecialIterator.java,v 1.3 2008/08/15 07:20:20 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.special;

import com.unitesk.testfusion.core.iterator.Int8ArrayIterator;

/**
 * Iterator of special values for the <code>byte</code> type.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class Int8SpecialIterator extends Int8ArrayIterator
{
    /** Array of values. */
    public static final byte[] values =
    {
        Byte.MIN_VALUE,
        Byte.MIN_VALUE + 1,

       -2, 
       -1,
        0, 
        1,

        Byte.MAX_VALUE - 1, 
        Byte.MAX_VALUE,
    };

    /** Default constructor. */
    public Int8SpecialIterator()
    {
        super(values);
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected Int8SpecialIterator(Int8SpecialIterator r)
    {
        super(r);
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Int8SpecialIterator clone()
    {
        return new Int8SpecialIterator(this);
    }
}
