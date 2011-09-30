/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Sqrt32DirectRoundIterator.java,v 1.1 2008/08/20 17:19:02 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.sqrt;

import com.unitesk.testfusion.core.type.SingleType;

/**
 * Class <code>Sqrt32DirectRoundIterator</code> implements iteration of
 * hard-to-round cases for the single-precision square root in
 * direct rounding mode.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Sqrt32DirectRoundIterator extends Sqrt32Adapter
{
    /** Default constructor. */
    public Sqrt32DirectRoundIterator()
    {
        super(new SqrtDirectRoundIterator(SingleType.PRECISION));
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to direct round iterator.
     */
    protected Sqrt32DirectRoundIterator(Sqrt32DirectRoundIterator r)
    {
        super(r.iterator.clone());
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Sqrt32DirectRoundIterator clone()
    {
        return new Sqrt32DirectRoundIterator(this);
    }

}
