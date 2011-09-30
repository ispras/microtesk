/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: DoubleIterator.java,v 1.2 2008/08/14 12:29:32 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Abstract class that represents iterator of <code>double</code> values.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class DoubleIterator extends Int64Iterator
{
    /**
     * Returns the current value of iterator.
     * 
     * @return the current value of iterator.
     */
    public abstract double doubleValue();
    
    /**
     * Returns the current value of iterator as <code>Double</code> object.
     * 
     * @return the current value of iterator as <code>Double</code> object.
     */
    public Object value()
    {
        return new Double(doubleValue());
    }
    
    /**
     * Returns the bit representation of the current value.
     * 
     * @return the bit representation of the current value.
     */
    public long int64Value()
    {
        return Double.doubleToRawLongBits(doubleValue());
    }
}
