/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: DependencyConfig.java,v 1.19 2009/05/21 14:49:51 kamkin Exp $
 */

package com.unitesk.testfusion.core.config;

import com.unitesk.testfusion.core.dependency.Dependency;
import com.unitesk.testfusion.core.template.DependencyIterator;

/**
 * Configuration of register or content dependency.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class DependencyConfig extends IndexConfig
{
	/** Dependency object. */
    protected Dependency dependency;
    
    /**
     * Base constructor.
     * 
     * @param <code>dependency</code> the dependency object.
     */
    public DependencyConfig(Dependency dependency, int iteratorNumber)
    {
    	super(dependency.getDependencyType().getName(), iteratorNumber);
        
        this.dependency = dependency;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to dependency configuration object.
     */
    protected DependencyConfig(DependencyConfig r)
    {
        super(r);
        
        dependency = r.dependency;
    }
    
    /**
     * Returns the dependency object.
     * 
     * @return the dependency object.
     */
    public Dependency getDependency()
    {
        return dependency;
    }
    
    /**
     * Checks if the dependency is register dependency or not.
     * 
     * @return <code>true</code> if the dependency is register dependency;
     *         <code>false</code> otherwise.
     */
    public boolean isRegisterDependency()
    {
        return dependency.isRegisterDependency();
    }
    
    /**
     * Creates dependency iterator according to the configuration.
     * 
     * @return the created dependency iterator.
     */
    public abstract DependencyIterator createDependencyIterator();
    
    /**
     * Returns the full name of the dependency configuration.
     * 
     * @return the full name of the configuration.
     */
    public String getFullName()
    {
        return parent.getFullName() + "." + getName();
    }
    
    /**
     * Returns a copy of the configuration.
     * 
     * @return a copy of the configuration.
     */
    public abstract DependencyConfig clone();
}
