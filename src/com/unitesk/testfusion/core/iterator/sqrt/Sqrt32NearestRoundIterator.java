/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Sqrt32NearestRoundIterator.java,v 1.1 2008/08/20 17:19:02 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.sqrt;

import com.unitesk.testfusion.core.type.SingleType;

/**
 * Class <code>Sqrt32NearestRoundIterator</code> implements iteration of
 * hard-to-round cases for the single-precision square root in
 * round-to-nearest mode.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Sqrt32NearestRoundIterator extends Sqrt32Adapter
{
    /** Default constructor. */
    public Sqrt32NearestRoundIterator()
    {
        super(new SqrtNearestRoundIterator(SingleType.PRECISION));
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to direct round iterator.
     */
    protected Sqrt32NearestRoundIterator(Sqrt32NearestRoundIterator r)
    {
        super(r.iterator.clone());
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Sqrt32NearestRoundIterator clone()
    {
        return new Sqrt32NearestRoundIterator(this);
    }

}
