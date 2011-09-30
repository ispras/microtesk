/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: IntegerType.java,v 1.5 2008/08/13 16:11:56 kamkin Exp $
 */

package com.unitesk.testfusion.core.type;

import com.unitesk.testfusion.core.model.ContentType;

/**
 * Class <code>IntegerType</code> represents a family of integer types with width
 * from 1 to 32.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class IntegerType extends ContentType
{
    /**
     * Returns the minimum value of the integer type for the given width.
     * 
     * @param <code>width</code> the width (number of bits).
     * 
     * @return the minimum value of the integer type for the given width. 
     */
    public static int MIN_VALUE(int width)
    {
        return -MAX_VALUE(width) - 1;
    }
    
    /**
     * Returns the maximum value of the integer type for the given width.
     * 
     * @param <code>width</code> the width (number of bits).
     * 
     * @return the maximum value of the integer type for the given width. 
     */
    public static int MAX_VALUE(int width)
    {
        return 0xffffffff >>> (32 - width + 1);
    }

    /** Array of instances of the <code>IntegerType</code> class. */
    protected static IntegerType[] TYPE = new IntegerType[33];
    
    static
    {
        TYPE[1]  = new IntegerType(1);
        TYPE[2]  = new IntegerType(2);
        TYPE[3]  = new IntegerType(3);
        TYPE[4]  = new IntegerType(4);
        TYPE[5]  = new IntegerType(5);
        TYPE[6]  = new IntegerType(6);
        TYPE[7]  = new IntegerType(7);
        TYPE[8]  = new IntegerType(8);
        TYPE[9]  = new IntegerType(9);
        TYPE[10] = new IntegerType(10);
        TYPE[11] = new IntegerType(11);
        TYPE[12] = new IntegerType(12);
        TYPE[13] = new IntegerType(13);
        TYPE[14] = new IntegerType(14);
        TYPE[15] = new IntegerType(15);
        TYPE[16] = new IntegerType(16);
        TYPE[17] = new IntegerType(17);
        TYPE[18] = new IntegerType(18);
        TYPE[19] = new IntegerType(19);
        TYPE[20] = new IntegerType(20);
        TYPE[21] = new IntegerType(21);
        TYPE[22] = new IntegerType(22);
        TYPE[23] = new IntegerType(23);
        TYPE[24] = new IntegerType(24);
        TYPE[25] = new IntegerType(25);
        TYPE[26] = new IntegerType(26);
        TYPE[27] = new IntegerType(27);
        TYPE[28] = new IntegerType(28);
        TYPE[29] = new IntegerType(29);
        TYPE[30] = new IntegerType(30);
        TYPE[31] = new IntegerType(31);
        TYPE[32] = new IntegerType(32);
    };

    /**
     * Returns the instance of the integer type for the given width.
     * 
     * @param  <code>width</code> the width.
     * @return the instance of the integer type for the given width.
     */
    public static IntegerType TYPE(int width)
    {
        return TYPE[width];
    }
    
    /**
     * Checks if the value is <code>width</code>-bit integer or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is <code>width</code>-bit integer;
     *         <code>false</code> otherwise.
     */
    public static boolean isIntegerValue(long value, int width)
    {
        return value >= MIN_VALUE(width) && value <= MAX_VALUE(width);
    }    
    
    /**
     * Returns the base type for for the given width.
     * 
     * @param  <code>width</code> the width (number of width).
     * @return the base type for for the given width.
     */
    protected static ContentType base(int width)
    {
        if(width == 32)
            { return DoubleWordType.TYPE; }
        
        return TYPE(width + 1);
    }
    
    /** Width (number of bits). */
    protected int width;
    
    /**
     * Constructor.
     * 
     * @param <code>width</code> the width (number of bits).
     */
    public IntegerType(int width)
    {
        super(Integer.toString(width) + "-bit Integer Value", base(width));
        
        this.width = width;
        
        if(0 >= width || width >= 33)
            { throw new IllegalArgumentException("Incorrect width"); }
    }
    
    /**
     * Returns the width of the integer type.
     * 
     * @return the width of the integer type.
     */
    public int getWidth()
    {
        return width;
    }
    
    /**
     * Checks if the value is correct integer or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is correct integer;
     *         <code>false</code> otherwise.
     */
    public boolean checkType(long value)
    {
        return isIntegerValue(value, width);
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
}
