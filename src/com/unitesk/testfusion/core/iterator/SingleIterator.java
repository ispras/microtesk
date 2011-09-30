/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SingleIterator.java,v 1.3 2008/08/15 07:20:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Abstract class that represents iterator of <code>single</code> values.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class SingleIterator extends Int32Iterator
{
    /**
     * Returns the current value of iterator.
     * 
     * @return the current value of iterator.
     */
    public abstract float singleValue();
    
    /**
     * Returns the bit representation of the current value.
     * 
     * @return the bit representation of the current value.
     */
    public int int32Value()
    {
        return Float.floatToRawIntBits(singleValue());
    }

    /**
     * Returns the current value of iterator as <code>Float</code> object.
     * 
     * @return the current value of iterator as <code>Float</code> object.
     */
    public Object value()
    {
        return new Float(singleValue());
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public abstract SingleIterator clone();
}
