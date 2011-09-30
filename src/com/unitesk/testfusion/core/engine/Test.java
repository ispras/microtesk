/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Test.java,v 1.15 2009/08/06 14:05:37 kamkin Exp $
 */

package com.unitesk.testfusion.core.engine;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.Program;

/**
 * Class <code>Test</code> implements so-called test specification, which
 * responsible for writing test programs into files.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Test
{
    public static final int DIGIT_NUMBER = 5;
    
    /** Processor under test. */
    protected Processor processor;
    
    /** Context of generation. */
    protected GeneratorContext context;
    
    /** Test program template. */
    protected TestProgramTemplate template;
    
    /** File prefix. */
    protected String filePrefix;
    
    /** File mark - suffix of prefix to distinguish target and control tests. */
    protected String fileMark;
    
    /** File extension. */
    protected String fileExt;
    
    /** Output directory. */
    protected String outputDirectory;
    
    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     * 
     * @param <code>template</code> the test program template.
     * 
     * @param <code>filePrefix</code> the file prefix.
     * 
     * @param <code>fileMark</code> the file mark.
     * 
     * @param <code>fileExt</code> the file extension.
     * 
     * @param <code>outputDirectory</code> the output directory.
     */
    public Test(Processor processor, GeneratorContext context, TestProgramTemplate template,
        String filePrefix, String fileMark, String fileExt, String outputDirectory)
    {
        this.processor       = processor;
        this.context         = context;
        this.template        = template;
        this.filePrefix      = filePrefix;
        this.fileMark        = fileMark;
        this.fileExt         = fileExt;
        this.outputDirectory = outputDirectory;
    }

    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     * 
     * @param <code>template</code> the test program template.
     * 
     * @param <code>filePrefix</code> the file prefix.
     * 
     * @param <code>fileMark</code> the file mark.
     * 
     * @param <code>fileExt</code> the file extension.
     */
    public Test(Processor processor, GeneratorContext context, TestProgramTemplate template,
        String filePrefix, String fileMark, String fileExt)
    {
        this(processor, context, template, filePrefix, fileMark, fileExt, "");
    }

    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     * 
     * @param <code>template</code> the test program template.
     * 
     * @param <code>filePrefix</code> the file prefix.
     * 
     * @param <code>fileMark</code> the file mark.
     */
    public Test(Processor processor, GeneratorContext context, TestProgramTemplate template,
        String filePrefix, String fileExt)
    {
        this(processor, context, template, filePrefix, "", fileExt, "");
    }
    
    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     * 
     * @param <code>filePrefix</code> the file prefix.
     * 
     * @param <code>fileExt</code> the file extension.
     * 
     * @param <code>outputDirectory</code> the output directory.
     */
    public Test(Processor processor, GeneratorContext context, String filePrefix, String fileExt, String outputDirectory)
    {
        this(processor, context, new DefaultTestProgramTemplate(), filePrefix, fileExt, outputDirectory);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     * 
     * @param <code>filePrefix</code> the file prefix.
     * 
     * @param <code>fileExt</code> the file extension.
     */
    public Test(Processor processor, GeneratorContext context, String filePrefix, String fileExt)
    {
        this(processor, context, filePrefix, fileExt, "");
    }
    
    /**
     * Checks if self-checking test generation is enabled.
     * 
     * @return <code>true</code> if self-checking test generation is enabled;
     *         <code>false</code> otherwise.
     */
    public boolean isSelfCheck()
    {
        return template.isSelfCheck();
    }
    
    /**
     * Enables/disables self-checking test generation.
     * 
     * @param <code>selfCheck</code> the self-checking status.
     */
    public void setSelfCheck(boolean selfCheck)
    {
        template.setSelfCheck(selfCheck);
    }
    
    /**
     * Returns the processor.
     * 
     * @return the processor.
     */
    public Processor getProcessor()
    {
        return processor;
    }
    
    /**
     * Sets the processor.
     *
     * @param <code>processor</code> the processor.
     */
    public void setProcessor(Processor processor)
    {
        this.processor = processor;
    }
    
    /**
     * Returns the context of generation.
     * 
     * @return the context of generation.
     */
    public GeneratorContext getContext()
    {
        return context;
    }
    
    /**
     * Sets the context of generation.
     * 
     * @param <code>context</code> the context of generation.
     */
    public void setContext(GeneratorContext context)
    {
        this.context = context;
    }
    
    /**
     * Returns the test program template.
     * 
     * @return the test program template.
     */
    public TestProgramTemplate getTemplate()
    {
        return template;
    }
    
    /**
     * Sets the test program template.
     * 
     * @param <code>template</code> the test program template.
     */
    public void setTemplate(TestProgramTemplate template)
    {
        this.template = template;
    }
    
    /**
     * Returns the file prefix.
     * 
     * @return the file prefix.
     */
    public String getFilePrefix()
    {
        return filePrefix;
    }
    
    /**
     * Sets the file prefix.
     * 
     * @param <code>filePrefix</code> the file prefix.
     */
    public void setFilePrefix(String filePrefix)
    {
        this.filePrefix = filePrefix;
    }
    
    /**
     * Returns the file mark.
     * 
     * @return the file mark.
     */
    public String getFileMark()
    {
        return fileMark;
    }
    
    /**
     * Sets the file mark.
     * 
     * @param <code>fileMark</code> the file mark.
     */
    public void setFileMark(String fileMark)
    {
        this.fileMark = fileMark;
    }
    
    /**
     * Returns the file extension.
     * 
     * @return the file extension.
     */
    public String getFileExt()
    {
        return fileExt;
    }
    
    /**
     * Sets the file extension.
     * 
     * @param <code>fileExt</code> sets the file extension.
     */
    public void setFileExt(String fileExt)
    {
        this.fileExt = fileExt;
    }
    
    /**
     * Returns the file name without extension.
     * 
     * @param  <code>i</code> the number of test program.
     * 
     * @return the file name without extension. 
     */
    public String getFileNameWithoutExt(int i)
    {
        StringBuffer result = new StringBuffer(filePrefix + (fileMark.isEmpty() ? "" : "_" + fileMark));

        int j = i;
        int n = DIGIT_NUMBER - 1;

        while((j /= 10) != 0)
            { n--; }
        
        result.append("_");

        while(n-- > 0)
            { result.append('0'); }
        
        result.append(Integer.toString(i));

        return result.toString();
    }
    
    /**
     * Returns the file name of test program.
     * 
     * @param  <code>i</code> the number of test program.
     * 
     * @return the file name of test program. 
     */
    public String getFileName(int i)
    {
        return getFileNameWithoutExt(i) + fileExt;
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
     * Writes the test program into the file.
     * 
     * @param <code>fileName</code> the file name of test program.
     * 
     * @param <code>testProgram</code> the test program.
     * 
     * @param <code>testPrefix</code> the test prefix.
     * 
     * @param <code>testSuffix</code> the test suffix.
     */
    public void writeTest(String fileName, Program testProgram, Program testPrefix, Program testSuffix)
    {
        TestProgramWriter writer = new TestProgramWriter(fileName, outputDirectory);
        
        writer.write(template.getHeader());
        writer.write(template, testPrefix);
        writer.write(template, testProgram);
        writer.write(template, testSuffix);
        writer.write(template.getFooter());
        writer.close();
    }
    
    /**
     * Writes the test program into the file.
     * 
     * @param <code>i</code> the number of test program.
     * 
     * @param <code>testProgram</code> the test program.
     * 
     * @param <code>testPrefix</code> the test prefix.
     * 
     * @param <code>testSuffix</code> the test suffix.
     */
    public void writeTest(int i, Program testProgram, Program testPrefix, Program testSuffix)
    {
        writeTest(getFileName(i), testProgram, testPrefix, testSuffix);
    }
}
