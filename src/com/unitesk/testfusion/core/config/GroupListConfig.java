/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: GroupListConfig.java,v 1.2 2008/08/15 07:20:02 kamkin Exp $
 */

package com.unitesk.testfusion.core.config;

/**
 * Abstract class that represents list of group configurations.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class GroupListConfig extends SelectionConfig
{
    /** List of group configurations. */
    protected ConfigList<GroupConfig> groups = new ConfigList<GroupConfig>();
    
    /** Default constructor. */
    public GroupListConfig()
    {
        super();
    }
    
    /**
     * Basic constructor.
     * 
     * @param <code>name</code> the name of the configuration.
     */
    public GroupListConfig(String name)
    {
        super(name);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to configuration object. 
     */
    protected GroupListConfig(GroupListConfig r)
    {
        super(r);
        
        groups = r.groups.clone(this);
    }
    
    /**
     * Returns the number of groups in the configuration.
     * 
     * @return the number of groups in the configuration.
     */
    public int countGroup()
    {
        return groups.size();
    }
    
    /**
     * Returns the <code>i</code>-th group of the configuration.
     * 
     * @param  <code>i</code> the index of the configuration.
     * 
     * @return the <code>i</code>-th group of the configuration.
     */
    public GroupConfig getGroup(int i)
    {
        return groups.getConfig(i);
    }
    
    /**
     * Finds the group configuration with the given name.
     * 
     * @param  <code>name</code> the name of the group configuration to be
     *         found.
     * 
     * @return the group configuration with name <code>name</code> if it
     *         exists in the configuration; <code>null</code> otherwise.
     */
    public GroupConfig getGroup(String name)
    {
        return groups.getConfig(name);
    }

    /**
     * Adds the group to the configuration.
     * 
     * @param <code>group</code> the group configuration to be added.
     */
    public void registerGroup(GroupConfig group)
    {
        group.setParent(this);
        groups.addConfig(group);
    }

    /**
     * Checks if the configration is empty, i.e. it does not contain test
     * situations.
     * 
     * @return <code>true</code> if the configuration is empty;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty()
    {
        int i, size;
        
        size = countGroup();
        for(i = 0; i < size; i++)
        {
            GroupConfig group = getGroup(i);

            if(!group.isEmpty())
                { return false; }
        }
        
        return true;
    }
}
