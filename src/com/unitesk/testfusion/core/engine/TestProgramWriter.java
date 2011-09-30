/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: TestProgramWriter.java,v 1.7 2008/09/10 15:19:57 kamkin Exp $
 */

package com.unitesk.testfusion.core.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.core.util.Utils;

/**
 * Test program writer.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class TestProgramWriter
{
    /** File writer. */
    protected FileWriter writer;

    /** Size of block to be written. */
    protected int blockSize;
    
    /**
     * Constructor.
     * 
     * @param <code>fileName</code> the file name.
     * 
     * @param <code>outputDirectory</code> the output directory.
     * 
     * @param <code>blockSize</code> the block size.
     */
    public TestProgramWriter(String fileName, String outputDirectory, int blockSize)
    {
        this.blockSize = blockSize;
        
        try
        {
            if(!Utils.isNullOrEmpty(outputDirectory))
            {
                File directory = new File(outputDirectory);
                
                if(!directory.exists())
                    { directory.mkdirs(); }
            }
            
            writer = new FileWriter(Utils.isNullOrEmpty(outputDirectory) ? fileName : outputDirectory + "/" + fileName);
        }
        catch(IOException e)
            { e.printStackTrace(); }
    }
    
    /**
     * Constructor.
     * 
     * @param <code>fileName</code> the file name.
     * 
     * @param <code>outputDirectory</code> the output directory.
     */
    public TestProgramWriter(String fileName, String outputDirectory)
    {
        this(fileName, outputDirectory, Integer.MAX_VALUE);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>fileName</code> the file name.
     * 
     * @param <code>blockSize</code> the block size.
     */
    public TestProgramWriter(String fileName, int blockSize)
    {
        this(fileName, "", blockSize);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>fileName</code> the file name.
     */
    public TestProgramWriter(String fileName)
    {
        this(fileName, "");
    }
    
    /**
     * Returns the block size.
     * 
     * @return the block size.
     */
    public int getBlockSize()
    {
        return blockSize;
    }
    
    /**
     * Sets the block size.
     * 
     * @param <code>blockSize</code> the block size.
     */
    public void setBlockSize(int blockSize)
    {
        this.blockSize = blockSize;
    }
    
    /**
     * Writes the string into the file.
     * 
     * @param <code>string</code> the string to be written.
     */
    public void write(String string)
    {
        try
        {
            writer.write(string);
            writer.flush();
        }
        catch(IOException e)
            { e.printStackTrace(); }
    }
    
    /**
     * Writes the program's prefix into the file.
     *
     * @param <code>template</code> the test program template.
     * 
     * @param <code>program</code> the program to be written.
     * 
     * @param <code>size</code> the size of the prefix.
     */
    public void write(TestProgramTemplate template, Program program, int size)
    {
        if(program.countInstruction() < size)
            { size = program.countInstruction(); }
        
        try
        {
            for(int i = 0; i < size; i++)
            {
                Instruction instruction = program.getInstruction(i);
                writer.write("\t" + instruction.text(template) + "\n");
            }
            
            writer.flush();
        }
        catch(IOException e)
            { e.printStackTrace(); }
    }

    /**
     * Writes the program into the file.
     *
     * @param <code>template</code> the test program template.
     * 
     * @param <code>program</code> the program to be written.
     */
    public void write(TestProgramTemplate template, Program program)
    {
        write(template, program, program.countInstruction());
    }
    
    /**
     * Writes the program's prefix into the file. <code>blockSize</code>
     * specifies the size of the prefix.
     *
     * @param <code>template</code> the test program template.
     * 
     * @param <code>program</code> the program to be written.
     */
    public void writeBlock(TestProgramTemplate template, Program program)
    {
        if(program.countInstruction() < blockSize)
            { return; }
        
        write(template, program, blockSize);
        
        program.removePrefix(blockSize);
    }

    /** Closes the test program writer. */
    public void close()
    {
        try
        {
            writer.close();
        }
        catch(IOException e)
            { e.printStackTrace(); }
    }
}
