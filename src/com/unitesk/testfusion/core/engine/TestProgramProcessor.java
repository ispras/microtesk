/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: TestProgramProcessor.java,v 1.8 2010/01/14 16:54:39 vorobyev Exp $
 */

package com.unitesk.testfusion.core.engine;

import java.io.File;

import com.unitesk.testfusion.core.util.Utils;

/**
 * Class <code>TestProgramProcessor</code> implements actions for processing
 * generated test programs.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class TestProgramProcessor
{
    /**
     * Returns the full path to the test package (folder that contains test
     * program, makefile, etc.).
     * 
     * @param  <code>packageName</code> the package name.
     * 
     * @param  <code>outputDirectory</code> the output directory.
     * 
     * @return the full path to the test package.
     */
    protected static String getFullPackageName(String packageName, String outputDirectory)
    {
        return Utils.isNullOrEmpty(outputDirectory) ? packageName : outputDirectory + "/" + packageName;
    }
    
    /**
     * Moves file into the test package.
     * 
     * @param <code>fileName</code> the file name.
     * 
     * @param <code>packageName</code> the package name.
     * 
     * @param <code>outputDirectory</code> the output directory.
     */
    public static void renameFile(String fileName, String packageName, String outputDirectory)
    {
        String fullPackageName = getFullPackageName(packageName, outputDirectory);
        
        File directoryFrom = new File(outputDirectory);
        File directoryTo   = new File(fullPackageName);
        
        File fileFrom = new File(directoryFrom, fileName);
        File fileTo   = new File(directoryTo,   fileName);
        
        fileTo.delete();
        
        fileFrom.renameTo(fileTo);
    }
    
    /**
     * Processes the generated test programs.
     * 
     * @param <code>controlFileName</code> the test program for the control
     *        processor (main processor if coprorocessor is being tested).
     * 
     * @param <code>targetFileName</code> the test program for the target
     *        processor (processor under test).
     * 
     * @param <code>dataFileName</code> the test data program.
     * 
     * @param <code>packageName</code> the package name.
     */
    public void process(String controlFileName, String targetFileName, String dataFileName, String packageName)
    {
        process(controlFileName, targetFileName, dataFileName, packageName, "");
    }
    
    /**
     * Processes the generated test programs.
     * 
     * @param <code>controlFileName<code> the test program for the control
     *        processor (main processor if coprorocessor is being tested).
     * 
     * @param <code>targetFileName<code> the test program for the target
     *        processor (processor under test).
     * 
     * @param <code>dataFileName</code> the test data program.
     * 
     * @param <code>packageName<code> the package name.
     * 
     * @param <code>outputDirectory</code> the output directory.
     */
    public void process(String controlFileName, String targetFileName, String dataFileName, String packageName, String outputDirectory)
    {
        System.out.println("Creating package: " + packageName);
        
        File directory = new File(getFullPackageName(packageName, outputDirectory));

        directory.mkdir();
        
        String fullPackageName = getFullPackageName(packageName, outputDirectory);
        
        if(!Utils.isNullOrEmpty(controlFileName))
        {
            System.out.println("Moving file: " + controlFileName + " to " + fullPackageName);
            renameFile(controlFileName, packageName, outputDirectory);
        }
        
        if(!Utils.isNullOrEmpty(targetFileName))
        {
            System.out.println("Moving file: " + targetFileName + " to " + fullPackageName);
            renameFile(targetFileName, packageName, outputDirectory);
        }
        
        if(!Utils.isNullOrEmpty(dataFileName))
        {
            System.out.println("Moving file: " + dataFileName + " to " + fullPackageName);

            renameFile(dataFileName, packageName, outputDirectory);
        }
    }
}
