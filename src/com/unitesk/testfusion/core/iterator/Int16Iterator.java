/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Int16Iterator.java,v 1.3 2008/08/15 07:20:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Abstract class that represents iterator of <code>short</code> values.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Int16Iterator extends Int32Iterator 
{
    /**
     * Returns the current value of iterator.
     * 
     * @return the current value of iterator.
     */
    public abstract short int16Value();
    
    /**
     * Returns the current value of iterator converted into <code>int</code>
     * type.
     * 
     * @return the current value of iterator converted into <code>int</code>
     *         type.
     */
    public int int32Value()
    {
        return int16Value(); 
    }
    
    /**
     * Returns the current value of iterator as <code>Short</code> object.
     * 
     * @return the current value of iterator as <code>Short</code> object.
     */
    public Object value()
    {
        return new Short(int16Value());
    }
    
    /**
     * Returns a string representation of the current value.
     * 
     * @return a string representation of the current value.
     */
    public String toString()
    {
        return Integer.toString(int16Value());
    }
    
    /**
     * Returns a hexadecimal string representation of the current value.
     * 
     * @return a hexadecimal string representation of the current value.
     */
    public String toHexString()
    {
        String res = Integer.toHexString(int16Value());
        
        return "0x" + (int16Value() >= 0 ? res : res.substring(4));
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public abstract Int16Iterator clone();
}
