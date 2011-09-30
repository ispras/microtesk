/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SingleType.java,v 1.8 2009/11/09 14:53:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.type;

import com.unitesk.testfusion.core.model.ContentType;

/**
 * Class <code>SingleType</code> represents single precision floating-point type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class SingleType extends ContentType
{
    /** Precision (width of fraction plus one). */
    public static final int PRECISION = 24;
    
    /** Width of exponent. */
    public static final int EXPONENT_LENGTH = 8;

    /** Width of fraction. */
    public static final int FRACTION_LENGTH	= PRECISION - 1;

    /** Bit mask for getting a sign. */
    public static final int SIGN_MASK = 0x80000000;

    /** Bit mask for getting an exponent. */
    public static final int EXPONENT_MASK = 0x7f800000;

    /** Bit mask for getting a fraction. */
    public static final int FRACTION_MASK = 0x7fffff;

    /** Bit mask for getting the upper bit of a fraction. */
    public static final int FRACTION_UPPER_BIT_MASK = 1 << (FRACTION_LENGTH - 1);
    
    /** Maximum value of fraction. */
    public static final int MAX_FRACTION = FRACTION_MASK;

    /** Maximum value of exponent. */
    public static final int MAX_EXPONENT = 0xff;

    /** Maximum value of exponent for normalized numbers. */
    public static final int MAX_NORMALIZED_EXPONENT = MAX_EXPONENT - 1;

    /** Minimum value of exponent for normalized numbers. */
    public static final int MIN_NORMALIZED_EXPONENT = 0x1;

    /** Exponent bias. */
    public static final int BIAS = 0x7f;
    
    /** Plus zero. */
    public static final float PLUS_ZERO = createSingle(0, 0, 0);
    
    /** Minus zero. */
    public static final float MINUS_ZERO = createSingle(1, 0, 0);

    /** Plus infinity. */
    public static final float PLUS_INFINITY = Float.POSITIVE_INFINITY;
    
    /** Minus infinity. */
    public static final float MINUS_INFINITY = Float.NEGATIVE_INFINITY;
    
    /** Singleton instance of the <code>SingleType</code> class. */
    public static final SingleType TYPE = new SingleType();
    
    /** Returns width of the type. */
    public static int getTypeWidth()
    {
        return WordType.getTypeWidth();
    }
    
    /**
     * Checks if the value is single or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is single; <code>false</code>
     *         otherwise.
     */
    public boolean isSingle(long value)
    {
        return WordType.isWord(value);
    }
    
    /**
     * Checks if the value is normalized.
     * 
     * @param  <code>value</code> the value to be checked.

     * @return <code>true</code> if the value is normalized; <code>false</code>
     *         otherwise.
     */
    public static boolean isNormalized(float value)
    {
        int bits = Float.floatToRawIntBits(value);
        int exp = (bits >>> FRACTION_LENGTH) & MAX_EXPONENT;
                
        return exp <= MAX_NORMALIZED_EXPONENT & exp >= MIN_NORMALIZED_EXPONENT;
    }
    
    /**
     * Checks if the value is denormalized.
     * 
     * @param  <code>value</code> the value to be checked.

     * @return <code>true</code> if the value is denormalized; <code>false</code>
     *         otherwise.
     */
    public static boolean isDenormalized(float value)
    {
        int bits = Float.floatToRawIntBits(value);
        int exp = (bits >>> FRACTION_LENGTH) & MAX_EXPONENT;
        int fraction = bits & MAX_FRACTION;
        
        return exp == 0x0 && fraction != 0x0;
    }
    
    /**
     * Checks if the value is plus or minus infinity.
     * 
     * @param  <code>value</code> the value to be checked.

     * @return <code>true</code> if the value is plus or minus infinity;
     *         <code>false</code> otherwise.
     */
    public static boolean isInfinity(float value)
    {
        return Float.isInfinite(value);
    }
    
    /**
     * Checks if the value is plus infinity.
     * 
     * @param  <code>value</code> the value to be checked.

     * @return <code>true</code> if the value is plus infinity;
     *         <code>false</code> otherwise.
     */
    public static boolean isPlusInfinity(float value)
    {
        return value == PLUS_INFINITY;
    }
    
    /**
     * Checks if the value is minus infinity.
     * 
     * @param  <code>value</code> the value to be checked.

     * @return <code>true</code> if the value is plus infinity;
     *         <code>false</code> otherwise.
     */
    public static boolean isMinusInfinity(float value)
    {
        return value == MINUS_INFINITY;
    }
    
    /**
     * Checks if the value is NaN (Not a Number).
     * 
     * @param  <code>value</code> the value to be checked.

     * @return <code>true</code> if the value is NaN; <code>false</code>
     *         otherwise.
     */
    public static boolean isNaN(float value)
    {
        return Float.isNaN(value);
    }
    
    /**
     * Checks if the value is plus or minus zero.
     * 
     * @param  <code>value</code> the value to be checked.

     * @return <code>true</code> if the value is plus or minus zero;
     *         <code>false</code> otherwise.
     */
    public static boolean isZero(float value)
    {
        return value == 0.0;
    }
    
    /**
     * Returns the sign of the value.
     * 
     * @param  <code>value</code> the value.
     * @return the sign of the value.
     */
    public static int getSign(float value)
    {
        int bits = Float.floatToRawIntBits(value);
        
        return (bits & SIGN_MASK)!= 0 ? 1 : 0;
    }
    
    /**
     * Returns the exponent of the value.
     * 
     * @param  <code>value</code> the value.
     * @return the exponent of the value.
     */
    public static int getExponent(float value)
    {
        int bits = Float.floatToRawIntBits(value);
        
        return (bits >>> FRACTION_LENGTH) & MAX_EXPONENT;
    }
    
    /**
     * Returns the fraction of the value.
     * 
     * @param  <code>value</code> the value.
     * @return the exponent of the value.
     */
    public static int getFraction(float value)
    {
        int bits = Float.floatToRawIntBits(value);
        
        return bits & MAX_FRACTION;    
    }
    
    /**
     * Returns the position the lower unit in the value's fraction.
     * 
     * @param  <code>value</code> the value.
     * @return the position the lowest unit in the value's fraction if it exist;
     *         <code>-1</code> otherwise.
     */
    public static int getLowerUnitPosition(int value)
    {
        int i = 0;
        
        if(value == 0)
            return -1;
        
        for(i = 0; i <= 22; i++)
        {
            if(((value >>> i) & 0x1) == 0x1)
                break;
        }
        
        return i;
    }
    
    /**
     * Returns the position the upper unit in the value's fraction.
     * 
     * @param  <code>value</code> the value.
     * @return the position the upper unit in the value's fraction. if it
     *         exists; <code>-1</code> otherwise
     */
    public static int getUpperUnitPosition(int value)
    {
        int i = 0;
        
        if(value == 0)
            return -1;
        
        for(i = 22; i >= 0; i--)
        {
            if (((value >>> i) & 0x1) == 0x1)
                break;
        }
        
        return 22 - i;
    }
    
    /**
     * Creates the single-precision floating-point number.
     * 
     * @param  <code>sign</code> the sign.
     * 
     * @param  <code>exp</code> the exponent.
     * 
     * @param  <code>fraction</code> the fraction.
     * 
     * @return the created single-precision floating-point number.
     */
    public static float createSingle(int sign, int exp, int fraction) 
    {
        int bits = (sign << 31) | (exp << 23) | fraction;
        
        try {
            if(sign > 1 || exp > MAX_EXPONENT || fraction > MAX_FRACTION)
                throw new Exception();
        }
        catch(Exception e)
        {            
        }
        
        return Float.intBitsToFloat(bits);
    }
    
    /**
     * Creates the single-precision floating-point number.
     * 
     * @param  <code>bits</code> the bit representation of the number.
     * @return the created single-precision floating-point number.
     */
    public static float createSingle(int bits)
    {
        return Float.intBitsToFloat(bits);
    }
    
    /** Default constructor. */
    private SingleType()
    {
        super("Single", WordType.TYPE);
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
        return isSingle(value);
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
        int intValue = (int)value;
        
        return Float.toString(Float.intBitsToFloat(intValue)) +
            " (0x" + Integer.toHexString(intValue) + ")";
    }

    /** Returns width of the type. */
    public int getWidth()
    {
        return getTypeWidth();
    }
}
