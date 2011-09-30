/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigWalker.java,v 1.13 2008/08/19 11:54:26 vorobyev Exp $
 */

package com.unitesk.testfusion.core.config.walker;

import com.unitesk.testfusion.core.config.*;

/**
 * Depth-first configuration hierarchy walker.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ConfigWalker
{
    /** Visit all descendant items recursively. */
    public static final int VISIT_ALL = 0;
    
    /** Visit immediate children only. */
    public static final int VISIT_IMMEDIATE_CHILDREN = 1;
    
    /** Target configuration. */
    protected Config config;
    
    /** Configuration visitor. */
    protected ConfigVisitor visitor;
    
    /**
     * Mode of traversal. It can possess the following values:
     * 
     * <code>VISIT_ALL</code> -
     * visit all descendant items recursively
     * 
     * <code>VISIT_IMMEDIATE_CHILDREN</code> -
     * visit immediate children only. 
     */
    protected int mode;
    
    /**
     * Constructor.
     * 
     * @param <code>config</code> the configuration to be traversed.
     * @param <code>visitor</code> the configuration visitor.
     * @param <code>mode</code> the mode of traversal.
     */
    public ConfigWalker(Config config, ConfigVisitor visitor, int mode)
    {
        this.config = config;
        this.visitor = visitor;
        this.mode = mode;
    }
    
    /**
     * Constructor. By default <code>VISIT_ALL</code> mode is used.
     * 
     * @param <code>config</code> the configuration to be traversed.
     * @param <code>visitor</code> the configuration visitor.
     */
    public ConfigWalker(Config config, ConfigVisitor visitor)
    {
        this(config, visitor, VISIT_ALL);
    }
    
    protected void processTestSuite(TestSuiteConfig testSuite)
    {
        int i, size;
        
        visitor.onTestSuite(testSuite);
        
        if(mode == VISIT_IMMEDIATE_CHILDREN && testSuite.getParent() == config)
        	{ return; }
        
        size = testSuite.countTest();
        
    	TestConfig test;
    	
        for(i = 0; i < size; i++)
        {
        	test = testSuite.getTest(i);
        	processTest(test);
        }
    }
    
    /**
     * Processes test configuration.
     * 
     * @param <code>test</code> the test configuration.
     */
    protected void processTest(TestConfig test)
    {
        int i, size;
        
        visitor.onTest(test);
        
        if(mode == VISIT_IMMEDIATE_CHILDREN && test.getParent() == config)
            { return; }
        
        size = test.countSection();
        for(i = 0; i < size; i++)
        {
            SectionConfig section = test.getSection(i);
            
            processSection(section);
        }
    }
    
    /**
     * Processes section configuration.
     * 
     * @param <code>section</code> the section configurtion.
     */
    protected void processSection(SectionConfig section)
    {
        int i, size;

        visitor.onSection(section);
        
        if(mode == VISIT_IMMEDIATE_CHILDREN && section.getParent() == config)
            { return; }
        
        if(section.isLeaf())
            { processProcessor(section.getProcessor()); }
        else
        {
            size = section.countSection();
            for(i = 0; i < size; i++)
            {
                SectionConfig subsection = section.getSection(i);
                
                processSection(subsection);
            }
        }
    }
    
    /**
     * Processes processor configuration.
     * 
     * @param <code>processor</code> the processor configuration.
     */
    protected void processProcessor(ProcessorConfig processor)
    {
        int i, size;

        if(processor == null)
            { return; }
    
        visitor.onProcessor(processor);
        
        if(mode == VISIT_IMMEDIATE_CHILDREN && processor.getParent() == config)
            { return; }
        
        size = processor.countGroup();
        for(i = 0; i < size; i++)
        {
            GroupConfig group = processor.getGroup(i);
            
            processGroup(group);
        }
    }
    
    /**
     * Processes group configuration.
     * 
     * @param <code>group</code> the group configuration.
     */
    protected void processGroup(GroupConfig group)
    {
        int i, size;
        
        visitor.onGroup(group);
        
        if(mode == VISIT_IMMEDIATE_CHILDREN && group.getParent() == config)
            { return; }
        
        size = group.countGroup();
        for(i = 0; i < size; i++)
        {
            GroupConfig subgroup = group.getGroup(i);
            
            processGroup(subgroup);
        }
        
        size = group.countInstruction();
        for(i = 0; i < size; i++)
        {
            InstructionConfig instruction = group.getInstruction(i);
            
            processInstruction(instruction);
        }
    }
    
    /**
     * Processes instruction configuration.
     * 
     * @param <code>instruction</code> the instruction configuration.
     */
    protected void processInstruction(InstructionConfig instruction)
    {
        int i, size;
        
        visitor.onInstruction(instruction);
        
        if(mode == VISIT_IMMEDIATE_CHILDREN && instruction.getParent() == config)
            { return; }
        
        size = instruction.countSituation();
        for(i = 0; i < size; i++)
        {
            SituationConfig situation = instruction.getSituation(i);
            
            processSituation(situation);
        }
    }
    
    /**
     * Processes test situation configuration.
     * 
     * @param <code>situation</code> the test situation configuration.
     */
    protected void processSituation(SituationConfig situation)
    {
        visitor.onSituation(situation);
    }
    
    /** Traverses configuration. */
    public void process()
    {
        visitor.onStart(config);
        
        if(config instanceof TestSuiteConfig)
        	{ processTestSuite((TestSuiteConfig) config); }
        else if(config instanceof TestConfig)
            { processTest((TestConfig)config); }
        else if(config instanceof SectionConfig)
            { processSection((SectionConfig)config); }
        else if(config instanceof ProcessorConfig)
            { processProcessor((ProcessorConfig)config); }
        else if(config instanceof GroupConfig)
            { processGroup((GroupConfig)config); }
        else if(config instanceof InstructionConfig)
            { processInstruction((InstructionConfig)config); }
        else if(config instanceof SituationConfig)
            { processSituation((SituationConfig)config); }
        
        visitor.onEnd();
    }
}
