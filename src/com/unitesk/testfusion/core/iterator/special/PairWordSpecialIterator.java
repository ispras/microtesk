/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: PairWordSpecialIterator.java,v 1.3 2008/08/15 07:20:20 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.special;

import com.unitesk.testfusion.core.iterator.Int32x32Iterator;

/**
 * Iterator of special values for pairs of 32-bit integer numbers.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class PairWordSpecialIterator extends Int32x32Iterator
{
    /** Default constructor. */
    public PairWordSpecialIterator()
    {
        super(new Int32SpecialIterator(), new Int32SpecialIterator());
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected PairWordSpecialIterator(PairWordSpecialIterator r)
    {
        super(r);
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public PairWordSpecialIterator clone()
    {
        return new PairWordSpecialIterator(this);
    }
}
