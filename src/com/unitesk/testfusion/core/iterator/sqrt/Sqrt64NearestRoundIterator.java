/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Sqrt64NearestRoundIterator.java,v 1.1 2008/08/20 17:19:02 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.sqrt;

import com.unitesk.testfusion.core.type.DoubleType;

/**
 * Class <code>Sqrt64NearestRoundIterator</code> implements iteration of
 * hard-to-round cases for the single-precision square root in
 * round-to-nearest mode.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Sqrt64NearestRoundIterator extends Sqrt64Adapter
{
    /** Default constructor. */
    public Sqrt64NearestRoundIterator()
    {
        super(new SqrtNearestRoundIterator(DoubleType.PRECISION));
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to direct round iterator.
     */
    protected Sqrt64NearestRoundIterator(Sqrt64NearestRoundIterator r)
    {
        super(r.iterator.clone());
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Sqrt64NearestRoundIterator clone()
    {
        return new Sqrt64NearestRoundIterator(this);
    }

}
