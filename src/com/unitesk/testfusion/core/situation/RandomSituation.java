/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: RandomSituation.java,v 1.2 2009/07/09 14:48:07 kamkin Exp $
 */

package com.unitesk.testfusion.core.situation;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.generator.Random;
import com.unitesk.testfusion.core.model.Operand;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.Program;

/**
 * Class that represents single-value random test situation. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class RandomSituation extends SingleSituation
{
    /** Default constructor. */
    public RandomSituation()
    {
        super("random");
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the situation.
     */
    public RandomSituation(String name)
    {
        super(name);
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the situation.
     * 
     * @param <code>text</code> the description of the situation.
     */
    public RandomSituation(String name, String text)
    {
        super(name, text);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to situation object.
     */
    protected RandomSituation(RandomSituation r)
    {
        super(r);
    }

    /**
     * Returns <code>true</code> because empty situation is always consistent.
     * 
     * @return <code>true</code>.
     */
    @Override
    public boolean isConsistent()
    {
        return true;
    }

    /**
     * Constructs random situation.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     */
    @Override
    public boolean construct(Processor processor, GeneratorContext context)
    {
        for(int i = 0; i < countOperand(); i++)
        {
            Operand operand = getOperand(i);
            
            operand.setLongValue(Random.value(operand.getContentType()));
        }
        
        return true;
    }
    
    /**
     * Returns a program that makes the preparation of the given layer of the
     * current test situation.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     * 
     * @param <code>layer</code> the preparation layer.
     */
    @Override
    public abstract Program prepare(Processor processor, GeneratorContext context, int layer);
}
