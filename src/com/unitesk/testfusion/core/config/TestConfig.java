/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestConfig.java,v 1.46 2010/01/13 11:46:59 vorobyev Exp $
 */

package com.unitesk.testfusion.core.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.unitesk.testfusion.core.config.register.RegisterIteratorConfig;
import com.unitesk.testfusion.core.config.walker.ConfigEmptyVisitor;
import com.unitesk.testfusion.core.config.walker.ConfigLocalizer;
import com.unitesk.testfusion.core.config.walker.ConfigWalker;
import com.unitesk.testfusion.core.engine.GeneratorEngine;
import com.unitesk.testfusion.core.engine.Test;
import com.unitesk.testfusion.core.engine.TestProgramTemplate;
import com.unitesk.testfusion.gui.GUI;

/**
 * Root configuration of MicroTESK test.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class TestConfig extends SectionListConfig
{
    /** Maximum value of test size. */
	public static final int MAX_TEST_SIZE = Integer.MAX_VALUE;
	
    /** Processor configuration. */
    protected ProcessorConfig processor = new ProcessorConfig();
    
    /** List of dependency configurations. */
    protected DependencyListConfig deps = new DependencyListConfig();

    /** Settings. */
    protected SettingsConfig settings = new SettingsConfig();
    
    /** Test program generator. */
    protected GeneratorEngine generator;
    
    /** Test size (number of test actions within a test program). */
    protected int testSize;
    
    /** Enables/disables self-checking test generation. */
    protected boolean selfCheck;
    
    /** Default constructor. */
    public TestConfig()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> the test name.
     */
    public TestConfig(String name)
    {
        super(name);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor configuration.
     */
    public TestConfig(ProcessorConfig processor)
    {
        this();
        
        this.processor = processor;
    }
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the test name.
     * 
     * @param <code>processor</code> the processor configuration.
     */
    public TestConfig(String name, ProcessorConfig processor)
    {
        super(name);
        
        this.processor = processor;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to test configuration object.
     */
    protected TestConfig(TestConfig r)
    {
        super(r);

        if(processor != null)
            { processor = r.processor.clone(); }
        
        deps = r.deps.clone();
        settings  = r.settings.clone();

        // Generator objects is not copied deeply.
        generator = r.generator;
        
        testSize = r.testSize;
        selfCheck = r.selfCheck;
    }
    
    /**
     * Returns the dependecy list configuration.
     * 
     * @return the dependecy list configuration.
     */
    public DependencyListConfig getDependencies()
    {
        return deps;
    }
    
    /**
     * Sets the dependency list configuration.
     * 
     * @param <code>deps</code> the dependecy list configuration. 
     */
    public void setDependencies(DependencyListConfig deps)
    {
        this.deps = deps;
        
        // set dependency list configurations in options
        getOptions().setDependencies(deps.clone());
        getOptions().getCrossDependencies().setClearCopyOfDeps(deps.clone());
    }
    
    /**
     * Returns the processor configuration.
     * 
     * @return the processor configuration.
     */
    public ProcessorConfig getProcessor()
    {
        return processor;
    }

    /**
     * Sets the processor configuration.
     * 
     * @param <code>processor</code> the processor configuration.
     */
    public void setProcessor(ProcessorConfig processor)
    {
        this.processor = processor;
    }
    
    /**
     * Returns the settings.
     * 
     * @return the settings.
     */
    public SettingsConfig getSettings()
    {
        return settings;
    }
    
    /**
     * Sets the settings.
     * 
     * @param <code>settings</code> the settings.
     */
    public void setSettings(SettingsConfig settings)
    {
        this.settings = settings;
    }
    
    /**
     * Generates test name according to the strategy specified in the settings.
     *
     * @return the test name.
     */
    public String generateTestName()
    {
        int strategy = settings.getTestNameStrategy();
        
        if(strategy == SettingsConfig.SPECIFIED_NAME)
            { return settings.getTestName(); }

        Config config = this;
        ConfigLocalizer localizer = new ConfigLocalizer();
        ConfigWalker walker = new ConfigWalker(this, localizer);
        
        walker.process();
        
        switch(strategy)
        {
            case SettingsConfig.SITUATION_NAME:
            {
                if(localizer.isSituationLocalized())
                    { config = localizer.getSituation(); break; }
                
                // Do not break
            }
            case SettingsConfig.INSTRUCTION_NAME:
            {
                if(localizer.isInstructionLocalized())
                    { config = localizer.getInstruction(); break; }
                
                // Do not break
            }
            case SettingsConfig.GROUP_NAME:
            {
                if(localizer.isGroupLocalized())
                    { config = localizer.getGroup(); break; }
                
                // Do not break
            }
        }
        
        String testName;
        
        if(settings.isFullName())
        {
            String processorName = getProcessor().getName();
            
            if(config instanceof TestConfig || config instanceof SectionConfig)
                { testName = processorName; }
            else
                { testName = processorName + "_" + config.getFullName(); }
        }
        else
        {
            if(config instanceof SituationConfig)
                { testName = config.getParent().getName() + "_" + config.getName(); }
            else
                { testName = config.getName(); }
        }
        
        return testName.toLowerCase().replace('.', '_');
    }
    
    /**
     * Returns the test program generator.
     * 
     * @return the test program generator.
     */
    public GeneratorEngine getGenerator()
    {
        return generator;
    }
    
    /**
     * Sets the test program generator.
     * 
     * @param <code>generator</code> the test program generator.
     */
    public void setGenerator(GeneratorEngine generator)
    {
        this.generator = generator;
    }
    
    /**
     * Returns the test size (number of test action within a test program).
     * 
     * @return the test size.
     */
    public int getTestSize()
    {
        return testSize;
    }
    
    /**
     * Sets the test size (number of test action within a test program).
     * 
     * @param <code>testSize</code> the test size.
     */
    public void setTestSize(int testSize)
    {
        this.testSize = testSize;
    }
    
    /**
     * Checks if self-checking test generation is enabled.
     * 
     * @return <code>true</code> if self-checking test generation is enabled;
     *         <code>false</code> otherwise.
     */
    public boolean isSelfCheck()
    {
        return selfCheck;
    }
    
    /**
     * Enables/disables self-checking test generation.
     * 
     * @param <code>selfCheck</code> the self-checking status.
     */
    public void setSelfCheck(boolean selfCheck)
    {
        this.selfCheck = selfCheck;
    }
    
    /**
     * Creates the section configuration.
     * 
     * @return the created section configuration.
     */
    public SectionConfig createSection()
    {
        return new SectionConfig(processor.clone(), deps.clone());
    }

    /**
     * Creates the section configuration.
     * 
     * @param <code>name</code> the name of the section.
     * @return the created section configuration.
     */
    public SectionConfig createSection(String name)
    {
        return new SectionConfig(name, processor.clone(), deps.clone());
    }
    
    /**
     * Returns name of the file with test configuration.
     *  
     * @return name of the file with test configuration.
     */
    public String getFileName()
    {
        return (name.equals(Config.DEFAULT_CONFIGURATION_NAME)) ?
                name : name + "." + GUI.EXTENSION;
    }
    
    protected class InfoCollectorVisitor extends ConfigEmptyVisitor
    {
    	/** Map from Equivalence class to Info. */
    	HashMap<String, String> equivalenceInfoHash = new HashMap<String, String>();

        /**
         * Handler of instruction configuration.
         * 
         * @param <code>instruction</code> the instruction configuration.
         */
        public void onInstruction(InstructionConfig instruction) 
        {
        	Config parent = instruction.getParent();

        	if((parent instanceof GroupConfig) && instruction.isSelected())
        	{
        		GroupConfig group = (GroupConfig)parent;
        		
        		String className = instruction.getEquivalenceClass();
        		String info = getClassInfo(className );
        		
        		info += String.format("%-30s", "\t" + instruction.toString() + ": ");
        		info += String.format("%-30s", "group=" + group.getName()) + "\n";
        		
        		setClassInfo(className, info);
        	}
        }
        
        protected String getClassInfo(String className)
        {
        	if(!equivalenceInfoHash.containsKey(className) )
        	{
        		equivalenceInfoHash.put(className, new String());
        	}
        	
    		return equivalenceInfoHash.get(className);
        }
        
        protected void setClassInfo(String className, String info)
        {
        	equivalenceInfoHash.put(className, info);
        }
        
        public String getInfo()
        {
        	Set<String> keySet = equivalenceInfoHash.keySet();
        	
        	Iterator<String> iterator = keySet.iterator();
        	
        	String key;
        	String info = "";
        	
        	while(iterator.hasNext())
        	{
        		key = iterator.next();
        		String infoEquivalence = equivalenceInfoHash.get(key);
        		
        		info += ("\tClass " + key + ": \n");
        		info += infoEquivalence + "\n";
        	}
        	
        	return info;
        }
    }
    
    public void generateConfigurationInfo(String fileName)
    {
    	String info = new String();

    	Test targetTest = generator.getTargetTest(); 
    	TestProgramTemplate testProgramTemplate = targetTest.getTemplate();
    	
    	// Set header
    	info += testProgramTemplate.getHeader();

    	// Set dependencies info
    	info += "\t/****************************************************************************************/\n";
    	info += "\t/* Dependencies:                                                                        */\n";
    	info += "\t/* Format: Dependency[Number]: TypeOfRegister[ContentType]                              */\n"; 
    	info += "\t/*     Min=minCount                                                                     */\n";
    	info += "\t/*     Max=maxCount                                                                     */\n";
    	info += "\t/*     Define-Define=enableDependency                                                   */\n";
    	info += "\t/*     Define-Use=enableDependency                                                      */\n";
    	info += "\t/*     Use-Define=enableDependency                                                      */\n";
    	info += "\t/*     Use-Use=enableDependency                                                         */\n";
    	info += "\t/* Description:                                                                         */\n";
    	info += "\t/*     Number - serial number of dependency.                                            */\n";
    	info += "\t/*     TypeOfRegister - name of processor register type for dependency.                 */\n";
    	info += "\t/*     ContentType - optional name of register content type,                            */\n";  
    	info += "\t/*         if it is not set - dependency exists for all content types.                  */\n";
    	info += "\t/*     Min - less dependency count of current type in the test situation                */\n"; 
    	info +=	"\t/*         if dependency exists.                                                        */\n";
    	info += "\t/*     Max - greatest dependency count of current type in the test situation            */\n";
    	info += "\t/*         if dependency exists.                                                        */\n";
    	info += "\t/*     Define-Define - using of write-write dependency.                                 */\n";
    	info += "\t/*     Define-Use - using of write-read dependency.                                     */\n";
    	info += "\t/*     Use-Define - using of read-write dependency.                                     */\n";
    	info += "\t/*     Use-Use - using of read-read dependency.                                         */\n";
    	info += "\t/****************************************************************************************/\n";
    	
    	DependencyConfig depConfig;
    	for(int i = 0; i < deps.countDependency(); ++i)
    	{
    		depConfig = deps.getDependency(i);
			info += "\tdependency[" + i + "]: " + depConfig + "\n";    		
    		
			if(depConfig instanceof RegisterDependencyConfig)
    		{
    			RegisterDependencyConfig regDepConfig = (RegisterDependencyConfig)depConfig;
    			RegisterIteratorConfig registerIteratorConfig = regDepConfig.getRegisterIterator();
    			
    			info += "\tMin=" + registerIteratorConfig.getMinNumber() + "\n";
    			info += "\tMax=" + registerIteratorConfig.getMaxNumber() + "\n";
    			
    			info += "\tDefine-Define:" + registerIteratorConfig.isDefineDefine() + "\n";
    			info += "\tDefine-Use: " + registerIteratorConfig.isDefineUse() + "\n";
    			info += "\tUse-Define: " + registerIteratorConfig.isUseDefine() + "\n";
    			info += "\tUse-Use: " + registerIteratorConfig.isUseUse() + "\n";
    		}
			
			info += "\n";
    	}
    	info += "\n";

    	InfoCollectorVisitor visitor = new InfoCollectorVisitor();
    	ConfigWalker configWalker = new ConfigWalker(this, visitor, ConfigWalker.VISIT_ALL);
    	configWalker.process();
    	
    	// Set instructions info
    	info += "\t/****************************************************************************************/\n";
    	info += "\t/* Instructions:                                                                        */\n";
    	info += "\t/* Format: Class=className:                                                             */\n";
    	info += "\t/*     Instruction: Group=groupName                                                     */\n";
    	info += "\t/* Description:                                                                         */\n";
    	info += "\t/*     Class - name of equivalence class, if it is null then it is unique               */\n"; 
    	info += "\t/*         for instruction                                                              */\n";
    	info += "\t/*     Instruction - instruction name.                                                  */\n";
    	info += "\t/*     Group - group name, may used as default equivalence class                        */\n"; 
    	info +=	"\t/*         else it is meaningless field.                                                */\n";
    	info += "\t/****************************************************************************************/\n";
    	
    	info += visitor.getInfo();
    	
    	try
		{
    		File file = new File(fileName);
    		file.createNewFile();
    		
    		FileWriter fstream = new FileWriter(file);
    		BufferedWriter out = new BufferedWriter(fstream);

	    	out.write(info);
	    	out.flush();
	    	out.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
    }
    
    /**
     * Returns a copy of the test configuration.
     *
     * @return a copy of the test configuration.
     */
    public TestConfig clone()
    {
        return new TestConfig(this);
    }
}
