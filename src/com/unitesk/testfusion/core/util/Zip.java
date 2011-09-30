/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Zip.java,v 1.5 2008/09/17 10:33:19 protsenko Exp $
 */

package com.unitesk.testfusion.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZIP archivator.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class Zip
{  
    private static ZipOutputStream zipOutputStream;

    /** Print names of archiving files. */
    public static boolean print = true; 
    
    /**
     * Compresses the directory.
     *  
     * @param <code>directory</code> the directory name.
     * 
     * @param <code>method</code> the compression method.
     */
    public static void compress(String directory, int method)        
    {
        compress(directory, directory + ".zip", method);
    }

    /**
     * Compresses the directory.
     * 
     * @param <code>directory</code> the directory name.
     * 
     * @param <code>archive</code> the archive name.
     * 
     * @param <code>method</code> the compression method.
     */
    public static void compress(String directory, String archive, int method)
    {
        try
        {
            FileOutputStream fileOutputStream;
            
            fileOutputStream = new FileOutputStream(archive);
            
            zipOutputStream = new ZipOutputStream(fileOutputStream);
                
            zipOutputStream.setLevel(method);
            
            compress(directory);
            
            zipOutputStream.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Compresses the directory.
     * 
     * @param <code>directory</code> the directory name.
     */
    public static void compress(String directory)
    {
        try
        {
            File file = new File(directory);
            
            String[] files = file.list();
    
            for(int i = 0; i < files.length; i++)
            {
                File fileForCompress = new File(directory + "//" + files[i]);
                
                if(fileForCompress.isFile())
                {
                    if(print)
                        { System.out.println("archiving file: " + fileForCompress.toString()); } 
                    
                    ZipEntry zipEntry = new ZipEntry(fileForCompress.toString());
    
                    FileInputStream fileInputStream = new FileInputStream(fileForCompress.toString());
                    
                    zipOutputStream.putNextEntry(zipEntry);
                    
                    for(int c = fileInputStream.read(); c != -1; c = fileInputStream.read()) 
                    {
                        zipOutputStream.write(c);
                    }
    
                    fileInputStream.close();
                }
                else
                {
                    compress(fileForCompress.toString());
                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
