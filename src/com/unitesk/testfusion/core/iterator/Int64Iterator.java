/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Int64Iterator.java,v 1.3 2008/08/15 07:20:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Abstract class that represents iterator of <code>long</code> values.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Int64Iterator implements Iterator 
{
    /**
     * Returns the current value of iterator.
     * 
     * @return the current value of iterator.
     */
    public abstract long int64Value();
    
    /**
     * Returns the current value of iterator as <code>Long</code> object.
     * 
     * @return the current value of iterator as <code>Long</code> object.
     */
    public Object value()
    {
        return new Long(int64Value());
    }

    /**
     * Returns a string representation of the current value.
     * 
     * @return a string representation of the current value.
     */
    public String toString()
    {
        return Long.toString(int64Value());
    }
    
    /**
     * Returns a hexadecimal string representation of the current value.
     * 
     * @return a hexadecimal string representation of the current value.
     */
    public String toHexString()
    {
        return Long.toHexString(int64Value());
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public abstract Int64Iterator clone();
}
