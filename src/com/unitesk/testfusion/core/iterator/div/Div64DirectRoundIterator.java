/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Div64DirectRoundIterator.java,v 1.1 2008/08/20 17:18:48 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.div;

import com.unitesk.testfusion.core.type.DoubleType;

/**
 * Class <code>Div64DirectRoundIterator</code> implements iteration of
 * hard-to-round cases for the double-precision division operation in
 * direct rounding mode.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Div64DirectRoundIterator extends Div64Adapter
{
    /** Default constructor. */
    public Div64DirectRoundIterator()
    {
        super(new DivDirectRoundIterator(DoubleType.PRECISION));
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to direct round iterator.
     */
    protected Div64DirectRoundIterator(Div64DirectRoundIterator r)
    {
        super(r.iterator.clone());
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Div64DirectRoundIterator clone()
    {
        return new Div64DirectRoundIterator(this);
    }

}
