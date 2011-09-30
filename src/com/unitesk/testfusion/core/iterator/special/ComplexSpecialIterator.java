/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ComplexSpecialIterator.java,v 1.3 2008/08/15 07:20:20 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.special;

import com.unitesk.testfusion.core.iterator.Int32x32Iterator;

/**
 * Iterator of special values for complex single-precision numbers.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class ComplexSpecialIterator extends Int32x32Iterator
{
    /** Default constructor. */
    public ComplexSpecialIterator()
    {
        super(new SingleSpecialIterator(), new SingleSpecialIterator());
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected ComplexSpecialIterator(ComplexSpecialIterator r)
    {
        super(r);
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public ComplexSpecialIterator clone()
    {
        return new ComplexSpecialIterator(this);
    }
}
