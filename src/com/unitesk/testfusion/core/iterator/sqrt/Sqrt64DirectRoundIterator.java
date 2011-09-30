/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Sqrt64DirectRoundIterator.java,v 1.1 2008/08/20 17:19:02 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.sqrt;

import com.unitesk.testfusion.core.type.DoubleType;

/**
 * Class <code>Sqrt64DirectRoundIterator</code> implements iteration of
 * hard-to-round cases for the double-precision square root in
 * direct rounding mode.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Sqrt64DirectRoundIterator extends Sqrt64Adapter
{
    /** Default constructor. */
    public Sqrt64DirectRoundIterator()
    {
        super(new SqrtDirectRoundIterator(DoubleType.PRECISION));
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to direct round iterator.
     */
    protected Sqrt64DirectRoundIterator(Sqrt64DirectRoundIterator r)
    {
        super(r.iterator.clone());
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Sqrt64DirectRoundIterator clone()
    {
        return new Sqrt64DirectRoundIterator(this);
    }

}
