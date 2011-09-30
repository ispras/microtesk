/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: DependencyListConfig.java,v 1.2 2008/12/09 12:37:30 kozlov Exp $
 */

package com.unitesk.testfusion.core.config;

/**
 * Class that represents list of dependency configurations.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class DependencyListConfig extends Config 
{
    /** List of dependency configurations. */
    protected ConfigList<DependencyConfig> deps = new ConfigList<DependencyConfig>(); 
    
    /**
     * Default constructor.
     */
    public DependencyListConfig() {}
    
    /**
     * Copy constructor.
     */
    public DependencyListConfig(DependencyListConfig config)
    {
        for (DependencyConfig dep : config.deps)
        {
            DependencyConfig cloneDep = dep.clone();
            cloneDep.setParent(this);
            this.deps.add(cloneDep);
        }
    }
    
    /**
     * Returns the number of dependencies in the options.
     * 
     * @return the number of dependencies in the options.
     */
    public int countDependency()
    {
        return deps.size();
    }
    
    /**
     * Returns the <code>i</code>-th dependency of the options.
     * 
     * @param  <code>i</code> the index of the dependency configuration.
     * 
     * @return the <code>i</code>-th dependency of the options.
     */
    public DependencyConfig getDependency(int i)
    {
        return deps.getConfig(i);
    }

    /**
     * Finds the dependency configuration with the given name.
     * 
     * @param  <code>name</code> the name of the dependency configuration to be
     *         found.
     * 
     * @return the dependency configuration with name <code>name</code> if it
     *         exists in the options; <code>null</code> otherwise.
     */
    public DependencyConfig getDependency(String name)
    {
        return deps.getConfig(name);
    }
    
    /**
     * Adds the dependency to the dependecy list.
     * 
     * @param <code>dependency</code> the dependency configuration to be added.
     */
    public void registerDependency(DependencyConfig dependency)
    {
        // Register dependency type.
        dependency.setParent(this);
        deps.add(dependency);
    }
    
    public DependencyListConfig clone()
    {
        return new DependencyListConfig(this); 
    }
    
    public String getFullName()
    {
        return parent.getFullName() + "." + "dependencies";
    }
}
