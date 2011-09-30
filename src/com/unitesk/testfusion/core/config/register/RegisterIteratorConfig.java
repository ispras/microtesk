/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: RegisterIteratorConfig.java,v 1.5 2009/08/06 10:27:20 kamkin Exp $
 */

package com.unitesk.testfusion.core.config.register;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.core.dependency.RegisterDependency;
import com.unitesk.testfusion.core.template.register.RegisterIterator;

public abstract class RegisterIteratorConfig extends Config
{
    /** Dependency. */
    protected RegisterDependency dependency;
    
    /** Types of dependencies to be iterated. */
    protected int flags;
    
    /** Minimum number of register dependencies. */
    protected int minNumber = 0;
    
    /** Maximum number of register dependencies. */
    protected int maxNumber = Integer.MAX_VALUE;

    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the dependency.
     */
    public RegisterIteratorConfig(RegisterDependency dependency)
    {
        this.dependency = dependency;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to register iterator configuration.
     */
    protected RegisterIteratorConfig(RegisterIteratorConfig r)
    {
        super(r);
        
        dependency = r.dependency;
        flags = r.flags;
        minNumber = r.minNumber;
        maxNumber = r.maxNumber;
    }
    
    /**
     * Checks if define-use dependencies are iterated.
     * 
     * @return <code>true<code> if define-use dependencies are iterated.
     */
    public boolean isDefineUse()
    {
        return (flags & RegisterIterator.DEFINE_USE) != 0;
    }
    
    /**
     * Checks if define-define dependencies are iterated.
     * 
     * @return <code>true<code> if define-define dependencies are iterated.
     */
    public boolean isDefineDefine()
    {
        return (flags & RegisterIterator.DEFINE_DEFINE) != 0;
    }
    
    /**
     * Checks if use-use dependencies are iterated.
     * 
     * @return <code>true<code> if use-use dependencies are iterated.
     */
    public boolean isUseUse()
    {
        return (flags & RegisterIterator.USE_USE) != 0;
    }
    
    /**
     * Checks if use-define dependencies are iterated.
     * 
     * @return <code>true<code> if se-define dependencies are iterated.
     */
    public boolean isUseDefine()
    {
        return (flags & RegisterIterator.USE_DEFINE) != 0;
    }
    
    /**
     * Enables/disables iteration of define-use dependencies.
     * 
     * @param <code>enable<code> the enabling status.
     */
    public void setDefineUse(boolean enable)
    {
        if(enable)
            { flags |= RegisterIterator.DEFINE_USE; }
        else
            { flags &= ~RegisterIterator.DEFINE_USE; }
    }
    
    /**
     * Enables/disables iteration of define-define dependencies.
     * 
     * @param <code>enable<code> the enabling status.
     */
    public void setDefineDefine(boolean enable)
    {
        if(enable)
            { flags |= RegisterIterator.DEFINE_DEFINE; }
        else
            { flags &= ~RegisterIterator.DEFINE_DEFINE; }
    }
    
    /**
     * Enables/disables iteration of use-use dependencies.
     * 
     * @param <code>enable<code> the enabling status.
     */
    public void setUseUse(boolean enable)
    {
        if(enable)
            { flags |= RegisterIterator.USE_USE; }
        else
            { flags &= ~RegisterIterator.USE_USE; }
    }
    
    /**
     * Enables/disables iteration of use-define dependencies.
     * 
     * @param <code>enable<code> the enabling status.
     */
    public void setUseDefine(boolean enable)
    {
        if(enable)
            { flags |= RegisterIterator.USE_DEFINE; }
        else
            { flags &= ~RegisterIterator.USE_DEFINE; }
    }
    
    /**
     * Returns the minimum number of dependencies.
     * 
     * @return the minimum number of dependencies.
     */
    public int getMinNumber()
    {
        return minNumber;
    }
    
    /**
     * Sets the minimum number of dependencies.
     * 
     * @param <code>minNumber</code> the minimum number of dependencies.
     */
    public void setMinNumber(int minNumber)
    {
        this.minNumber = minNumber;
    }

    /**
     * Returns the maximum number of dependencies.
     * 
     * @return the maximum number of dependencies.
     */
    public int getMaxNumber()
    {
        return maxNumber;
    }
    
    /**
     * Sets the maximum number of dependencies.
     * 
     * @param <code>maxNumber</code> the maximum number of dependencies.
     */
    public void setMaxNumber(int maxNumber)
    {
        this.maxNumber = maxNumber;
    }
    
    /**
     * Creates iterator of register dependencies according to the configuration.
     * 
     * @return the created iterator of register dependencies.
     */
    public abstract RegisterIterator createRegisterIterator();
    
    /**
     * Returns a copy of the configuration.
     * 
     * @return a copy of the configuration.
     */
    public abstract RegisterIteratorConfig clone();
}
