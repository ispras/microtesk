/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SingleSpecialIterator.java,v 1.4 2008/08/18 08:08:36 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.special;

import com.unitesk.testfusion.core.iterator.BooleanIterator;
import com.unitesk.testfusion.core.iterator.Int32ArrayIterator;
import com.unitesk.testfusion.core.iterator.ProductIterator;
import com.unitesk.testfusion.core.iterator.SingleIterator;
import com.unitesk.testfusion.core.type.SingleType;

/**
 * Iterator of special values for <code>float</code> type.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class SingleSpecialIterator extends SingleIterator
{
    /**
     * Iterator of exponent for the <code>float</code> type.
     * 
     * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
     */
    public static class ExponentIterator extends Int32ArrayIterator
    {
        /** Array of exponent values. */
        public static final int[] values =
        {
            0x00, 
            0x01, 
            0x7f, 
            0xfe,
            0xff
        };

        /** Default constructor. */
        public ExponentIterator()
        {
            super(values);
        }
    }

    /**
     * Iterator of fraction for the <code>float</code> type.
     * 
     * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
     */
    public static class FractionIterator extends Int32ArrayIterator
    {
        /** Array of fraction values. */
        public static final int[] values =
        {
            0x000000,
            0x000001, 
            0x7ffffe, 
            0x7fffff
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
    public SingleSpecialIterator()
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
    protected SingleSpecialIterator(SingleSpecialIterator r)
    {
        productIterator = r.productIterator.clone();
    }

    /** Initializes the iterator. */
    public void init()
    {
        productIterator.init();
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

    /**
     * Returns the current value of the iterator.
     * 
     * @return the current value of the iterator.
     */
    public float singleValue()
    {
        boolean sign     = ((Boolean)productIterator.value(0)).booleanValue();
        int     exp      = ((Integer)productIterator.value(1)).intValue();
        int     fraction = ((Integer)productIterator.value(2)).intValue();

        return SingleType.createSingle(sign == true ? 1 : 0, exp, fraction);
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
    public SingleSpecialIterator clone()
    {
        return new SingleSpecialIterator(this);
    }
}
