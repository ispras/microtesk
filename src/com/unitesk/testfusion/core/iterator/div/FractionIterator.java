/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: FractionIterator.java,v 1.1 2008/08/20 17:18:48 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.div;

import com.unitesk.testfusion.core.iterator.Iterator;

/**
 * Iterator of fractions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface FractionIterator extends Iterator
{
    /**
     * Returns the current value.
     * 
     * @return the current value.
     */
    public Fraction value();
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public FractionIterator clone();
}
