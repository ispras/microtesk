/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Div32Iterator.java,v 1.1 2008/08/20 17:18:48 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.div;

import com.unitesk.testfusion.core.iterator.Iterator;

/**
 * Iterator of operand values for single-precision division.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface Div32Iterator extends Iterator
{
    /**
     * Returns the current dividend.
     * 
     * @return the current dividend.
     */
    public float getDividend();
    
    /**
     * Returns the current divisor.
     * 
     * @return the current divisor.
     */
    public float getDivisor();
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Div32Iterator clone();
}
