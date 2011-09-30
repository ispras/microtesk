/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: DoubleSpecialIterator.java,v 1.4 2008/08/18 08:08:36 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.special;

import com.unitesk.testfusion.core.iterator.BooleanIterator;
import com.unitesk.testfusion.core.iterator.DoubleIterator;
import com.unitesk.testfusion.core.iterator.Int64ArrayIterator;
import com.unitesk.testfusion.core.iterator.ProductIterator;
import com.unitesk.testfusion.core.type.DoubleType;

/**
 * Iterator of special values for the <code>double</code> type.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class DoubleSpecialIterator extends DoubleIterator
{
    /**
     * Iterator of exponent for the <code>double</code> type.
     * 
     * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
     */
    public static class ExponentIterator extends Int64ArrayIterator
    {
        /** Array of exponent values. */
        protected static final long[] values =
        {
            0x000, 
            0x001, 
            0x3ff, 
            0x7fe, 
            0x7ff
        };

        /** Default constructor. */
        public ExponentIterator()
        {
            super(values);
        }
    }

    /**
     * Iterator of fraction for the <code>double</code> type.
     * 
     * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
     */
    public static class FractionIterator extends Int64ArrayIterator
    {
        /** Array of fraction values. */
        public static final long[] values =
        {
            0x0000000000000L,
            0x0000000000001L, 
            0xffffffffffffeL,
            0xfffffffffffffL
        };

        /** Default constructor. */
        public FractionIterator()
        {
            super(values);
        }
    }

    /** Product iterator. */
    protected ProductIterator productIterator = new ProductIterator();

    /** Default constructor. */
    public DoubleSpecialIterator()
    {
        productIterator.registerIterator(new BooleanIterator());
        productIterator.registerIterator(new ExponentIterator());
        productIterator.registerIterator(new FractionIterator());
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected DoubleSpecialIterator(DoubleSpecialIterator r)
    {
        productIterator = r.productIterator;
    }

    /** Initializes the iterator. */
    public void init()
    {
        productIterator.init();
    }

    /**
     * Returns the current value of the iterator.
     * 
     * @return the current value of the iterator.
     */
    public double doubleValue()
    {
        Boolean sign     = ((Boolean)productIterator.value(0)).booleanValue();
        Long    exp      = ((Long)productIterator.value(1)).longValue();
        Long    fraction = ((Long)productIterator.value(2)).longValue();

        return DoubleType.createDouble(sign == true ? 1 : 0, exp, fraction);
    }

    /**
     * Checks if the iterator is not exhausted (value is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasValue()
    {
        return productIterator.hasValue();
    }

    /** Makes iteration. */
    public void next()
    {
        productIterator.next();
    }

    /** Stops the iterator. */
    public void stop()
    {
        productIterator.stop();
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public DoubleSpecialIterator clone()
    {
        return new DoubleSpecialIterator(this);
    }
}
