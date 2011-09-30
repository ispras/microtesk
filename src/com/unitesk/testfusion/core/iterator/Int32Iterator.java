/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Int32Iterator.java,v 1.3 2008/08/15 07:20:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Abstract class that represents iterator of <code>int</code> values.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Int32Iterator extends Int64Iterator 
{
    /**
     * Returns the current value of iterator.
     * 
     * @return the current value of iterator.
     */
    public abstract int int32Value();
    
    /**
     * Returns the current value of iterator converted to <code>long</code> type.
     * 
     * @return the current value of iterator converted to <code>long</code> type.
     */
    public long int64Value()
    {
        return int32Value(); 
    }
    
    /**
     * Returns the current value of iterator as <code>Integer</code> object.
     * 
     * @return the current value of iterator as <code>Integer</code> object.
     */
    public Object value()
    {
        return new Integer(int32Value());
    }

    /**
     * Returns a string representation of the current value.
     * 
     * @return a string representation of the current value.
     */
    public String toString()
    {
        return Integer.toString(int32Value());
    }
    
    /**
     * Returns a hexadecimal string representation of the current value.
     * 
     * @return a hexadecimal string representation of the current value.
     */
    public String toHexString()
    {
        return Integer.toHexString(int32Value());
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public abstract Int32Iterator clone();
}
