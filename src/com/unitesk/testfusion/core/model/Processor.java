/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Processor.java,v 1.7 2008/08/19 10:59:27 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

/**
 * Abstract class that represents a microprocessor.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Processor implements ResetableObject
{
    /** Name of the processor. */
    protected String name;
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the processor.
     */
    public Processor(String name)
    {
        this.name = name;
    }

    /** Default constructor. */
    public Processor()
    {
        this("");
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to processor object.
     */
    protected Processor(Processor r)
    {
        name = r.name;
    }
    
    /**
     * Returns the name of the processor.
     * 
     * @return the name of the processor.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Executes the object.
     * 
     * @param <code>executable</code> the executable object.
     */
    public void execute(Executable executable)
    {
        executable.execute(this);
    }

    /**
     * Returns the name of the processor.
     * 
     * @return the name of the processor.
     */
    public String toString()
    {
        return getName();
    }

    /** Resets the state of the processor. */
    public abstract void reset();
    
    /**
     * Returns a copy of the processor.
     * 
     * @return a copy of the processor.
     */
    public abstract Processor clone();
}
