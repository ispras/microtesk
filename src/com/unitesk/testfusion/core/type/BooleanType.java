/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BooleanType.java,v 1.5 2009/11/09 14:53:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.type;

import com.unitesk.testfusion.core.model.ContentType;

/**
 * Class <code>BooleanType</code> represents 1-bit type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BooleanType extends ContentType
{
    /** Minimum value of the type. */
    public static final int MIN_VALUE = 0;
    
    /** Maximum value of the type. */
    public static final int MAX_VALUE = 1;
    
    /** Singleton instance of the <code>BooleanType</code> class. */
    public static final BooleanType TYPE = new BooleanType();

    /** Returns width of the type. */
    public static int getTypeWidth()
    {
        return 1;
    }
    
    /**
     * Checks if the value is boolean or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is boolean; <code>false</code>
     *         otherwise.
     */
    public static boolean isBoolean(long value)
    {
        return value == 0 || value == 1;
    }
    
    /** Default constructor. */
    private BooleanType()
    {
        // Boolean type is the alias of the 1-bit integer type.
        super("Boolean", IntegerType.TYPE(1), true);
    }
    
    /**
     * Checks if the value is boolean or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is boolean; <code>false</code>
     *         otherwise.
     */
    public boolean checkType(long value)
    {
        return isBoolean(value);
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
        return Long.toString(value);
    }
    
    /** Returns width of the type. */
    public int getWidth()
    {
        return getTypeWidth();
    }
}
