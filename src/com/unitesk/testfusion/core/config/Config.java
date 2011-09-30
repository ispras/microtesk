/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Config.java,v 1.29 2009/07/08 08:25:55 kamkin Exp $
 */

package com.unitesk.testfusion.core.config;

import com.unitesk.testfusion.core.util.Utils;

/**
 * Abstract class that represents MicroTESK configuration or its part.
 * Configuration has hierarchical structure. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Config
{
    /** Default name of the configuration. */
    public static final String DEFAULT_CONFIGURATION_NAME = "<Unknown>"; 
    
    /** Name of the configuration. */
    protected String name;
    
    /** Parent of the configuration. */
    protected Config parent;
   
    /**
     * Basic constructor.
     * 
     * @param <code>name</code> the name of the configuration.
     */
    public Config(String name)
    {
        this.name = name;
    }
    
    /** Default constructor. */
    public Config()
    {
        this(DEFAULT_CONFIGURATION_NAME);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to configuration object to be copied.
     */
    protected Config(Config r)
    {
        name   = r.name;
        parent = r.parent;
    }
    
    /**
     * Returns the name of the configuration.
     *
     * @return the name of the configuration.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Checks if the configuration is initialized.
     * 
     * @return <code>true</code> if the configuration is initialized;
     *         <code>false</code> otherwise.
     */
    public boolean isDefined()
    {
        return !isUndefined();
    }
    
    /**
     * Checks if the configuration is not initialized.
     * 
     * @return <code>true</code> if the configuration is not initialized;
     *         <code>false</code> otherwise.
     */
    public boolean isUndefined()
    {
        return Utils.isNullOrEmpty(name) || name.equals(DEFAULT_CONFIGURATION_NAME);
    }
    
    /**
     * Sets the name of the configuration.
     * 
     * @param <code>name</code> the new name of the configuration.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * Returns the full name of the configuration.
     * By default a full name of a configuration coincides with its local name.
     * 
     * @return the full name of the configuration.
     */
    public String getFullName()
    {
        return getName();
    }
    
    /**
     * Returns the parent of the configuration.
     * 
     * @return the parent of the configuration.
     */
    public Config getParent()
    {
        return parent;
    }
    
    /**
     * Checks if the configuration is the root of the hierarchy.
     * 
     * @return if the configuration is root of the hierarchy, then
     *         the method returns true; otherwise it returns false.
     */
    public boolean isRoot()
    {
        return parent == null;
    }
    
    /**
     * Sets the parent of the configuration.
     *
     * @param <code>parent</code> the new parent of the configuration. 
     */
    protected void setParent(Config parent)
    {
        this.parent = parent;
    }
    
    /**
     * Checks if configuration <code>config</code> is an ancestor of this
     * configuration.
     *
     * @param  <code>config</code> the configuration to be checked.
     * 
     * @return <code>true</code> if configuration <code>config</code> is
     *         ancestor of this configuration; <code>false</code> otherwise.
     */
    public boolean isAncestor(Config config)
    {
        if(isRoot())
            { return false; }
        
        if(config == parent)
            { return true; }
        
        return parent.isAncestor(config);
    }
    
    /**
     * Checks if configuration <code>config</code> is a descendant of this
     * configuration.
     *
     * @param  <code>config</code> the configuration to be checked.
     * 
     * @return <code>true</code> if configuration <code>config</code> is
     *         descentant of this configuration; <code>false</code> otherwise.
     */
    public boolean isDescendant(Config config)
    {
        return config.isAncestor(this);
    }
    
    /**
     * Returns a closest common ancestor of configuration <code>config</code>
     * and this configuration. It is assumed that a common ancestor exists.
     * 
     * @param  <code>config</code> the configuration to be used for finding closest
     *         common ancestor with this configuration.
     *         
     * @return closest common ancestor of configuration <code>config</code> and
     *         this configuration.
     */
    public Config getClosestCommonAncestor(Config config)
    {
        if(config == this || isDescendant(config))
            { return this; }
        
        if(isAncestor(config))
            { return config; }
        
        return getClosestCommonAncestor(config.getParent());
    }
    
    /**
     * Returns the nesting level of the configuration in the hierarchy.
     * Root configuration has level zero.
     * 
     * @return the nesting level of the configuration.
     */
    public int getNestingLevel()
    {
        if(isRoot())
            { return 0; }
        
        return parent.getNestingLevel() + 1;
    }
    
    /**
     * Returns a string representation of the configuration.
     * 
     * @return a string representation of the configuration.
     */
    public String toString()
    {
        return getName();
    }
    
    /**
     * Checks if the configration is empty, i.e. it does not contain useful
     * information. By default configuration is not empty.
     * 
     * @return <code>true</code> if the configuration is empty;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        return false;
    }
    
    /**
     * Returns a copy of the configuration.
     *
     * @return a copy of the configuration.
     */
    public abstract Config clone();
    
    /**
     * Returns a copy of the configuration.
     *
     * @param  <code>cloneParent</code> the new clone parent.
     * 
     * @return a copy of the configuration.
     */
    public Config clone(Config cloneParent)
    {
        Config clone = clone();
        
        clone.setParent(cloneParent);
        
        return clone;
    }
}
