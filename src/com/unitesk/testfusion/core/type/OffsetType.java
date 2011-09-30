/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: OffsetType.java,v 1.3 2009/11/09 14:53:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.type;

import com.unitesk.testfusion.core.model.ContentType;

/**
 * Class <code>OffsetType</code> represents address offset type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class OffsetType extends ContentType
{
    /** Singleton instance of the <code>OffsetType</code> class. */
    public static final OffsetType TYPE = new OffsetType();

    /** Returns width of the type. */
    public static int getTypeWidth()
    {
        return HalfWordType.getTypeWidth();
    }
    
    /**
     * Checks if the value is a correct offset.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is a correct offset;
     *         <code>false</code> otherwise.
     */
    public static boolean isOffset(long value)
    {
        return HalfWordType.isHalfWord(value);
    }
    
    /** Default constructor. */
    private OffsetType()
    {
        super("Offset", HalfWordType.TYPE);
    }
    
    /**
     * Checks if the value is a correct offset.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is a correct offset;
     *         <code>false</code> otherwise.
     */
    public boolean checkType(long value)
    {
        return isOffset(value);
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
