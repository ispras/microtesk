/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: RandomRegisterIterator.java,v 1.6 2009/06/10 16:24:10 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.register;

import java.util.ArrayList;

import com.unitesk.testfusion.core.dependency.Dependencies;
import com.unitesk.testfusion.core.dependency.Dependency;
import com.unitesk.testfusion.core.dependency.RegisterDependency;
import com.unitesk.testfusion.core.generator.Random;
import com.unitesk.testfusion.core.model.Operand;

/**
 * Random iterator of register dependencies between instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RandomRegisterIterator extends RegisterIterator
{
    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the register dependency.
     */
    public RandomRegisterIterator(RegisterDependency dependency)
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
    public RandomRegisterIterator(RegisterDependency dependency, int flags, int minNumber, int maxNumber)
    {
        super(dependency, flags, minNumber, maxNumber);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to register iterator.
     */
    protected RandomRegisterIterator(RandomRegisterIterator r)
    {
        super(r);
    }

    /**
     * Construct randomly <code>number</code> dependencies.
     * 
     * @param <code>number</code> the number of dependencies to be constructed.
     */
    protected void construct(int number)
    {
        int i, size;
        
        ArrayList<Operand> operands = getDependentOperands();
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        
        size = operands.size();
        for(i = 0; i < size; i++)
        {
            Operand operand = operands.get(i);
            
            if(operand.isOutput() && (isDefineDefine() || isUseDefine()) ||
               operand.isInput()  && (isDefineUse()    || isUseUse()))
                { indexes.add(new Integer(i)); }
        }

        while(number-- > 0)
        {
            int index = Random.int32_non_negative_less(indexes.size());

            Operand operand = operands.get(indexes.get(index));
            Dependencies dependencies = getDependencies(operand);
            Dependency dependency = dependencies.get(Random.int32_non_negative_less(dependencies.size()));
            
            dependency.next();
            
            indexes.remove(index);
        }
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
            if(operand.isOutput() && isDefineDefine() || operand.isInput() && isDefineUse())
                { size++; }
        }
        
        int max = Math.min(Math.max(maxNumber, 0), size);
        int min = Math.min(Math.max(minNumber, 0), max);
        
        construct(Random.int32_range(min, max));
    }
    
    /** Makes iteration. */
    @Override
    public void next()
    {
        stop();
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    @Override
    public RandomRegisterIterator clone()
    {
        return new RandomRegisterIterator(this);
    }
}
