/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: DoubleType.java,v 1.7 2009/11/09 14:53:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.type;

import com.unitesk.testfusion.core.model.ContentType;

/**
 * Class <code>DoubleType</code> represents double-precision floating-point type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class DoubleType extends ContentType
{
    /** Precision (width of fraction plus one). */
    public static final int  PRECISION = 53;
    
    /** Width of exponent. */
    public static final int  EXPONENT_LENGTH = 11;
    
    /** Width of fraction. */
    public static final int  FRACTION_LENGTH = PRECISION - 1;
    
    /** Bit mask for getting a sign. */
    public static final long SIGN_MASK = 0x8000000000000000L;
    
    /** Bit mask for getting an exponent. */
    public static final long EXPONENT_MASK = 0x7ff0000000000000L;
    
    /** Bit mask for getting a fraction. */
    public static final long FRACTION_MASK = 0xfffffffffffffL;
    
    /** Maximum value of fraction. */
    public static final long MAX_FRACTION = FRACTION_MASK;

    /** Bit mask for getting the upper bit of a fraction. */
    public static final long FRACTION_UPPER_BIT_MASK = 1L << (FRACTION_LENGTH - 1);
    
    /** Maximum value of exponent. */
    public static final long MAX_EXPONENT = 0x7ffL;
    
    /** Maximum value of exponent for normalized numbers. */
    public static final long MAX_NORMALIZED_EXPONENT = MAX_EXPONENT - 1;

    /** Minimum value of exponent for normalized numbers. */
    public static final long MIN_NORMALIZED_EXPONENT = 0x1L;
    
    /** Exponent bias. */
    public static final long BIAS = 0x3ffL;
    
    /** Plus zero. */
    public static final double PLUS_ZERO = createDouble(0, 0, 0);
    
    /** Minus zero. */
    public static final double MINUS_ZERO = createDouble(1, 0, 0);

    /** Plus infinity. */
    public static final double PLUS_INFINITY = Double.POSITIVE_INFINITY;
    
    /** Minus infinity. */
    public static final double MINUS_INFINITY = Double.NEGATIVE_INFINITY;
    
    /** Singleton instance of the <code>DoubleType</code> clas. */
    public static final DoubleType TYPE = new DoubleType();

    /** Returns width of the type. */
    public static int getTypeWidth()
    {
        return DoubleWordType.getTypeWidth();
    }
    
    /**
     * Checks if the value is double or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is double; <code>false</code>
     *         otherwise.
     */
    public static boolean isDouble(long value)
    {
        return true; 
    }
    
    /**
     * Checks if the value is normalized.
     * 
     * @param  <code>value</code> the value to be checked.

     * @return <code>true</code> if the value is normalized; <code>false</code>
     *         otherwise.
     */
    public static boolean isNormalized(double value)
    {
        long bits = Double.doubleToRawLongBits(value);
        long exp = (bits >>> FRACTION_LENGTH) & MAX_EXPONENT;
                
        return exp <= MAX_NORMALIZED_EXPONENT & exp >= MIN_NORMALIZED_EXPONENT;
    }
    
    /**
     * Checks if the value is denormalized.
     * 
     * @param  <code>value</code> the value to be checked.

     * @return <code>true</code> if the value is denormalized; <code>false</code>
     *         otherwise.
     */
    public static boolean isDenormalized(double value)
    {
        long bits = Double.doubleToRawLongBits(value);
        long exp = (bits >>> FRACTION_LENGTH) & MAX_EXPONENT;
        long fraction = bits & MAX_FRACTION;
        
        return exp == 0x0 && fraction != 0x0;
    }
    
    /**
     * Checks if the value is plus or minus infinity.
     * 
     * @param  <code>value</code> the value to be checked.

     * @return <code>true</code> if the value is plus or minus infinity;
     *         <code>false</code> otherwise.
     */
    public static boolean isInfinity(double value)
    {
        return Double.isInfinite(value);
    }
    
    /**
     * Checks if the value is plus infinity.
     * 
     * @param  <code>value</code> the value to be checked.

     * @return <code>true</code> if the value is plus infinity;
     *         <code>false</code> otherwise.
     */
    public static boolean isPlusInfinity(double value)
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
    public static boolean isMinusInfinity(double value)
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
    public static boolean isNaN(double value)
    {
        return Double.isNaN(value);
    }
    
    /**
     * Checks if the value is plus or minus zero.
     * 
     * @param  <code>value</code> the value to be checked.

     * @return <code>true</code> if the value is plus or minus zero;
     *         <code>false</code> otherwise.
     */
    public static boolean isZero(double value)
    {
        return value == 0.0;
    }
    
    /**
     * Returns the sign of the value.
     * 
     * @param  <code>value</code> the value.
     * @return the sign of the value.
     */
    public static int getSign(double value)
    {
        long bits = Double.doubleToRawLongBits(value);
        
        return (bits & SIGN_MASK) != 0 ? 1 : 0;
    }
    
    /**
     * Returns the exponent of the value.
     * 
     * @param  <code>value</code> the value.
     * @return the exponent of the value.
     */
    public static long getExponent(double value)
    {
        long bits = Double.doubleToRawLongBits(value);
        
        return (bits >>> FRACTION_LENGTH) & MAX_EXPONENT;
    }
   
    /**
     * Returns the fraction of the value.
     * 
     * @param  <code>value</code> the value.
     * @return the exponent of the value.
     */
    public static long getFraction(double value)
    {
        long bits = Double.doubleToRawLongBits(value);
        
        return bits & MAX_FRACTION;    
    }
    
    /**
     * Returns the position the lower unit in the value's fraction.
     * 
     * @param  <code>value</code> the value.
     * @return the position the lowest unit in the value's fraction if it exist;
     *         <code>-1</code> otherwise.
     */
    public static int getLowerUnitPosition(long value)
    {
        int i = 0;
        
        if(value == 0)
            { return -1; }
        
        for(i = 0; i < FRACTION_LENGTH; i++)
        {
            if(((value >>> i) & 0x1) == 0x1)
                { break; }
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
    public static int getUpperUnitPosition(long value)
    {
        int i = 0;
        
        if(value == 0)
            { return -1; }
        
        for(i = FRACTION_LENGTH - 1; i >= 0; i--)
        {
            if(((value >>> i) & 0x1) == 0x1)
                { break; }
        }
        
        return (FRACTION_LENGTH - 1) - i;
    }

    /**
     * Creates the double-precision floating-point number.
     * 
     * @param  <code>sign</code> the sign.
     * 
     * @param  <code>exp</code> the exponent.
     * 
     * @param  <code>fraction</code> the fraction.
     * 
     * @return the created double-precision floating-point number.
     */
    public static double createDouble(long sign, long exp, long fraction) 
    {
        long bits = (sign << 63) | (exp << 52) | fraction;
        
        try {
            if(sign > 1 || exp > MAX_EXPONENT || fraction > MAX_FRACTION)
                throw new Exception();
        }
        catch(Exception e)
        {            
        }
        
        return Double.longBitsToDouble(bits);
    }
    
    /**
     * Creates the double-precision floating-point number.
     * 
     * @param  <code>bits</code> the bit representation of the number.
     * @return the created double-precision floating-point number.
     */
    public static double createDouble(long bits)
    {
        return Double.longBitsToDouble(bits);
    }
    
    /** Default constructor. */
    private DoubleType()
    {
        super("Double", DoubleWordType.TYPE);        
    }
    
    /**
     * Checks if the value is double or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is double; <code>false</code>
     *         otherwise.
     */
    public boolean checkType(long value)
    {
        return isDouble(value);
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
        return Double.toString(Double.longBitsToDouble(value)) +
            " (0x" + Long.toHexString(value) + ")";
    }

    /** Returns width of the type. */
    public int getWidth()
    {
        return getTypeWidth();
    }
}
