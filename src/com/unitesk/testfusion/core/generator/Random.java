/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Random.java,v 1.9 2009/08/18 14:52:36 vorobyev Exp $
 */

package com.unitesk.testfusion.core.generator;

import com.unitesk.testfusion.core.model.ContentType;
import com.unitesk.testfusion.core.model.IllegalContentTypeException;
import com.unitesk.testfusion.core.type.DoubleType;
import com.unitesk.testfusion.core.type.IntegerType;
import com.unitesk.testfusion.core.type.SingleType;

/**
 * Random generator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Random
{    
    private static java.util.Random random = new java.util.Random();

    //**********************************************************************************************
    // Bit 
    //**********************************************************************************************

    /**
     * Returns random boolean value.
     * 
     * @return random boolean value.
     */
    public static boolean bit()
    {
        return random.nextBoolean();
    }
    
    //**********************************************************************************************
    // Int8 
    //**********************************************************************************************

    /**
     * Returns random 8-bit value.
     * 
     * @return random 8-bit value.
     */
    public static byte int8()
    {
        return (byte)int32();
    }
    
    //**********************************************************************************************
    // Int16
    //**********************************************************************************************

    /**
     * Returns random 16-bit value.
     * 
     * @return random 16-bit value.
     */
    public static short int16()
    {
        return (short)int32();
    }
    
    /**
     * Returns random positive 16-bit value.
     * 
     * @return random positive 16-bit value.
     */
    public static short int16_positive()
    {
        short rnd = int16_non_negative();

        return rnd == 0 ? 1 : rnd;
    }
    
    /**
     * Returns random negative 16-bit value.
     * 
     * @return random negative 16-bit value.
     */
    public static short int16_negative()
    {
        return (short)(int32() | 0xffff8000);
    }

    /**
     * Returns random non-positive 16-bit value.
     * 
     * @return random non-positive 16-bit value.
     */
    public static short int16_non_positive()
    {
        int rnd = int16();
        
        return (short)(rnd > 0 ? -rnd : rnd);
    }
    
    /**
     * Returns random non-negative 16-bit value.
     * 
     * @return random non-negative 16-bit value.
     */
    public static short int16_non_negative()
    {
        return (short)(int32() & 0x7fff);
    }
    
    /**
     * Returns random non-positive 16-bit value that is greater than the given
     * value.
     * 
     * @param  <code>min</code> the lower bound of the generated values.
     * 
     * @return random non-positive 16-bit value that is greater than the given
     *         value.
     */
    public static short int16_non_positive_greater(short min)
    {
        return (short)int32_non_positive_greater(min);
    }
    
    /**
     * Returns random non-positive 16-bit value that is greater than or equal to
     * the given value.
     * 
     * @param  <code>min</code> the lower bound of the generated values.
     * 
     * @return random non-positive 16-bit value that is greater than or equal to
     *         the given value.
     */
    public static short int16_non_positive_greater_or_equal(short min)
    {
        return (short)int32_non_positive_greater_or_equal(min);
    }
    
    /**
     * Returns random non-negative 16-bit value that is less than the given
     * value.
     * 
     * @param  <code>max</code> the upper bound of the generated values.
     * 
     * @return random non-negative 16-bit value that is less than the given
     *         value.
     */
    public static short int16_non_negative_less(short max)
    {
        return (short)int32_non_negative_less(max);
    }

    /**
     * Returns random non-negative 16-bit value that is less than or equal to
     * the given value.
     * 
     * @param  <code>max</code> the upper bound of the generated values.
     * 
     * @return random non-negative 16-bit value that is less than or equal to
     *         the given value.
     */
    public static short int16_non_negative_less_or_equal(short max)
    {
        return (short)int32_non_negative_less_or_equal(max);
    }
    
    /**
     * Returns random 16-bit value from the given range.
     * 
     * @param  <code>min</code> the lower bound of the range.
     * 
     * @param  <code>max</code> the upper bound of the range.
     * 
     * @return random 16-bit value from the given range.
     */
    public static short int16_range(short min, short max)
    {
        return (short)int32_range(min, max); 
    }

    /**
     * Returns random 16-bit value which is not equal to the given one.
     * 
     * @param  <code>value</code> the given value.
     * 
     * @return random 16-bit value which is not equal to the given one.
     */
    public static short int16_non_equal(short value)
    {
        return (short)int32_non_equal(value); 
    }
    
    //**********************************************************************************************
    // Int32
    //**********************************************************************************************

    /**
     * Returns random 32-bit value.
     * 
     * @return random 32-bit value.
     */
    public static int int32()
    {
        return random.nextInt();
    }
    
    /**
     * Returns random positive 32-bit value.
     * 
     * @return random positive 32-bit value.
     */
    public static int int32_positive()
    {
        int rnd = int32_non_negative();

        return rnd == 0 ? 1 : rnd;
    }
    
    /**
     * Returns random negative 32-bit value.
     * 
     * @return random negative 32-bit value.
     */
    public static int int32_negative()
    {
        return int32() | 0x80000000;
    }

    /**
     * Returns random non-positive 32-bit value.
     * 
     * @return random non-positive 32-bit value.
     */
    public static int int32_non_positive()
    {
        int rnd = int32();
        
        return rnd > 0 ? -rnd : rnd;
    }
    
    /**
     * Returns random non-negative 32-bit value.
     * 
     * @return random non-negative 32-bit value.
     */
    public static int int32_non_negative()
    {
        return int32() & 0x7fffffff;
    }
    
    /**
     * Returns random non-positive 32-bit value that is greater than the given
     * value.
     * 
     * @param  <code>min</code> the lower bound of the generated values.
     * 
     * @return random non-positive 32-bit value that is greater than the given
     *         value.
     */
    public static int int32_non_positive_greater(int min)
    {
        int rnd;
        
        if(min == Integer.MIN_VALUE)
        {
            rnd = int32();
            
            return rnd == Integer.MIN_VALUE ? 0 : (rnd > 0 ? -rnd : rnd);
        }
        
        return -int32_non_negative_less(-min);
    }
    
    /**
     * Returns random non-positive 32-bit value that is greater than or equal to
     * the given value.
     * 
     * @param  <code>min</code> the lower bound of the generated values.
     * 
     * @return random non-positive 32-bit value that is greater than or equal to
     *         the given value.
     */
    public static int int32_non_positive_greater_or_equal(int min)
    {
        if(min == Integer.MIN_VALUE)
            { return int32_non_positive(); }
        
        return int32_non_positive_greater(min - 1);
    }
    
    /**
     * Returns random non-negative 32-bit value that is less than the given
     * value.
     * 
     * @param  <code>max</code> the upper bound of the generated values.
     * 
     * @return random non-negative 32-bit value that is less than the given
     *         value.
     */
    public static int int32_non_negative_less(int max)
    {
        return random.nextInt(max);
    }

    /**
     * Returns random non-negative 32-bit value that is less than or equal to
     * the given value.
     * 
     * @param  <code>max</code> the upper bound of the generated values.
     * 
     * @return random non-negative 32-bit value that is less than or equal to
     *         the given value.
     */
    public static int int32_non_negative_less_or_equal(int max)
    {
        if(max == Integer.MAX_VALUE)
            { return int32_non_negative(); }
        
        return int32_non_negative_less(max + 1);
    }
    
    /**
     * Returns random 32-bit value from the given range.
     * 
     * @param  <code>min</code> the lower bound of the range.
     * 
     * @param  <code>max</code> the upper bound of the range.
     * 
     * @return random 32-bit value from the given range.
     */
    public static int int32_range(int min, int max)
    {
        int rnd;
        
        if(min > max)
            { throw new IllegalArgumentException("min is greater than max"); }
        
        if(max >= 0 && min >= max - Integer.MAX_VALUE || max < 0 && max <= Integer.MAX_VALUE + min)
            { return min + int32_non_negative_less_or_equal(max - min); }
    
        rnd = int32();
        
        if(rnd < min)
            { return (rnd - Integer.MIN_VALUE) + min; }
        
        if(rnd > max)
            { return max - (Integer.MAX_VALUE - rnd); }
        
        return rnd; 
    }

    /**
     * Returns random 32-bit value which is not equal to the given one.
     * 
     * @param  <code>value</code> the given value.
     * 
     * @return random 32-bit value which is not equal to the given one.
     */
    public static int int32_non_equal(int value)
    {
        int rnd;
        
        do { rnd = int32(); } while(rnd == value);
        
        return rnd;
    }
    
    //**********************************************************************************************
    // Int64
    //**********************************************************************************************

    /**
     * Returns random 64-bit value.
     * 
     * @return random 64-bit value.
     */
    public static long int64()
    {
        return random.nextLong();
    }
    
    /**
     * Returns random positive 64-bit value.
     * 
     * @return random positive 64-bit value.
     */
    public static long int64_positive()
    {
        long rnd = int64_non_negative();

        return rnd == 0 ? 1 : rnd;
    }
    
    /**
     * Returns random negative 64-bit value.
     * 
     * @return random negative 64-bit value.
     */
    public static long int64_negative()
    {
        return int64() | 0x8000000000000000L;
    }

    /**
     * Returns random non-positive 64-bit value.
     * 
     * @return random non-positive 64-bit value.
     */
    public static long int64_non_positive()
    {
        long rnd = int64();
        
        return rnd > 0 ? -rnd : rnd;
    }
    
    /**
     * Returns random non-negative 64-bit value.
     * 
     * @return random non-negative 64-bit value.
     */
    public static long int64_non_negative()
    {
        return int64() & 0x7fffffffffffffffL;
    }
    
    /**
     * Returns random non-positive 64-bit value that is greater than the given
     * value.
     * 
     * @param  <code>min</code> the lower bound of the generated values.
     * 
     * @return random non-positive 64-bit value that is greater than the given
     *         value.
     */
    public static long int64_non_positive_greater(long min)
    {
        long rnd;
        
        if(min == Long.MIN_VALUE)
        {
            rnd = int64();
            
            return rnd == Long.MIN_VALUE ? 0 : (rnd > 0 ? -rnd : rnd);
        }
        
        return -int64_non_negative_less(-min);
    }
    
    /**
     * Returns random non-positive 64-bit value that is greater than or equal to
     * the given value.
     * 
     * @param  <code>min</code> the lower bound of the generated values.
     * 
     * @return random non-positive 64-bit value that is greater than or equal to
     *         the given value.
     */
    public static long int64_non_positive_greater_or_equal(long min)
    {
        if(min == Long.MIN_VALUE)
            { return int64_non_positive(); }
        
        return int64_non_positive_greater(min - 1);
    }
    
    /**
     * Returns random non-negative 32-bit value that is less than the given
     * value.
     * 
     * @param  <code>max</code> the upper bound of the generated values.
     * 
     * @return random non-negative 32-bit value that is less than the given
     *         value.
     */
    public static long int64_non_negative_less(long max)
    {
        return (int64() & 0x7fffffffffffffffL) % max;
    }

    /**
     * Returns random non-negative 64-bit value that is less than or equal to
     * the given value.
     * 
     * @param  <code>max</code> the upper bound of the generated values.
     * 
     * @return random non-negative 64-bit value that is less than or equal to
     *         the given value.
     */
    public static long int64_non_negative_less_or_equal(long max)
    {
        if(max == Long.MAX_VALUE)
            { return int64_non_negative(); }
        
        return int64_non_negative_less(max + 1);
    }
    
    /**
     * Returns random 64-bit value from the given range.
     * 
     * @param  <code>min</code> the lower bound of the range.
     * 
     * @param  <code>max</code> the upper bound of the range.
     * 
     * @return random 64-bit value from the given range.
     */
    public static long int64_range(long min, long max)
    {
        long rnd;
        
        if(min > max)
            { throw new IllegalArgumentException("min is greater than max"); }
        
        if(max >= 0 && min >= max - Long.MAX_VALUE || max < 0 && max <= Long.MAX_VALUE + min)
            { return min + int64_non_negative_less_or_equal(max - min); }
    
        rnd = int64();
        
        if(rnd < min)
            { return (rnd - Long.MIN_VALUE) + min; }     
        
        if(rnd > max)
            { return max - (Long.MAX_VALUE - rnd); }
        
        return rnd; 
    }
    
    /**
     * Returns random 64-bit value which is not equal to the given one.
     * 
     * @param  <code>value</code> the given value.
     * 
     * @return random 64-bit value which is not equal to the given one.
     */
    public static long int64_non_equal(int value)
    {
        long rnd;
        
        do { rnd = int64(); } while(rnd == value);
        
        return rnd;
    }
    
    //**********************************************************************************************
    // Float32
    //**********************************************************************************************

    /**
     * Returns random single-precision floating-point value.
     * 
     * @return random single-precision floating-point value.
     */
    public static float float32()
    {
        return Float.intBitsToFloat(int32());
    }
    
    /**
     * Returns random single-precision from [0, 1].
     * 
     * @return random single-precision from [0, 1].
     */
    public static float float32_normed()
    {
        return random.nextFloat();
    }

    /**
     * Returns random normalized single-precision.
     * 
     * @return random normalezed single-precision.
     */
    public static float float32_normalized()
    {
        int sign     = int32_non_negative_less(2);
        int exponent = int32_non_negative_less(SingleType.MAX_NORMALIZED_EXPONENT) + 1;
        int fraction = int32_non_negative_less(SingleType.MAX_FRACTION + 1);
        
        return SingleType.createSingle(sign, exponent, fraction);
    }
    
    /**
     * Returns random denormalized single-precision.
     * 
     * @return random denormalezed single-precision.
     */
    public static float float32_denormalized()
    {
        int sign     = int32_non_negative_less(2);
        int exponent = 0x0;
        int fraction = int32_non_negative_less(SingleType.MAX_FRACTION) + 1;
        
        return SingleType.createSingle(sign, exponent, fraction);
    }
   
    /**
     * Returns random single-precision zero (minus zero or plus zero).
     * 
     * @return random single-precision zero (minus zero or plus zero).
     */
    public static float float32_zero()
    {
        return SingleType.createSingle(int32_non_negative_less(2), 0, 0);   
    }

    /**
     * Returns random single-precision plus zero.
     * 
     * @return random single-precision plus zero.
     */
    public static float float32_plus_zero()
    {
        return SingleType.createSingle(0, 0, 0);   
    }

    /**
     * Returns random single-precision minus zero.
     * 
     * @return random single-precision minus zero.
     */
    public static float float32_minus_zero()
    {
        return SingleType.createSingle(1, 0, 0);   
    }
    
    /**
     * Returns random single-precision infinity (minus infinity or plus
     * infinity).
     * 
     * @return random single-precision infinity (minus infinity or plus
     *         infinity).
     */
    public static float float32_infinity()
    {
        int sign     = int32_non_negative_less(2);
        int exponent = SingleType.MAX_EXPONENT;
        int fraction = 0x0;
        
        return SingleType.createSingle(sign, exponent, fraction);
    }
    
    /**
     * Returns random single-precision plus infinity.
     * 
     * @return random single-precision plus infinity.
     */
    public static float float32_plus_infinity()
    {
        int exponent = SingleType.MAX_EXPONENT;
        int fraction = 0x0;
        
        return SingleType.createSingle(0, exponent, fraction);
    }

    /**
     * Returns random single-precision minus infinity.
     * 
     * @return random single-precision minus infinity.
     */
    public static float float32_minus_infinity()
    {
        int exponent = SingleType.MAX_EXPONENT;
        int fraction = 0x0;
        
        return SingleType.createSingle(1, exponent, fraction);
    }
        
    /**
     * Returns random single-precision NaN (Not-a-Number).
     * 
     * @return random single-precision NaN (Not-a-Number).
     */
    public static float float32_nan()
    {
        int sign     = int32_non_negative_less(2);
        int exponent = SingleType.MAX_EXPONENT;
        int fraction = int32_non_negative_less(SingleType.MAX_FRACTION) + 1;
        
        return SingleType.createSingle(sign, exponent, fraction);
    }
    
    /**
     * Returns random single-precision signaling NaN (Not-a-Number).
     * 
     * @return random single-precision signaling NaN (Not-a-Number).
     */
    public static float float32_signal_nan()
    {
        int sign     = int32_non_negative_less(2);
        int exponent = SingleType.MAX_EXPONENT;
        int fraction = Random.int32_range(1, SingleType.FRACTION_UPPER_BIT_MASK - 1);
        
        return SingleType.createSingle(sign, exponent, fraction);
    }
    
    /**
     * Returns random single-precision quite NaN (Not-a-Number).
     * 
     * @return random single-precision quite NaN (Not-a-Number).
     */
    public static float float32_quite_nan()
    {
        int sign     = int32_non_negative_less(2);
        int exponent = SingleType.MAX_EXPONENT;
        int fraction = (Random.int32() & SingleType.FRACTION_MASK) | SingleType.FRACTION_UPPER_BIT_MASK;
        
        return SingleType.createSingle(sign, exponent, fraction);
    }
    
    //**********************************************************************************************
    // Float64
    //**********************************************************************************************

    /**
     * Returns random double-precision floating-point value.
     * 
     * @return random double-precision floating-point value.
     */
    public static double float64()
    {
        return random.nextDouble();
    }
    
    /**
     * Returns random double-precision from [0, 1].
     * 
     * @return random double-precision from [0, 1].
     */
    public static double float64_normed()
    {
        return random.nextDouble();
    }
    
    /**
     * Returns random normalized double-precision.
     * 
     * @return random normalezed double-precision.
     */
    public static double float64_normalized()
    {
        long sign     = int64_non_negative_less(2);
        long exponent = int64_non_negative_less(DoubleType.MAX_NORMALIZED_EXPONENT) + 1;
        long fraction = int64_non_negative_less(DoubleType.MAX_FRACTION + 1);
        
        return DoubleType.createDouble(sign, exponent, fraction);
    }

    /**
     * Returns random denormalized double-precision.
     * 
     * @return random denormalezed double-precision.
     */
    public static double float64_denormalized()
    {
        long sign     = int64_non_negative_less(2);
        long exponent = 0x0;
        long fraction = int64_non_negative_less(DoubleType.MAX_FRACTION) + 1;
        
        return DoubleType.createDouble(sign, exponent, fraction);
    }
    
    /**
     * Returns random double-precision zero (minus zero or plus zero).
     * 
     * @return random double-precision zero (minus zero or plus zero).
     */
    public static double float64_zero()
    {
        return DoubleType.createDouble(int64_non_negative_less(2), 0, 0);   
    }

    /**
     * Returns random double-precision plus zero.
     * 
     * @return random double-precision plus zero.
     */
    public static double float64_plus_zero()
    {
        return DoubleType.createDouble(0, 0, 0);   
    }

    /**
     * Returns random double-precision minus zero.
     * 
     * @return random double-precision minus zero.
     */
    public static double float64_minus_zero()
    {
        return DoubleType.createDouble(1, 0, 0);   
    }
    
    /**
     * Returns random double-precision infinity (minus infinity or plus
     * infinity).
     * 
     * @return random double-precision infinity (minus infinity or plus
     *         infinity).
     */
    public static double float64_infinity()
    {
        long sign     = int64_non_negative_less(2);
        long exponent = DoubleType.MAX_EXPONENT;
        long fraction = 0x0;
        
        return DoubleType.createDouble(sign, exponent, fraction);
    }

    /**
     * Returns random double-precision plus infinity.
     * 
     * @return random double-precision plus infinity.
     */
    public static double float64_plus_infinity()
    {
        long exponent = DoubleType.MAX_EXPONENT;
        long fraction = 0x0;
        
        return DoubleType.createDouble(0, exponent, fraction);
    }

    /**
     * Returns random double-precision minus infinity.
     * 
     * @return random double-precision minus infinity.
     */
    public static double float64_minus_infinity()
    {
        long exponent = DoubleType.MAX_EXPONENT;
        long fraction = 0x0;
        
        return DoubleType.createDouble(1, exponent, fraction);
    }
        
    /**
     * Returns random double-precision NaN (Not-a-Number).
     * 
     * @return random double-precision NaN (Not-a-Number).
     */
    public static double float64_nan()
    {
        long sign     = int64_non_negative_less(2);
        long exponent = DoubleType.MAX_EXPONENT;
        long fraction = int64_non_negative_less(DoubleType.MAX_FRACTION) + 1;
        
        return DoubleType.createDouble(sign, exponent, fraction);
    }

    /**
     * Returns random double-precision signaling NaN (Not-a-Number).
     * 
     * @return random double-precision signaling NaN (Not-a-Number).
     */
    public static double float64_signal_nan()
    {
        long sign     = int64_non_negative_less(2);
        long exponent = DoubleType.MAX_EXPONENT;
        long fraction = Random.int64_range(1, DoubleType.FRACTION_UPPER_BIT_MASK - 1);
        
        return DoubleType.createDouble(sign, exponent, fraction);
    }
    
    /**
     * Returns random double-precision quite NaN (Not-a-Number).
     * 
     * @return random double-precision quite NaN (Not-a-Number).
     */
    public static double float64_quite_nan()
    {
        long sign     = int64_non_negative_less(2);
        long exponent = DoubleType.MAX_EXPONENT;
        long fraction = (Random.int64() & DoubleType.FRACTION_MASK) | DoubleType.FRACTION_UPPER_BIT_MASK;
        
        return DoubleType.createDouble(sign, exponent, fraction);
    }
    
    //**********************************************************************************************
    // General
    //**********************************************************************************************

    /**
     * Returns random value of the given content type.
     * 
     * @param  <code>type</code> the content type.
     * 
     * @return random value of the given content type.
     */
    public static long value(ContentType type)
    {
        if(type.equals(ContentType.BOOLEAN))
            { return bit() ? 1L : 0L; }
        if(type.equals(ContentType.BYTE))
            { return int8(); }
        if(type.equals(ContentType.HALF_WORD))
            { return int16(); }
        if(type.equals(ContentType.WORD))
            { return int32(); }
        if(type.equals(ContentType.PAIR_WORD))
            { return int64(); }
        if(type.equals(ContentType.DOUBLE_WORD))
            { return int64(); }
        if(type.equals(ContentType.SINGLE))
            { return int32(); }
        if(type.equals(ContentType.PAIR_SINGLE))
            { return int64(); }
        if(type.equals(ContentType.DOUBLE))
            { return int64(); }
        if(type.equals(ContentType.OFFSET))
            { return int16(); }
        if(type.equals(ContentType.DATA_ADDRESS))
            { return int64(); }
        if(type.equals(ContentType.INSTRUCTION_ADDRESS))
            { return int64(); }
        if(type.equals(ContentType.PHYSICAL_ADDRESS))
            { return int64(); }
        
        if(type instanceof IntegerType)
        {
            IntegerType integerType = (IntegerType)type;
            
            return int32() & ((1 << integerType.getWidth()) - 1);
        }
        
        throw new IllegalContentTypeException(type);
    }
}
