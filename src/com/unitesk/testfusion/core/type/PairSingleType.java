/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: PairSingleType.java,v 1.6 2009/11/09 14:53:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.type;

import com.unitesk.testfusion.core.model.ContentType;

/**
 * Class <code>PairSingleType</code> represents pair-single type, which consists
 * of pairs of singles.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class PairSingleType extends ContentType
{
    /** Singleton instance of the <code>PairSingleType</code> class. */
    public static final PairSingleType TYPE = new PairSingleType();

    /** Returns width of the type. */
    public static int getTypeWidth()
    {
        return 2 * SingleType.getTypeWidth();
    }
    
    /**
     * Checks if the value is pair-single or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is pair-single; <code>false</code>
     *         otherwise.
     */
    public static boolean isPairSingle(long value)
    {
        return true; 
    }
    
    /**
     * Returns the upper single of the value.
     * 
     * @param  <code>value</code> the value.
     * 
     * @return the upper single of the value.
     */
    public static float getHiSingle(long value)
    {
        return Float.intBitsToFloat(PairWordType.getHiWord(value));
    }
    
    /**
     * Returns the lower single of the value.
     * 
     * @param  <code>value</code> the value.
     * 
     * @return the lower single of the value.
     */
    public static float getLoSingle(long value)
    {
        return Float.intBitsToFloat(PairWordType.getLoWord(value));
    }
    
    /**
     * Creates the pair of singles.
     * 
     * @param  <code>hi</code> the upper single.
     * 
     * @param  <code>lo</code> the lower single.
     * 
     * @return the created pair of singles.
     */
    public static long createPairSingle(float hi, float lo)
    {
        return PairWordType.createDoubleWord(Float.floatToRawIntBits(hi), Float.floatToRawIntBits(lo));
    }
    
    /** Default constructor. */
    private PairSingleType()
    {
        super("Pair Single", DoubleWordType.TYPE);        
    }
    
    /**
     * Checks if the value is pair-single or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is pair-single; <code>false</code>
     *         otherwise.
     */
    public boolean checkType(long value)
    {
        return isPairSingle(value);
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
        int hi = PairWordType.getHiWord(value);
        int lo = PairWordType.getLoWord(value);
        
        return Float.toString(Float.intBitsToFloat(hi)) +
               " (0x" + Integer.toHexString(hi) + "), " +
               Float.toString(Float.intBitsToFloat(lo)) +
               " (0x" + Integer.toHexString(lo) + ")";
    }

    /** Returns width of the type. */
    public int getWidth()
    {
        return getTypeWidth();
    }
}
