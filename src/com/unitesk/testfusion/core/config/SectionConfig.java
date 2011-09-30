/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SectionConfig.java,v 1.32 2008/10/30 08:17:49 kozlov Exp $
 */

package com.unitesk.testfusion.core.config;

/**
 * Configuration of test template section.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SectionConfig extends SectionListConfig
{
    /** Processor configuration. */
    protected ProcessorConfig processor = new ProcessorConfig();
    
    /** Default constructor. */ 
    public SectionConfig() {}

    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor configuration.
     * 
     * @param <code>deps</code> the dependency list configuration.
     */
    public SectionConfig(ProcessorConfig processor, DependencyListConfig deps)
    {
        super(deps);
        
        this.processor = processor;
        
        this.processor.setParent(this);
    }
    
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the section.
     * 
     * @param <code>processor</code> the processor configuration.
     * 
     * @param <code>deps</code> the dependency list configuration.
     */
    public SectionConfig(String name, ProcessorConfig processor, DependencyListConfig deps)
    {
        super(name, deps);
        
        this.processor = processor;
        
        this.processor.setParent(this);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to section configuration object.
     */
    protected SectionConfig(SectionConfig r)
    {
        super(r);
     
        processor = r.processor.clone();
        processor.setParent(this);
    }
    
    /**
     * Returns the fully qualified name of the section.
     * 
     * @return the fully qualified name of the section.
     */
    public String getFullName()
    {
        if(isTop())
            { return getName(); }
        
        return parent.getFullName() + "." + getName();
    }
    
    /**
     * Checks if the section configuration is on top of the section hierarchy,
     * i.e. it has no parent or its parent is not a section configuration.
     * 
     * @return <code>true</code> if the section configuration is top;
     * <code>false</code> otherwise.
     */
    public boolean isTop()
    {
        return parent == null || !(parent instanceof SectionConfig);
    }
    
    /**
     * Checks if the section configuration is a leaf of the section hierarchy,
     * i.e. it has no subsections.
     * 
     * @return <code>true</code> if the section configuration is leaf;
     * <code>false</code> otherwise.
     */
    public boolean isLeaf()
    {
        return sections.isEmpty();
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
     * @param <code>processor</code> the new processor configuration.
     */
    public void setProcessor(ProcessorConfig processor)
    {
        this.processor = processor;
    }
    
    /**
     * Returns a copy of the section configuration.
     *
     * @return a copy of the section configuration.
     */
    public SectionConfig clone()
    {
        return new SectionConfig(this);
    }
}
