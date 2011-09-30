/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: CrossDependencyConfig.java,v 1.7 2008/12/13 12:04:15 kozlov Exp $
 */

package com.unitesk.testfusion.core.config;

/**
 * Configuration of a cross dependency. This configuration defines 
 * dependencies, which are registered in the test configuration, for 
 * pair of section configurations.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class CrossDependencyConfig extends Config
{
    /** Section. */
    protected SectionConfig dependsOn;
    
    /** List of dependency configurations. */
    protected DependencyListConfig deps;
    
    /** Default constructor. */
    public CrossDependencyConfig() {}

    public CrossDependencyConfig(SectionConfig dependOn)
    {
        this.dependsOn = dependOn;
        
        this.deps = new DependencyListConfig();
        this.deps.setParent(this);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>firstSection</code>
     * 
     * @param <code>secondSection</code>
     */
    public CrossDependencyConfig(SectionConfig dependsOn, 
            DependencyListConfig deps)
    {
        this(dependsOn);
        
        this.deps = deps;
        this.deps.setParent(this);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>config</code> the reference to the cross dependency 
     *        configuration.
     */
    public CrossDependencyConfig(CrossDependencyConfig config)
    {
        this.dependsOn = config.dependsOn;

        this.deps = config.deps.clone();
        this.deps.setParent(this);
    }
    
    /**
     * Adds the dependencies to the options.
     * 
     * @param <code>deps</code> the dependencies to be added.
     */
    public void setDependencies(DependencyListConfig deps)
    {
        this.deps = deps;
    }
    
    /**
     * Returns the list of dependency configurations.
     * 
     * @return the list of dependency configurations.
     */
    public DependencyListConfig getDependencies()
    {
        return deps;
    }
    
    /**
     * Returns the depends on section.
     *  
     * @return the depends on section.
     */
    public SectionConfig getDependsOnSection()
    {
        return dependsOn;
    }
    
    public CrossDependencyConfig clone() 
    {   
        return new CrossDependencyConfig(this);
    }
}
