/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Module.java,v 1.3 2008/08/19 10:18:35 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

/**
 * Abstract class that represents a microprocessor module.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Module
{
    /** Processor. */
    protected Processor processor;
    
    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor.
     */
    public Module(Processor processor)
    {
        this.processor = processor;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the module object.
     */
    protected Module(Module r)
    {
        processor = r.processor;
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
}
