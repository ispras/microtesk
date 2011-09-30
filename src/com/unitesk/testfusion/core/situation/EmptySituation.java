/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: EmptySituation.java,v 1.7 2009/07/09 14:48:07 kamkin Exp $
 */

package com.unitesk.testfusion.core.situation;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.Program;

/**
 * Class that represents single-value test situation with empty construction
 * and preparation. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class EmptySituation extends SingleSituation
{
    /** Default constructor. */
    public EmptySituation()
    {
        super("empty");
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the empty situation.
     */
    public EmptySituation(String name)
    {
        super(name);
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the empty situation.
     *
     * @param <code>text</code> the description of the empty situation.
     */
    public EmptySituation(String name, String text)
    {
        super(name, text);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to empty situation object.
     */
    protected EmptySituation(EmptySituation r)
    {
        super(r);
    }
    
    /**
     * Returns <code>true</code> because empty situation is always consistent.
     * 
     * @return <code>true</code>.
     */
    public boolean isConsistent()
    {
        return true;
    }

    /**
     * Constructs the empty situation (does nothing).
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     */
	public boolean construct(Processor processor, GeneratorContext context)
	{
        return true;
	}

    /**
     * Returns the empty preparation program.
     * 
     * @param  <code>processor</code> the processor.
     * 
     * @param  <code>context</code> the context of generation.
     * 
     * @param  <code>layer</code> the preparation layer.
     * 
     * @return the empty preparation program.
     */
    public Program prepare(Processor processor, GeneratorContext context, int layer)
    {
        return new Program();
    }
    
    /**
     * Returns a string representation of the situation.
     * 
     * @return a string representation of the situation.
     */
    public String toString()
    {
        return getName();
    }
    
    /**
     * Returns a copy of the test situation.
     * 
     * @return a copy of the test situation.
     */
    public EmptySituation clone()
    {
        return new EmptySituation(this);
    }
}
