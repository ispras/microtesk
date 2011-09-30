/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigList.java,v 1.9 2009/07/08 08:25:55 kamkin Exp $
 */

package com.unitesk.testfusion.core.config;

import java.util.ArrayList;

/**
 * Homogeneous list of MicroTESK configuration parts.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ConfigList<T extends Config> extends ArrayList<T>
{
    public static final long serialVersionUID = 0;
    
    /**
     * Returns the index of the configuration in the list.
     * 
     * @return the index of the configuration in the list if it exists;
     *         <code>-1</code>otherwise.
     */
    public int getIndex(Config config)
    {
        int i, size;
        
        size = size();
        for(i = 0; i < size; i++)
        {
            if(getConfig(i) == config)
                { return i; }
        }
            
        return -1;
    }
    
    /**
     * Returns the <code>i</code>-th item of the configuration list.
     * 
     * @param  <code>i</code> the index of the configuration.
     * 
     * @return the <code>i</code>-th item of the configuration list.
     */
    public T getConfig(int i)
    {
        return get(i);
    }
    
    /**
     * Finds the configuration with the given name.
     * 
     * @param  <code>name</code> the name of the configuration to be found.
     * 
     * @return the configuration with name <code>name</code> if such
     *         configuration exists in the list; <code>null</code> otherwise.
     */
    public T getConfig(String name)
    {
        int i, size;
        
        size = size();
        for(i = 0; i < size; i++)
        {
            T config = getConfig(i);
            
            if(config.getName().equals(name))
                { return config; }
        }
        
        return null;
    }

    /**
     * Adds the item to the configuration list.
     * 
     * @param <code>config</code> the configuration to be added.
     */
    public void addConfig(T config)
    {
        add(config);
    }
    
    /**
     * Adds the item to the configuration list at the specified position.
     * 
     * @param <code>i</code> the position of the configuration.
     * 
     * @param <code>config</code> the configuration to be added.
     */
    public void addConfig(int i, T config)
    {
        add(i, config);
    }
    
    /**
     * Removes the <code>i</code>-th item of the configuration list.
     * 
     * @param <code>i</code> the index of the configuration.
     */
    public void removeConfig(int i)
    {
        remove(i);
    }
    
    /**
     * Removes the configuration with the given name.
     * 
     * @param <code>name</code> the name of the configuration.
     */
    public void removeConfig(String name)
    {
        int i, size;
        
        size = size();
        for(i = 0; i < size; i++)
        {
            T config = getConfig(i);
            
            if(config.getName().equals(name))
                { removeConfig(i); return; }
        }
    }
    
    /**
     * Removes the configuration from the configuration list.
     * 
     * @param <code>config</code> the configuration to be removed.
     */
    public void removeConfig(T config)
    {
        remove(config);
    }

    /**
     * Returns a copy of the configuration list.
     * 
     * @return a copy of the configuration list.
     */
    @SuppressWarnings("unchecked")
    public ConfigList<T> clone()
    {
        ConfigList<T> cloneList = new ConfigList<T>();
        
        for(T item: this)
        {
            T cloneItem = (T)item.clone();
            
            cloneList.add(cloneItem);
        }
        
        return cloneList;
    }
    
    /**
     * Returns a copy of the configuration list.
     * 
     * @param  <code>cloneParent</code> the new parent of the copy.
     * 
     * @return a copy of the configuration list.
     */
    @SuppressWarnings("unchecked")
    public ConfigList<T> clone(Config cloneParent)
    {
        ConfigList<T> cloneList = new ConfigList<T>();
        
        for(T item: this)
        {
            T cloneItem = (T)item.clone(cloneParent);

            cloneList.add(cloneItem);
        }
        
        return cloneList;
    }
}
