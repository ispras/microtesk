/* 
 * Copyright (c) 2007-2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Dependencies.java,v 1.4 2009/06/10 16:24:07 kamkin Exp $
 */

package com.unitesk.testfusion.core.dependency;

import java.util.ArrayList;
import java.util.HashSet;

import com.unitesk.testfusion.core.util.Utils;

/**
 * Class represents a set of dependencies.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Dependencies extends ArrayList<Dependency>
{
    public static final long serialVersionUID = 0;

    /** Default constructor. */
    public Dependencies() {}

    /**
     * Adds the dependencies to the set.
     * 
     * @param <code>deps</code> the dependencies to be added.
     */
    public void add(Dependencies deps)
    {
        addAll(deps);
    }
    
    /**
     * Checks if the given dependency is in the set.
     *
     * @param  <code>type</code> the dependency type.
     * 
     * @return <code>true</code> if the dependency is in the set;
     *         <code>false</code> otherwise.
     */
    public boolean exists(Dependency dependency)
    {
        for(Dependency dep: this)
        {
            if(dep.getDependencyType().equals(dependency))
                { return true; }
        }
        
        return false;
    }
    
    /**
     * Checks if there is dependency of given type in the set.
     *
     * @param  <code>type</code> the dependency type.
     * 
     * @return <code>true</code> if dependency exists in the set;
     *         <code>false</code> otherwise.
     */
    public boolean exists(DependencyType type)
    {
        for(Dependency dep: this)
        {
            if(dep.getDependencyType().equals(type))
                { return true; }
        }
        
        return false;
    }
    
    /**
     * Returns the set of unique the dependencies.
     * 
     * @return the set of the unique dependencies.
     */
    public Dependencies uniques()
    {
        int i, size;
        Dependencies uniques = new Dependencies();
        
        HashSet<String> types = new HashSet<String>();
        
        size = size();
        for(i = 0; i < size; i++)
        {
            Dependency dep = get(i);
            DependencyType type = dep.getDependencyType();
            
            if(!types.contains(type.toString()))
            {
                types.add(type.toString());
                uniques.add(dep);
            }
        }
        
        return uniques;
    }
    
    /**
     * Returns a string representation of the dependencies.
     * 
     * @return a string representation of the dependencies.
     */
    public String toString()
    {
        int i, size, flag;
        
        StringBuffer buffer = new StringBuffer();
        
        size = size();
        
        for(i = flag = 0; i < size; i++)
        {
            String dep = get(i).toString();
            
            if(!Utils.isNullOrEmpty(dep))
                { buffer.append(dep); flag = 1; break; }
        }
        
        for(++i; i < size; i++)
        {
            String dep = get(i).toString();
            
            if(!Utils.isNullOrEmpty(dep))
            {
                buffer.append(", ");
                buffer.append(dep);
            }
        }
        
        return flag != 0 ? buffer.toString() : "<None>";
    }
    
    /**
     * Returns a copy of the dependencies.
     * 
     * @return a copy of the dependencies.
     */
    public Dependencies clone()
    {
        Dependencies deps = new Dependencies();
        
        for(Dependency dep : this)
            { deps.add(dep.clone()); }
        
        return deps;
    }
}
