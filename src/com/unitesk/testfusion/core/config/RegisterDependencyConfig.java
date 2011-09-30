/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: RegisterDependencyConfig.java,v 1.3 2008/09/17 14:28:40 kozlov Exp $
 */

package com.unitesk.testfusion.core.config;

import com.unitesk.testfusion.core.config.register.*;
import com.unitesk.testfusion.core.dependency.RegisterDependency;
import com.unitesk.testfusion.core.template.DependencyIterator;

/**
 * Configuration of register dependency.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RegisterDependencyConfig extends DependencyConfig
{
    /** Index of the exhaustive iterator of register dependencies. */
    public static final int EXHAUSTIVE_REGISTER_ITERATOR = 0;

    /** Index of the number iterator of register dependencies. */
    public static final int NUMBER_REGISTER_ITERATOR = 1;

    /** Index of the multiset template iterator. */
    public static final int RANDOM_REGISTER_ITERATOR = 2;
    
    /** Number of register dependency iterators. */
    public static final int REGISTER_ITERATOR_NUMBER = 3;

    /** Array of template iterator configurations. */
    protected RegisterIteratorConfig[] registerIterators;
    
    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the register dependency.
     */
    public RegisterDependencyConfig(RegisterDependency dependency)
    {
        super(dependency, REGISTER_ITERATOR_NUMBER);
        
        registerIterators = new RegisterIteratorConfig[]{
                new ExhaustiveRegisterIteratorConfig(dependency),
                new NumberRegisterIteratorConfig(dependency),
                new RandomRegisterIteratorConfig(dependency)
        };
        
        for(RegisterIteratorConfig config : registerIterators)
            { config.setParent(this); }
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the register dependency
     *        configuration.
     */
    protected RegisterDependencyConfig(RegisterDependencyConfig r)
    {
        super(r);
        
        registerIterators = new RegisterIteratorConfig[REGISTER_ITERATOR_NUMBER];
        
        for(int i = 0; i < REGISTER_ITERATOR_NUMBER; i++)
        {
            registerIterators[i] = r.registerIterators[i].clone();
            registerIterators[i].setParent(this);
        }
    }
    
    /**
     * Returns the register iterator configuration by index.
     * 
     * @param  <code>index</code> the value of index.
     * @return the register iterator configuration.
     */
    public RegisterIteratorConfig getRegisterIterator(int index)
    {
        return registerIterators[index];
    }

    /**
     * Returns the register iterator configuration corresponding to the current
     * value of index.
     * 
     * @return the choosen register iterator configuration.
     */
    public RegisterIteratorConfig getRegisterIterator()
    {
        return getRegisterIterator(getIndex());
    }

    /**
     * Creates dependency iterator according to the configuration.
     * 
     * @return the created dependency iterator.
     */
    public DependencyIterator createDependencyIterator()
    {
        RegisterIteratorConfig registerIterator = getRegisterIterator();
        
        return registerIterator.createRegisterIterator();
    }
    
    /**
     * Returns a copy of the configuration.
     *
     * @return a copy of the configuration.
     */
    public RegisterDependencyConfig clone()
    {
        return new RegisterDependencyConfig(this);
    }
}
