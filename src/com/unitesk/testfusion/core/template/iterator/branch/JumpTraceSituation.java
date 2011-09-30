/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: JumpTraceSituation.java,v 1.4 2009/07/09 14:48:09 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.Program;

/**
 * Unconditional jump trace situation.
 *  
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class JumpTraceSituation extends BranchTraceSituation
{
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of situation.
     * 
     * @param <code>text</code> the description of situation.
     * 
     * @param <code>branchLabel</code> the branch label (index of target
     *        instruction in test template). 
     *        
     * @param <code>branchTrace</code> the branch execution trace.
     */
    public JumpTraceSituation(String name, String text, int branchNumber, int branchIndex, int branchLabel, BranchTrace branchTrace)
    {
        super(name, text, branchNumber, branchIndex, branchLabel, branchTrace, null, null);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>branchLabel</code> the branch label (index of target
     *        instruction in test template). 
     *        
     * @param <code>branchTrace</code> the branch execution trace.
     */
    public JumpTraceSituation(int branchNumber, int branchIndex, int branchLabel, BranchTrace branchTrace)
    {
        super(branchNumber, branchIndex, branchLabel, branchTrace, null, null);
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of situation.
     * 
     * @param <code>text</code> the description of situation.
     */
    public JumpTraceSituation(String name, String text)
    {
        this(name, text, 0, 0, 0, new BranchTrace());
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of situation.
     */
    public JumpTraceSituation(String name)
    {
        this(name, "");
    }
    
    /** Default constructor. */
    public JumpTraceSituation() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to branch trace situation object.
     */
    protected JumpTraceSituation(JumpTraceSituation r)
    {
        super(r);
    }
    /**
     * Constructs operands values that satisfy branch condition.
     */
    @Override
    public void satisfyCondition() {}
    
    /**
     * Constructs operands values that violate branch condition.
     */
    @Override
    public void violateCondition() {}
    
    /**
     * Performs some initialization for method <code>step()</code>. This method
     * is invoked before calling method <code>step()</code>. It is always
     * invoked by generator regardless of calling method <code>step()</code>.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     */
    @Override
    public void init(Processor processor, GeneratorContext context) {}
    
    /**
     * Returns the step program for the branch instruction. This method is
     * invoked before calling method <code>construct()</code>.
     * 
     * @return the step program for branch instruction.
     */
    @Override
    public Program step()
    {
        return new Program();
    }
    
    /**
     * Constructs the branch trace situation.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     */
    @Override
    public abstract boolean construct(Processor processor, GeneratorContext context);

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
    public abstract Program prepare(Processor processor, GeneratorContext context, int layer);
}
