/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Int13SpecialIterator.java,v 1.3 2008/08/15 07:20:20 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.special;

import com.unitesk.testfusion.core.iterator.Int16ArrayIterator;

/**
 * Iterator of special values for the 13-bit integer type.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class Int13SpecialIterator extends Int16ArrayIterator
{
    /** Maximum value of the 13-bit integer. */
    public final static short BITS13_MAX_VALUE =  4095;

    /** Minimum value of the 13-bit integer. */
    public final static short BITS13_MIN_VALUE = -4096;
    
    /** Array of values. */
    protected static final short[] values =
    {
        BITS13_MIN_VALUE, 
        BITS13_MIN_VALUE + 1,

        BITS13_MIN_VALUE + Byte.MAX_VALUE - 1,
        BITS13_MIN_VALUE + Byte.MAX_VALUE,
        BITS13_MIN_VALUE + Byte.MAX_VALUE + 1,
        BITS13_MIN_VALUE + Byte.MAX_VALUE + 2,

       -2, 
       -1, 
        0, 
        1,

        BITS13_MAX_VALUE - Byte.MAX_VALUE - 2,
        BITS13_MAX_VALUE - Byte.MAX_VALUE - 1,
        BITS13_MAX_VALUE - Byte.MAX_VALUE,
        BITS13_MAX_VALUE - Byte.MAX_VALUE + 1,

        BITS13_MAX_VALUE - 1, 
        BITS13_MAX_VALUE
    };

    /** Default constructor. */
    public Int13SpecialIterator()
    {
        super(values);
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected Int13SpecialIterator(Int13SpecialIterator r)
    {
        super(r);
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Int13SpecialIterator clone()
    {
        return new Int13SpecialIterator(this);
    }
}
