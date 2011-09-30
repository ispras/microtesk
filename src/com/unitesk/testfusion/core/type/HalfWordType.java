/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: HalfWordType.java,v 1.6 2009/11/09 14:53:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.type;

import com.unitesk.testfusion.core.model.ContentType;
import com.unitesk.testfusion.core.util.Utils;

/**
 * Class <code>HalfWordType</code> represents half word type, which consists of
 * 16-bit integer values.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class HalfWordType extends ContentType
{
    /** Minimum value of the type. */
    public static final int MIN_VALUE = Short.MIN_VALUE;

    /** Maximum value of the type. */
    public static final int MAX_VALUE = Short.MAX_VALUE;
    
    /** Singleton instance of the <code>HalfWordType</code> class. */
    public static final HalfWordType TYPE = new HalfWordType();
    
    /** Returns width of the type. */
    public static int getTypeWidth()
    {
        return 16;
    }
    
    /**
     * Checks if the value is half-word or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is half-word; <code>false</code>
     *         otherwise.
     */
    public static boolean isHalfWord(long value)
    {
        return value >= MIN_VALUE && value <= MAX_VALUE;
    }
    
    /**
     * Returns the upper byte of the value.
     * 
     * @param  <code>value</code> the value.
     * @return the upper byte of the value.
     */
    public static final byte getHiByte(short value)
    {
        return (byte)(value >>> 8);
    }
    
    /**
     * Returns the lower byte of the value.
     * 
     * @param  <code>value</code> the value.
     * @return the lower byte of the value.
     */
    public static final byte getLoByte(short value)
    {
        return (byte)(value & 0xff);
    }
    
    /**
     * Creates the half word.
     * 
     * @param  <code>hi</code> the upper byte.
     * 
     * @param  <code>lo</code> the lower byte.
     * 
     * @return the created half word.
     */
    public static final short createHalfWord(byte hi, byte lo)
    {
        return (short)((hi << 8) | (lo & 0xff));
    }
    
    /** Default constructor. */
    private HalfWordType()
    {
        super("Half Word", WordType.TYPE);
    }
    
    /**
     * Checks if the value is half-word or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is half-word; <code>false</code>
     *         otherwise.
     */
    public boolean checkType(long value)
    {
        return isHalfWord(value);
    }

    /**
     * Returns a string representation of the value.
     * 
     * @param  <code>value</code> the value to be represented as a string.
     * 
     * @return a string representation of the value.
     */
    public String getDescription(long value)
    {
        return "0x" + Utils.toHexString((short)value);
    }

    /** Returns width of the type. */
    public int getWidth()
    {
        return getTypeWidth();
    }
}
