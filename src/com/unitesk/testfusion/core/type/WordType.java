/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: WordType.java,v 1.5 2009/11/09 14:53:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.type;

import com.unitesk.testfusion.core.model.ContentType;

/**
 * Class <code>WordType</code> represents word type, which consists of 32-bit
 * integer values.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class WordType extends ContentType
{
    /** Minimum value of the type. */
    public static final int MIN_VALUE = Integer.MIN_VALUE;

    /** Maximum value of the type. */
    public static final int MAX_VALUE = Integer.MAX_VALUE;
    
    /** Singleton instance of the <code>WordType</code> class. */
    public static final WordType TYPE = new WordType();

    /** Returns width of the type. */
    public static int getTypeWidth()
    {
        return 32;
    }
    
    /**
     * Checks if the value is single or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is single; <code>false</code>
     *         otherwise.
     */
    public static boolean isWord(long value)
    {
        return value >= MIN_VALUE && value <= MAX_VALUE; 
    }
    
    /**
     * Returns the upper half of the value.
     * 
     * @param  <code>value</code> the value.
     * 
     * @return the upper half of the value.
     */
    public static final short getHiHalfWord(int value)
    {
        return (short)(value >>> 16);
    }
    
    /**
     * Returns the lower half of the value.
     * 
     * @param  <code>value</code> the value.
     * 
     * @return the lower half of the value.
     */
    public static final short getLoHalfWord(long value)
    {
        return (short)(value & 0xffff);
    }
    
    /**
     * Creates the word.
     * 
     * @param  <code>hi</code> the upper half.
     * 
     * @param  <code>lo</code> the lower half.
     * 
     * @return the created word.
     */
    public static final int createWord(short hi, short lo)
    {
        return (hi << 16) | (lo & 0xffff);
    }
    
    /** Default constructor. */
    private WordType()
    {
        super("Word", IntegerType.TYPE(32), true);        
    }
    
    /**
     * Checks if the value is single or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is single; <code>false</code>
     *         otherwise.
     */
    public boolean checkType(long value)
    {
        return isWord(value);
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
