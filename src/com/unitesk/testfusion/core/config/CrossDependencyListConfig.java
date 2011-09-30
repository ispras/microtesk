/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: CrossDependencyListConfig.java,v 1.7 2008/12/18 14:01:19 kozlov Exp $
 */

package com.unitesk.testfusion.core.config;

import java.util.ArrayList;

import com.unitesk.testfusion.gui.event.ConfigChangedEvent;
import com.unitesk.testfusion.gui.event.ConfigChangedListener;

/**
 * Set of cross dependency configurations.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class CrossDependencyListConfig extends Config
{
    /** List of listeners. */
    private ArrayList<ConfigChangedListener> listeners = 
        new ArrayList<ConfigChangedListener>();
    
    private ConfigChangedEvent event = new ConfigChangedEvent(this);
    
    /** Clear copy of dependency list configuration. */
    protected DependencyListConfig clearCopyOfDeps;
    
    /** Cross dependecy list configuration. */
    protected ConfigList<CrossDependencyConfig> crossDeps = 
        new ConfigList<CrossDependencyConfig>();
    
    /**
     * Constructor.
     * 
     * @param <code>deps</code> list of dependency configuration for creating
     *        new cross dependency configurations.
     */
    public CrossDependencyListConfig(DependencyListConfig deps)
    {
        clearCopyOfDeps = deps;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>crossDeps</code> the reference to the cross dependency
     *        list configuration. 
     */
    public CrossDependencyListConfig(CrossDependencyListConfig config)
    {
        clearCopyOfDeps = config.clearCopyOfDeps.clone();
        
        crossDeps = config.crossDeps.clone();
    }
    
    /**
     * Removes cross dependency configuration if such cross dependecy exists. 
     * 
     * @param <code>dependsOn</code> the depend on section of cross dependecy
     *        for removing.
     */
    public void removeCrossDependency(SectionConfig dependsOn)
    {
        CrossDependencyConfig cross = getCrossDependency(dependsOn); 
        
        if (cross != null)
            { crossDeps.removeConfig(cross); }
        
        fireAddElementToConfig(event);
    }
    
    /**
     * Removes the specified cross dependency configuration from list.
     * 
     * @param <code>cross</code> the cross dependency configuration.
     */
    public void removeCrossDependency(CrossDependencyConfig cross)
    {
        crossDeps.removeConfig(cross);
        
        fireAddElementToConfig(event);
    }
    
    /**
     * Adds new cross dependecy configuration, if there isn't cross dependency
     * with specified sections.
     *   
     * @param <code>first</code> the depends on section of new cross dependecy.
     *  
     * @retrun the configuration with specified parameters if it exists, or new 
     *         created configuration otherwise.   
     */
    public CrossDependencyConfig addCrossDependency(SectionConfig dependsOn)
    {
        CrossDependencyConfig cross = getCrossDependency(dependsOn);
        
        if (cross == null)
        {
            CrossDependencyConfig newCross = new CrossDependencyConfig(
                    dependsOn, clearCopyOfDeps.clone());
            
            crossDeps.add(newCross);
            
            fireAddElementToConfig(event);
            
            return newCross;
        }
        else
            { return cross; }
    }
    
    /**
     * Returns cross dependency configuration with specified sections.
     * 
     * @param <code>dependsOn</code> the depends on section.
     * 
     * @return cross dependency configuration with specified sections if such
     *         cross dependecy exists or <code>null<code> otherwise.
     */
    public CrossDependencyConfig getCrossDependency(SectionConfig dependsOn)
    {
        for (CrossDependencyConfig cross : crossDeps) 
        {
            if (cross.getDependsOnSection() == dependsOn) 
                return cross;
        }
        
        return null;
    }
    
    /**
     * Returns if list contains cross dependency configuration with specified
     * sections.
     * 
     * @param <code>dependsOn</code> the depends on section.
     * 
     * @return <code>true</code> if list contains cross dependency configuration 
     *         with specified sections, or <code>false</code> otherwise. 
     */
    public boolean isContainCrossDependency(SectionConfig dependsOn)
    {
        return getCrossDependency(dependsOn) != null;
    }
    
    /**
     * Returns the position of specified cross dependency configuration in 
     * the list.
     * 
     * @param <code>config</code> the cross dependency configuration.
     * 
     * @return the position of specified cross dependency configuration in 
     *         the list, or <code>-1</code> if there isn't such configuration
     *         in the list.
     */
    public int getCrossDependencyIndex(CrossDependencyConfig config)
    {
        for (int i = 0; i < crossDeps.size(); i++)
        {
            if (config == crossDeps.getConfig(i))
                return i;
        }
        
        return -1;
    }
    
    /**
     * Returns cross dependency configuration with specified number.
     * 
     * @param <code>n</code> the number of configuration.
     * 
     * @return cross dependency configuration with specified number.
     */
    public CrossDependencyConfig getCrossDependency(int n)
    {
        return crossDeps.getConfig(n);
    }
    
    /**
     * Returns the number of cross dependencies in the configuration.
     * 
     * @return the number of cross dependencies in the configuration.
     */
    public int countCrossDependencies()
    {
        return crossDeps.size();
    }
    
    /**
     * Set clear copy of dependency list configuration.
     * 
     * @param <code>deps</code> the dependency list configuration.
     */
    public void setClearCopyOfDeps(DependencyListConfig deps)
    {
        this.clearCopyOfDeps = deps;
    }
    
    public CrossDependencyListConfig clone()
    {
        return new CrossDependencyListConfig(this);
    }
    
    /**
     * Adds the specified listener to the list of listeners.
     * 
     * @param <code>l</code> the add element to configuration listener.
     */
    public void addAddElementToConfigListener(ConfigChangedListener l)
    {
        listeners.add(l);
    }
    
    /**
     * Removes the specified listener from the list of listeners.
     * 
     * @param <code>l</code> the add element to configuration listener.
     */
    public void removeAddElementToConfigListener(ConfigChangedListener l)
    {
        listeners.remove(l);
    }
    
    /**
     * Fires the add element to configuration event to all listeners.
     * 
     * @param <code>event</code> the add element to configuration event.
     */
    protected void fireAddElementToConfig(ConfigChangedEvent event)
    {
        for (ConfigChangedListener listener : listeners)
            { listener.configChanged(event); }
    }
}
