/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ByteType.java,v 1.5 2009/11/09 14:53:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.type;

import com.unitesk.testfusion.core.model.ContentType;

/**
 * Class <code>ByteType</code> represents byte type, which consists of 8-bit
 * integer values.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class ByteType extends ContentType
{
    /** Minimum value of the type. */
    public static final int MIN_VALUE = Byte.MIN_VALUE;

    /** Maximum value of the type. */
    public static final int MAX_VALUE = Byte.MAX_VALUE;
        
    /** Singleton instance of the <code>ByteType</code> class. */
    public static final ByteType TYPE = new ByteType();
    
    /** Returns width of the type. */
    public static int getTypeWidth()
    {
        return 8;
    }
    
    /**
     * Checks if the value is byte or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is byte; <code>false</code>
     *         otherwise.
     */
    public static boolean isByte(long value)
    {
        return value >= MIN_VALUE && value <= MAX_VALUE;
    }
    
    /** Default constructor. */
    private ByteType()
    {
        super("Byte", IntegerType.TYPE(8), true);
    }
    
    /**
     * Checks if the value is byte or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is byte; <code>false</code>
     *         otherwise.
     */
    public boolean checkType(long value)
    {
        return isByte(value);
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
        return "0x" + Integer.toHexString((int)value);
    }

    /** Returns width of the type. */
    public int getWidth()
    {
        return getTypeWidth();
    }
}
