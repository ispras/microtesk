/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ExhaustiveRegisterIterator.java,v 1.10 2010/01/13 11:47:13 vorobyev Exp $
 */

package com.unitesk.testfusion.core.template.register;

import com.unitesk.testfusion.core.dependency.Dependency;
import com.unitesk.testfusion.core.dependency.RegisterDependency;
import com.unitesk.testfusion.core.iterator.IndexArrayIterator;
import com.unitesk.testfusion.core.iterator.Int32RangeIterator;
import com.unitesk.testfusion.core.model.Operand;

/**
 * Exhaustive iterator of register dependencies between instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ExhaustiveRegisterIterator extends RegisterIterator
{
    /** Iterator of dependencies number. */
    private Int32RangeIterator numberIterator = new Int32RangeIterator();
    
    /** Iterator of dependencies indexes. */
    private IndexArrayIterator indexIterator = new IndexArrayIterator();
    
    /** Set of dependent operands. */
    private DependencyCounter dependencyCounter = new DependencyCounter();
    
    /** Indexes of permitted dependencies. */
    private int[] indexes = new int[dependencies.size()];
    
    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the register dependency.
     */
    public ExhaustiveRegisterIterator(RegisterDependency dependency)
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
    public ExhaustiveRegisterIterator(RegisterDependency dependency, int flags, int minNumber, int maxNumber)
    {
        super(dependency, flags, minNumber, maxNumber);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to register iterator.
     */
    protected ExhaustiveRegisterIterator(ExhaustiveRegisterIterator r)
    {
        super(r);
        
        numberIterator = r.numberIterator.clone();
        indexIterator = r.indexIterator.clone();
        dependencyCounter = r.dependencyCounter.clone();
        
        int i, size;
        
        if(r.indexes == null)
            { indexes = null; }
        else
        {
            indexes = new int[size = r.indexes.length];
            for(i = 0; i < size; i++)
                { indexes[i] = r.indexes[i]; }
        }
    }
    
    private void initDependencies()
    {
        int i, size;
        
        size = dependencies.size();
        for(i = 0; i < size; i++)
        {
            Dependency dependency = dependencies.get(i);
            
            dependency.init();
        }
        
        dependencyCounter.clear();
    }
    
    private void createDependencies()
    {
        int i, size;
        int[] indexArray = indexIterator.indexArrayValue();

        initDependencies();
        
        size = indexIterator.getArraySize();
        for(i = 0; i < size; i++)
        {
            Dependency dependency = dependencies.get(indexes[indexArray[i]]);
            Operand operand = dependency.getDependentOperand();
            
            dependency.next();
            dependencyCounter.increment(operand);
        }
    }
    
    private void step()
    {
        if(!hasValue())
            { return; }

        if(indexIterator.hasValue())
        {
            indexIterator.next();
            
            if(indexIterator.hasValue())
                { return; }
        }
        
        if(numberIterator.hasValue())
        {
            numberIterator.next();
            
            if(numberIterator.hasValue())
                { return; }
        }
        
        stop();
    }
    
    private boolean createDependenciesAndCheckInconsistency()
    {
        createDependencies();
        
        int countOperand    = dependencyCounter.countOperand();
        int countDependency = dependencyCounter.countDependency();
        
        return countDependency != countOperand || !isConsistent();
    }
    
    /** Initializes the iterator. */
    @Override
    public void init()
    {
        int i, j, size;
        
        size = dependencies.size();
        
        indexes = new int[size];
        
        for(i = j = 0; i < size; i++)
        {
            Dependency dependency = dependencies.get(i);
            
            Operand operandDeps = dependency.getDependentOperand();
            Operand operandFrom = dependency.getDeterminantOperand();
            
            if(operandDeps.isInput()  && operandFrom.isOutput() && !isDefineUse())
                { continue; }
            
            if(operandDeps.isOutput() && operandFrom.isOutput() && !isDefineDefine())
                { continue; }
            
            if(operandDeps.isInput()  && operandFrom.isInput()  && !isUseUse())
                { continue; }
            
            if(operandDeps.isOutput() && operandFrom.isInput()  && !isUseDefine())
                { continue; }
            
            dependency.init();

            indexes[j++] = i;
        }

        // Can not meet the requirement.
        if(j < minNumber)
            { stop(); return; }
        
        hasValue = true;

        numberIterator.setMinValue(minNumber);
        numberIterator.setMaxValue(Math.min(j, maxNumber));
        numberIterator.setIncrementValue(1);
        numberIterator.init();

        if(!numberIterator.hasValue())
            { stop(); return; }
        
        indexIterator.setMinIndex(0);
        indexIterator.setMaxIndex(j - 1);
        indexIterator.setArraySize(minNumber);
        indexIterator.init();
        
        if(!indexIterator.hasValue())
            { stop(); return; }
        
        while(hasValue() && createDependenciesAndCheckInconsistency())
            {  step(); }
    }
    
    /** Makes iteration. */
    @Override
    public void next()
    {
        do { step(); }
        while(hasValue() && createDependenciesAndCheckInconsistency());
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    @Override
    public ExhaustiveRegisterIterator clone()
    {
        return new ExhaustiveRegisterIterator(this);
    }
}
