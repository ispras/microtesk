/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Div32NearestRoundIterator.java,v 1.1 2008/08/20 17:18:48 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.div;

import com.unitesk.testfusion.core.type.SingleType;

/**
 * Class <code>Div32NearestRoundIterator</code> implements iteration of
 * hard-to-round cases for the single-precision division operation in
 * round-to-nearest mode.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Div32NearestRoundIterator extends Div32Adapter
{
    /** Default constructor. */
    public Div32NearestRoundIterator()
    {
        super(new DivNearestRoundIterator(SingleType.PRECISION));
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to direct round iterator.
     */
    protected Div32NearestRoundIterator(Div32NearestRoundIterator r)
    {
        super(r.iterator.clone());
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Div32NearestRoundIterator clone()
    {
        return new Div32NearestRoundIterator(this);
    }
}
