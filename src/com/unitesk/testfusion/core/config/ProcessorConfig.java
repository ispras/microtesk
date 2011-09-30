/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ProcessorConfig.java,v 1.15 2008/09/17 14:28:40 kozlov Exp $
 */

package com.unitesk.testfusion.core.config;

import com.unitesk.testfusion.core.model.Processor;

/**
 * Configuration of microprocessor instruction set architecture.
 *  
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ProcessorConfig extends GroupListConfig
{
    /** Processor object. */
    protected Processor processor;
    
    /** Default constructor. */
    public ProcessorConfig()
    {
        super();
    }
    
    /**
     * Basic constructor.
     * 
     * @param <code>processor</code> the processor object.
     */
    public ProcessorConfig(Processor processor)
    {
        super(processor.getName());
        
        this.processor = processor;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to processor configuration object.
     */
    protected ProcessorConfig(ProcessorConfig r)
    {
        super(r);
        
        processor = r.processor;
    }
    
    /**
     * Returns the processor object.
     * 
     * @return the processor object.
     */
    public Processor getProcessor()
    {
        return processor;
    }
    
    /**
     * Sets the processor object.
     * 
     * @param <code>processor</code> the processor object.
     */
    public void setProcessor(Processor processor)
    {
        this.processor = processor;
    }
    
    /**
     * Returns the full name of the processor configuration.
     * 
     * @return the full name of the configuration.
     */
    public String getFullName()
    {
        return parent.getFullName();
    }
    
    /**
     * Returns a copy of the processor configuration.
     *
     * @return a copy of the processor configuration.
     */
    public ProcessorConfig clone()
    {
        return new ProcessorConfig(this);
    }
}
