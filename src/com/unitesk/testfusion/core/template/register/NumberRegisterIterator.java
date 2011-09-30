/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: NumberRegisterIterator.java,v 1.5 2009/06/10 16:24:10 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.register;

import java.util.ArrayList;

import com.unitesk.testfusion.core.dependency.RegisterDependency;
import com.unitesk.testfusion.core.iterator.Int32RangeIterator;
import com.unitesk.testfusion.core.model.Operand;

/**
 * Number iterator of register dependencies between instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class NumberRegisterIterator extends RandomRegisterIterator
{
    /** Iterates numbers of dependencies. */
    protected Int32RangeIterator iterator = new Int32RangeIterator();
    
    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the register dependency.
     */
    public NumberRegisterIterator(RegisterDependency dependency)
    {
        super(dependency);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the register dependency.
     * 
     * @param <code>flags</code> the types of dependencies.
     * 
     * @param <code>minNumber</code> the minimum number of dependencies.
     * 
     * @param <code>maxNumber</code> the maximum number of dependencies.
     */
    public NumberRegisterIterator(RegisterDependency dependency, int flags, int minNumber, int maxNumber)
    {
        super(dependency, flags, minNumber, maxNumber);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to register iterator.
     */
    protected NumberRegisterIterator(NumberRegisterIterator r)
    {
        super(r);
    }
    
    /** Initializes the iterator. */
    @Override
    public void init()
    {
        super.init();
        
        ArrayList<Operand> operands = getDependentOperands();
        
        int size = 0;
        
        for(Operand operand : operands)
        {
            if(operand.isOutput() && (isDefineDefine() || isUseDefine()) ||
               operand.isInput()  && (isDefineUse()    || isUseUse()))
                { size++; }
        }
        
        int max = Math.min(Math.max(maxNumber, 0), size);
        int min = Math.min(Math.max(minNumber, 0), max);
        
        iterator.setMinValue(min);
        iterator.setMaxValue(max);
        
        iterator.init();
        
        construct(iterator.int32Value());
    }
    
    /**
     * Checks if the iterator of dependencies is not exhausted.
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean hasValue()
    {
        return iterator.hasValue();
    }
    
    /** Makes iteration. */
    @Override
    public void next()
    {
        iterator.next();
        
        construct(iterator.int32Value());
    }
    
    /** Stops the iterator. */
    @Override
    public void stop()
    {
        iterator.stop();
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    @Override
    public NumberRegisterIterator clone()
    {
        return new NumberRegisterIterator(this);
    }
}
