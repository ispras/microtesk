/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: PairWordType.java,v 1.6 2009/11/09 14:53:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.type;

import com.unitesk.testfusion.core.model.ContentType;

/**
 * Class <code>PairWordType</code> represents pair-word type, which consists of
 * pairs of words.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class PairWordType extends ContentType
{
    /** Singleton instance of the <code>PairWordType</code> class. */
    public static final PairWordType TYPE = new PairWordType();

    /** Returns width of the type. */
    public static int getTypeWidth()
    {
        return 2 * WordType.getTypeWidth();
    }
    
    /**
     * Checks if the value is pair-word or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is pair-word; <code>false</code>
     *         otherwise.
     */
    public static boolean isPairWord(long value)
    {
        return true; 
    }
    
    /**
     * Returns the upper word of the value.
     * 
     * @param  <code>value</code> the value.
     * 
     * @return the upper word of the value.
     */
    public static final int getHiWord(long value)
    {
        return (int)(value >>> 32);
    }
    
    /**
     * Returns the lower word of the value.
     * 
     * @param  <code>value</code> the value.
     * 
     * @return the lower word of the value.
     */
    public static final int getLoWord(long value)
    {
        return (int)(value & 0xffffffffL);
    }
    
    /**
     * Creates the pair of words.
     * @param  <code>hi</code> the upper word.
     * 
     * @param  <code>lo</code> the lower word.
     * 
     * @return the created pair of words.
     */
    public static final long createDoubleWord(int hi, int lo)
    {
        return ((long)hi << 32) | ((long)lo & 0xffffffffL);
    }
    
    /** Default constructor. */
    private PairWordType()
    {
        super("Pair Word", DoubleWordType.TYPE);        
    }
    
    /**
     * Checks if the value is pair-word or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is pair-word; <code>false</code>
     *         otherwise.
     */
    public boolean checkType(long value)
    {
        return isPairWord(value);
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
        int hi = getHiWord(value);
        int lo = getLoWord(value);
        
        return "0x" + Integer.toHexString(hi) + ", " +
               "0x" + Integer.toHexString(lo);
    }

    /** Returns width of the type. */
    public int getWidth()
    {
        return getTypeWidth();
    }
}
