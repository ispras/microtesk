/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: DoubleWordType.java,v 1.6 2009/11/09 14:53:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.type;

import com.unitesk.testfusion.core.model.ContentType;

/**
 * Class <code>DoubleWordType</code> represents double word type, which consists
 * of 64-bit integer values.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class DoubleWordType extends ContentType
{
    /** Minimum value of the type. */
    public static final long MIN_VALUE = Long.MIN_VALUE;

    /** Maximum value of the type. */
    public static final long MAX_VALUE = Long.MAX_VALUE;
    
    /** Singleton instance of the <code>DoubleWordType</code> class. */
    public static final DoubleWordType TYPE = new DoubleWordType();
    
    /** Returns width of the type. */
    public static int getTypeWidth()
    {
        return 64;
    }

    /**
     * Always returns </code>true</code>.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code>.
     */
    public static final boolean isDoubleWord(long value)
    {
        return true;
    }
    
    /** Default constructor. */
    private DoubleWordType()
    {
        super("Double Word");
    }
    
    /**
     * Always returns </code>true</code>.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code>.
     */
    public boolean checkType(long value)
    {
        return isDoubleWord(value);
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
        return "0x" + Long.toHexString(value);
    }

    /** Returns width of the type. */
    public int getWidth()
    {
        return getTypeWidth();
    }
}
