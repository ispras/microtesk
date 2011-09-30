/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Int8Iterator.java,v 1.3 2008/08/15 07:20:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Abstract class that represents iterator of <code>byte</code> values.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Int8Iterator extends Int16Iterator 
{
    /**
     * Returns the current value of iterator.
     * 
     * @return the current value of iterator.
     */
    public abstract byte int8Value();
    
    /**
     * Returns the current value of iterator converted into <code>short</code>
     * type.
     * 
     * @return the current value of iterator converted into <code>short</code>
     *         type.
     */
    public short int16Value()
    {
        return int8Value();
    }

    /**
     * Returns the current value of iterator as <code>Byte</code> object.
     * 
     * @return the current value of iterator as <code>Byte</code> object.
     */
    public Object value()
    {
        return new Byte(int8Value());
    }
    
    /**
     * Returns a string representation of the current value.
     * 
     * @return a string representation of the current value.
     */
    public String toString()
    {
        return Long.toString(int8Value());
    }
    
    /**
     * Returns a hexadecimal string representation of the current value.
     * 
     * @return a hexadecimal string representation of the current value.
     */
    public String toHexString()
    {
        String res = Integer.toHexString(int8Value());
        
        return "0x" + (int8Value() >= 0 ? res : res.substring(6));
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public abstract Int8Iterator clone();
}
