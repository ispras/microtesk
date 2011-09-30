/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SettingsConfig.java,v 1.18 2008/09/17 12:07:47 kozlov Exp $
 */

package com.unitesk.testfusion.core.config;

import java.util.zip.Deflater;

/**
 * Settings, represented by this class, specify test name or test name
 * generation strategy and output directory to save generated test programs.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SettingsConfig extends Config
{
    /** Default test program prefix. */
	public static final String DEFAULT_TEST_NAME = "test";
    
    /** Default output directory. */
	public static final String DEFAULT_OUTPUT_DIRECTORY = "tests";
	
    /** Use specified name as test program prefix. */
    public static final int SPECIFIED_NAME = 0;
    
    /** Use concatenation of instruction name and situation name as test program prefix. */
    public static final int SITUATION_NAME = 1;
    
    /** Use instruction name as test program prefix. */
    public static final int INSTRUCTION_NAME = 2;
    
    /** Use group name as test program prefix. */
    public static final int GROUP_NAME = 3;
    
    /** Use processor name as test program prefix. */
    public static final int PROCESSOR_NAME = 4;

    /**
     * Checks if the test program prefix is correct, i.e. it consists of
     * characters 'a'-'z', 'A'-'Z', '0'-'9', '-', or '_'.
     */
    public static boolean isCorrectTestName(String name)
    {
        return name.matches("([a-zA-Z]|-|_|[0-9])++");
    }
    
    /** Specified test program prefix. */
    protected String testName = DEFAULT_TEST_NAME;
    
    /** Test name generation strategy. */
    protected int testNameStrategy = SPECIFIED_NAME;

    /** Use fully qualified name. */
    protected boolean fullName = false;

    /** Output directory. */
    protected String outputDirectory = DEFAULT_OUTPUT_DIRECTORY;
    
    /** Flag that indicates if output directory should be compressed. */
    protected boolean compress = false;
    
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
    
    /** Default constructor. */
    public SettingsConfig()
    {
        super();
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to settings object.
     */
    protected SettingsConfig(SettingsConfig r)
    {
        super(r);
        
        testName         = r.testName;
        testNameStrategy = r.testNameStrategy;
        fullName         = r.fullName;
        outputDirectory  = r.outputDirectory;
        compress         = r.compress;
        method           = r.method;
    }
    
    /**
     * Returns the output directory.
     * 
     * @return the output directory.
     */
    public String getOutputDirectory()
    {
        return outputDirectory;
    }
    
    /**
     * Sets the output directory.
     * 
     * @param <code>outputDirectory</code> the output directory.
     */
    public void setOutputDirectory(String outputDirectory)
    {
        this.outputDirectory = outputDirectory;
    }
    
    /**
     * Returns the test program prefix.
     * 
     * @return the test program prefix.
     */
    public String getTestName()
    {
        return testName;
    }
    
    /**
     * Sets the output directory.
     * 
     * @param <code>testName</code> the output directory.
     */
    public void setTestName(String testName)
    {
        this.testName = testName;
    }
    
    /**
     * Returns the test program prefix generation strategy.
     * 
     * @return the test program prefix generation strategy.
     */
    public int getTestNameStrategy()
    {
        return testNameStrategy;
    }
    
    /**
     * Sets the test program prefix generation strategy.
     * 
     * @param <code>testNameStrategy</code> the test program prefix generation
     *         strategy. It can possess one of the following values:
     *        
     *        <code>SPECIFIED_NAME</code> -
     *        use specified name as test program prefix;
     *
     *        <code>SITUATION_NAME</code> -
     *        use concatenation of instruction name and situation name as test
     *        name;
     * 
     *        <code>INSTRUCTION_NAME</code> -
     *        use instruction name as test program prefix;
     *
     *        <code>GROUP_NAME</code> -
     *        use group name as test program prefix;
     * 
     *        <code>PROCESSOR_NAME</code> -
     *        use processor name as test program prefix.
     */
    public void setTestNameStrategy(int testNameStrategy)
    {
        this.testNameStrategy = testNameStrategy;
    }

    /**
     * Checks if output directory should be compressed or not.
     * 
     * @return <code>true</code> if output directory should be compressed;
     *         <code>false</code> otherwise.
     */
    public boolean isCompress()
    {
        return compress;
    }
    
    /**
     * Sets compression flag.
     * 
     * @param <code>compress</code> flag that indicates if output directory
     *        should be compressed or not;
     */
    public void setCompress(boolean compress)
    {
        this.compress = compress;
    }
    
    /**
     * Returns the compression method.
     * 
     * @return the compression method.
     */
    public int getCompressionMethod()
    {
        return method;
    }
    
    /**
     * Sets the compression method.
     * 
     * @param <code>method</code> the compression method.
     */
    public void setCompressionMethod(int method)
    {
        this.method = method;
    }
    
    /**
     * Checks if fully qualified naming is used. 
     * 
     * @return <code>true</code> if fully qualified name is used;
     *         <code>false</code> otherwise.
     */
    public boolean isFullName()
    {
        return fullName;
    }
    
    /**
     * Enables/disables using of fully qualified naming. 
     * 
     * @param <code>fullName</code> the enabling status for using of fully
     *        qualified naming.
     */
    public void setFullName(boolean fullName)
    {
        this.fullName = fullName;
    }
    
    /**
     * Returns name of the specified test program prefix generation strategy.
     * 
     * @param <code>testNameStrategy</code> the test program prefix generation
     *         strategy. It can possess one of the following values:
     *        
     *        <code>SPECIFIED_NAME</code> -
     *        use specified name as test program prefix;
     *
     *        <code>SITUATION_NAME</code> -
     *        use concatenation of instruction name and situation name as test
     *        name;
     * 
     *        <code>INSTRUCTION_NAME</code> -
     *        use instruction name as test program prefix;
     *
     *        <code>GROUP_NAME</code> -
     *        use group name as test program prefix;
     * 
     *        <code>PROCESSOR_NAME</code> -
     *        use processor name as test program prefix.
     */
    public static String getStrategyName(int testNameStrategy)
    {
        switch(testNameStrategy)
        {
            case SITUATION_NAME:
                { return "SituationName"; }
            case INSTRUCTION_NAME:
                { return "InstructionName"; }
            case GROUP_NAME:
                { return "GroupName"; }
            case PROCESSOR_NAME:
                { return "ProcessorName"; }
            default:
                { return "SpecifiedName"; }
        }
    }
    
    /**
     * Returns a string representation of the settings.
     * 
     * @return a string representation of the settings.
     */
    public String toString()
    {
        return getStrategyName(testNameStrategy);
    }
    
    /**
     * Returns a copy of the settings.
     *
     * @return a copy of the settings.
     */
    public SettingsConfig clone()
    {
    	return new SettingsConfig(this);
    }
}
