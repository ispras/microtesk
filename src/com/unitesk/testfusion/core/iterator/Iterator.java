/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Iterator.java,v 1.7 2009/08/06 13:04:24 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Interface of iterator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface Iterator extends Cloneable
{
    /** Initializes iterator. */
    public void init();

    /**
     * Checks if the iterator is not exhausted (value is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasValue();
        
    /**
     * Returns the current value of iterator.
     * 
     * @return the current value of iterator.
     */
    public Object value();
    
    /** Makes iteration. */
    public void next();

    /** Stops iterator. */
    public void stop();
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public Iterator clone();
}
