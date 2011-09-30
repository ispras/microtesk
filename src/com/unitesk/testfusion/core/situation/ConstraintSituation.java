/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ConstraintSituation.java,v 1.2 2009/11/18 13:55:51 kamkin Exp $
 */

package com.unitesk.testfusion.core.situation;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.model.Processor;

/**
 * The abstract class that represents a test situation described by means of
 * constraints.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class ConstraintSituation extends Situation
{
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the test situation.
     * @param <code>text</code> the textual description of the test situation.
     */
    public ConstraintSituation(String name, String text)
    {
        super(CONSTRAINT, name, text);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the test situation.
     */
    public ConstraintSituation(String name)
    {
        this(name, "");
    }
    
    /** Default constructor. */
    public ConstraintSituation()
    {
        this("");
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to a constraint situation object.
     */
    protected ConstraintSituation(ConstraintSituation r)
    {
        super(r);
    }
 
    /**
     * Constructs the empty situation (does nothing).
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     */
    @Override
    public boolean construct(Processor processor, GeneratorContext context)
    {
        return true;
    }
}
