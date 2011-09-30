/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SimpleTraceSituation.java,v 1.3 2009/07/09 14:48:09 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.Program;

/**
 * Simple situation for conditinal branch instructions.
 *  
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class SimpleTraceSituation extends BranchTraceSituation
{
    /** Default constructor. */
    public SimpleTraceSituation()
    {
    }
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of situation.
     */
    public SimpleTraceSituation(String name)
    {
        super(name);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of situation.
     * 
     * @param <code>text</code> the description of situation.
     */
    public SimpleTraceSituation(String name, String text)
    {
        super(name, text);
    }

    /** Copy constructor. */
    protected SimpleTraceSituation(SimpleTraceSituation r)
    {
        super(r);
    }

    /**
     * Returns possibility of using single-instruction step for the given trace.
     * 
     * @return <code>true</code> if it is possible to use simple step;
     *         <code>false</code> otherwise.
     */
    public abstract boolean canUseSimpleStep();
    
    /**
     * Returns simple step if it is possible.
     * 
     * @return simple step.
     */
    public abstract Program simpleStep();
    
    /**
     * Returns general step if simple step is not possible for the given trace.
     * 
     * @return general step.
     */
    public abstract Program generalStep();
    
    /**
     * Returns the step program for the branch instruction. This method is
     * invoked before calling method <code>construct()</code>.
     * 
     * @return the step program for branch instruction.
     */
    @Override
    public Program step()
    {
        if(branchTrace.getChangeNumber() == 0)
            { return new Program(); }
        
        if(canUseSimpleStep())
            { return simpleStep(); }
        
        return generalStep();
    }

    /**
     * Returns the basic preparation program (if step is absent).
     * 
     * @param  <code>processor</code> the processor.
     * 
     * @param  <code>context</code> the context of generation.
     * 
     * @param  <code>layer</code> the preparation layer.
     * 
     * @return the basic preparation program.
     */
    public abstract Program basicPrepare(Processor processor, GeneratorContext context, int layer);
    
    /**
     * Returns the simple preparation program (if step is simple).
     * 
     * @param  <code>processor</code> the processor.
     * 
     * @param  <code>context</code> the context of generation.
     * 
     * @param  <code>layer</code> the preparation layer.
     * 
     * @return the simple preparation program.
     */
    public abstract Program simplePrepare(Processor processor, GeneratorContext context, int layer);
    
    /**
     * Returns the general preparation program (is step is general).
     * 
     * @param  <code>processor</code> the processor.
     * 
     * @param  <code>context</code> the context of generation.
     * 
     * @param  <code>layer</code> the preparation layer.
     * 
     * @return the general preparation program.
     */
    public abstract Program generalPrepare(Processor processor, GeneratorContext context, int layer);
    
    /**
     * Returns the preparation program for branch instruction. This method is
     * invoked after execution of method <code>construct()</code>.
     * 
     * @param  <code>processor</code> the processor.
     * 
     * @param  <code>context</code> the context of generation.
     * 
     * @param  <code>layer</code> the preparation layer.
     * 
     * @return the preparation program for branch instruction.
     */
    @Override
    public Program prepare(Processor processor, GeneratorContext context, int layer)
    {
        if(branchTrace.isEmpty())
            { return new Program(); }
        
        if(branchTrace.getChangeNumber() == 0)
            { return basicPrepare(processor, context, layer); }
        
        if(canUseSimpleStep())
            { return simplePrepare(processor, context, layer); }
        
        return generalPrepare(processor, context, layer);
    }
}
