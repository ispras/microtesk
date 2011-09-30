/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: RandomRegisterIteratorConfig.java,v 1.3 2008/09/13 14:25:15 kozlov Exp $
 */

package com.unitesk.testfusion.core.config.register;

import com.unitesk.testfusion.core.dependency.RegisterDependency;
import com.unitesk.testfusion.core.template.register.RandomRegisterIterator;

/**
 * Configuration of random register iterator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RandomRegisterIteratorConfig extends RegisterIteratorConfig
{
    /** String representation of the configuration. */
    public static final String NAME = "RandomRegisterIterator";
    
    /**
     * Constructor.
     *
     * @param <code>dependency</code> the dependency.
     */
    public RandomRegisterIteratorConfig(RegisterDependency dependency)
    {
        super(dependency);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to configuration object.
     */
    protected RandomRegisterIteratorConfig(RandomRegisterIteratorConfig r)
    {
        super(r);
    }
    
    /**
     * Creates iterator of register dependencies according to the configuration.
     * 
     * @return the created iterator of register dependencies.
     */
    public RandomRegisterIterator createRegisterIterator()
    {
        return new RandomRegisterIterator(dependency, flags, minNumber, maxNumber);
    }
    
    /**
     * Returns a string representation of the configuration.
     * 
     * @return a string representation of the configuration.
     */
    public String toString()
    {
        return NAME;
    }
    
    /**
     * Returns a copy of the configuration.
     * 
     * @return a copy of the configuration.
     */
    public RandomRegisterIteratorConfig clone()
    {
        return new RandomRegisterIteratorConfig(this);
    }
}
