/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: DataAddressType.java,v 1.5 2009/11/09 14:53:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.type;

import com.unitesk.testfusion.core.model.ContentType;

/**
 * Class <code>DataAddressType</code> represents data address type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class DataAddressType extends ContentType
{
    /** Singleton instance of the <code>DataAddressType</code> class. */
    public static final DataAddressType TYPE = new DataAddressType();

    /** Returns width of the type. */
    public static int getTypeWidth()
    {
        return DoubleWordType.getTypeWidth();
    }
    
    /**
     * Checks if the value is a correct data address or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is a correct data address;
     *         <code>false</code> otherwise.
     */
    public static boolean isVirtualAddress(long value)
    {
        return true; 
    }
    
    /** Default constructor. */
    private DataAddressType()
    {
        super("Data Address", DoubleWordType.TYPE);        
    }
    
    /**
     * Checks if the value is a correct data address or not.
     * 
     * @param  <code>value</code> the value to be checked.
     * 
     * @return <code>true</code> if the value is a correct data address;
     *         <code>false</code> otherwise.
     */
    public boolean checkType(long value)
    {
        return isVirtualAddress(value);
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
