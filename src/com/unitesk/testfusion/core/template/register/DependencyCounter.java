/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: DependencyCounter.java,v 1.1 2009/06/11 10:05:50 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.register;

import java.util.HashMap;
import java.util.Map.Entry;

import com.unitesk.testfusion.core.model.Operand;

/**
 * Maps operand to number of dependencies of the given type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class DependencyCounter extends HashMap<Operand, Integer>
{
    private static final long serialVersionUID = 1L;

    /** Number of dependencies. */
    protected int number = 0;
    
    /** Default constructor. */
    public DependencyCounter() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> reference to a dependency counter object.
     */
    protected DependencyCounter(DependencyCounter r)
    {
        super(r);
        
        number = r.number;
        
        for(Entry<Operand, Integer> entry : r.entrySet())
            { put(entry.getKey(), entry.getValue()); }
    }
    
    /**
     * Increments counter for the given operand.
     * 
     * @param <code>operand</code> the operand.
     */
    public void increment(Operand operand)
    {
        Integer count = get(operand);
        
        count = count == null ? new Integer(1) : new Integer(count.intValue() + 1);
        
        put(operand, count);
        
        number++;
    }
    
    /**
     * Decrements counter for the given operand.
     * 
     * @param <code>operand</code> the operand.
     */
    public void decrement(Operand operand)
    {
        int count = get(operand);
        
        if(count == 1)
            { remove(operand); }
        else
        {
            put(operand, count - 1);
        }
        
        number--;
    }
    
    public int countOperand()
    {
        return size();
    }
    
    public int countDependency()
    {
        return number;
    }
    
    /**
     * Returns a copy of the dependency counter.
     * 
     * @return a copy of the dependency counter.
     */
    public DependencyCounter clone()
    {
        return new DependencyCounter(this);
    }
}
