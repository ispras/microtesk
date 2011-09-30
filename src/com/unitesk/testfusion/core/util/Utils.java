/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Utils.java,v 1.13 2009/05/21 12:54:45 kamkin Exp $
 */

package com.unitesk.testfusion.core.util;

import java.io.File;
import java.util.Collection;

import com.unitesk.testfusion.gui.GUI;

/**
 * Class <code>Utils</code> implements utility methods.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Utils
{
    /**
     * Checks if the string is <code>null</code> or empty.
     *  
     * @param  <code>string</code> the string to be checked.
     * 
     * @return <code>true</code> if the string is <code>null</code> or empty;
     *         <code>false</code> otherwise.
     */
    public static boolean isNullOrEmpty(String string)
    {
        return string == null || string.isEmpty();
    }

    /**
     * Checks if the collection is <code>null</code> or empty.
     *  
     * @param  <code>collection</code> the collection to be checked.
     * 
     * @return <code>true</code> if the collection is <code>null</code> or empty;
     *         <code>false</code> otherwise.
     */
    public static boolean isNullOrEmpty(Collection collection)
    {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * Returns a hexadecimal string representation of the short value.
     * 
     * @param  <code>value</code> the value.
     * 
     * @return a hexadecimal string representation of the short value.
     */
    public static String toHexString(short value)
    {
        String res = Integer.toHexString(value);
        
        return value >= 0 ? res : res.substring(4);
    }
    
    /**
     * Adds inverted commas to specified string.
     * 
     * @param <code>s</code> the string. 
     * 
     * @return the string <code>s</code> in inverted commas. 
     */
    public static String quote(String s)
    {
        return "\"" + s + "\""; 
    }

    /**
     * Checks if the file path is correct or not.
     * 
     * @param  <code>path</code> the file path.
     * 
     * @return <code>true</code> if the file path is correct; <code>false</code>
     *         otherwise.
     */
    public static boolean isCorrectFilePath(String path)
    {
        File file = new File(path);
        
        try
        {
            file.getCanonicalPath();
            
            return true;
        }
        catch(Exception e)
            { return false;  } 
    }
    
    /**
     * Returns the specified file name without extention.
     * 
     * @param <code>fileName</code> the file name.
     * 
     * @return if specified file name has extension 
     * {@link com.unitesk.testfusion.gui.GUI#EXTENSION} then method returns
     * file name without extension, otherwise returns file name without any
     * changes.
     */
    public static String removeFileExtention(String fileName)
    {
        int size = GUI.EXTENSION.length();
        int length = fileName.length();
        
        return  (length <= size || 
                !fileName.substring(length - size - 1, length).
                    equals("." + GUI.EXTENSION)) ?
                            
            fileName : fileName.substring(0, length - size - 1);
    }
    
    /**
     * Removes blanks from specified string.
     * 
     * @param <code>s</code> the string for removing blanks.
     * 
     * @return the specified string without any blanks.
     */
    public static String removeBlanks(String s)
    {
        return s.replace(" ", "");
    }
    
    
}
