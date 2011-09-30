/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: TestProgramArchivator.java,v 1.1 2008/09/10 15:19:57 kamkin Exp $
 */

package com.unitesk.testfusion.core.engine;

import java.util.zip.Deflater;

import com.unitesk.testfusion.core.util.Zip;

/**
 * Test program archivator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class TestProgramArchivator
{
    /** Archivation is disabled. */
    public static final int NONE = 0;
    
    /** ZIP archivator. */
    public static final int ZIP = 1;
    
    /**
     * Type of archivator. Possible values are:
     * 
     * <code>NONE</code> - archivation is disabled.
     * 
     * <code>ZIP</code> - ZIP archivator.
     */
    protected int archivator = NONE;
    
    /**
     * Compression method. Possible values are:
     * 
     * <code>Deflater.BEST_SPEED</code> - fast compression;
     * 
     * <code>Deflater.DEFAULT_COMPRESSION</code> - normal compression;
     *
     * <code>Deflater.BEST_COMPRESSION</code> - maximum compression.
     *   
     * @see <code>java.util.zip.Deflater</code>.
     */
    protected int method = Deflater.DEFAULT_COMPRESSION;
    

    /**
     * Returns the type of archivator.
     * 
     * @return the type of archivator.
     */
    public int getArchivator()
    {
        return archivator;
    }
    
    /**
     * Sets the type of archivator.
     * 
     * @param <code>archivator</code> the type of archivator.
     */
    public void setArchivator(int archivator)
    {
        this.archivator = archivator;
    }
    
    /**
     * Returns the method of compression.
     * 
     * @return the method of compression.
     */
    public int getCompressionMethod()
    {
        return method;
    }
    
    /**
     * Sets the method of compression.
     * 
     * @param <code>method</code>
     */
    public void setCompressionMethod(int method)
    {
        this.method = method;
    }
    
    /**
     * Compresses the output directory.
     * 
     */
    public void compress(String outputDirectory)
    {
        if(archivator == ZIP)
            { Zip.compress(outputDirectory, method); }
    }
    
    /**
     * Returns a string representation of the archivator.
     * 
     * @return a string representation of the archivator.
     */
    public String toString()
    {
        return archivator == ZIP ? "ZIP" : "NONE";
    }
}
