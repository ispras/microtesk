/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Int16SpecialIterator.java,v 1.3 2008/08/15 07:20:20 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.special;

import com.unitesk.testfusion.core.iterator.Int16ArrayIterator;

/**
 * Iterator of special values for the 16-bit integer type.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class Int16SpecialIterator extends Int16ArrayIterator
{
    /** Array of values. */
    public static final short[] values =
    {
        Short.MIN_VALUE, 
        Short.MIN_VALUE + 1,

        Short.MIN_VALUE + Byte.MAX_VALUE - 1,
        Short.MIN_VALUE + Byte.MAX_VALUE,
        Short.MIN_VALUE + Byte.MAX_VALUE + 1,
        Short.MIN_VALUE + Byte.MAX_VALUE + 2,

       -2, 
       -1, 
        0, 
        1,

        Short.MAX_VALUE - Byte.MAX_VALUE - 2,
        Short.MAX_VALUE - Byte.MAX_VALUE - 1,
        Short.MAX_VALUE - Byte.MAX_VALUE,
        Short.MAX_VALUE - Byte.MAX_VALUE + 1,

        Short.MAX_VALUE - 1, 
        Short.MAX_VALUE
    };

    /** Default constructor. */
    public Int16SpecialIterator()
    {
        super(values);
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected Int16SpecialIterator(Int16SpecialIterator r)
    {
        super(r);
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Int16SpecialIterator clone()
    {
        return new Int16SpecialIterator(this);
    }
}
