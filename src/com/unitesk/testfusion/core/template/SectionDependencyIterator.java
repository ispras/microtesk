/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SectionDependencyIterator.java,v 1.6 2010/01/23 13:42:06 vorobyev Exp $
 */

package com.unitesk.testfusion.core.template;

import java.util.ArrayList;

import com.unitesk.testfusion.core.dependency.Dependencies;
import com.unitesk.testfusion.core.iterator.Iterator;
import com.unitesk.testfusion.core.model.Program;

/**
 * Iterator of test template dependencies. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SectionDependencyIterator implements Iterator
{
    /** Test template. */
    protected Program template;

    /** Registered iterators of dependencies. */
    protected ArrayList<DependencyIterator> iterators = new ArrayList<DependencyIterator>();
    
    /** Flags that indicates if the iterator of dependencies is not exhaused. */
    protected boolean hasValue;
    
    /**
     * Flags that indicates if the iterator of register dependencies is not
     * exhaused.
     */
    protected boolean hasRegisterDependencies = true;

    /**
     * Flags that indicates if the iterator of content dependencies is not
     * exhaused.
     */
    protected boolean hasContentDependencies = true;
    
    /** Default constructor. */
    public SectionDependencyIterator() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to dependency iterator object.
     */
    protected SectionDependencyIterator(SectionDependencyIterator r)
    {
    	for(int i = 0; i < iterators.size(); ++i)
    		{ iterators.add(r.iterators.get(i).clone() ); }

        template = r.template.clone();
        hasValue = r.hasValue;
        hasRegisterDependencies = r.hasRegisterDependencies;
        hasContentDependencies = r.hasContentDependencies;
    }
    
    /**
     * Registers the dependency iterator.
     * 
     * @param <code>iterator</code> the dependency iterator.
     */
    public void registerDependencyIterator(DependencyIterator iterator)
    {
        iterators.add(iterator);
    }
    
    /**
     * Registers the dependency iterators.
     * 
     * @param <code>iterators</code> the dependency iterators.
     */
    public void registerDependencyIterators(ArrayList<DependencyIterator> iterators)
    {
        this.iterators.addAll(iterators);
    }
    
    /** Removes registered dependency iterators. */
    public void clear()
    {
        iterators.clear();
    }
    
    /**
     * Builds the set of dependency instances to be iterated.
     * 
     * @param <code>template</code> the test template.
     */
    public void process(Program template)
    {
        int i, size;
        
        this.template = template;
        
        size = iterators.size();
        for(i = 0; i < size; i++)
        {
            DependencyIterator iterator = iterators.get(i);
            
            iterator.process(template);
        }
    }

    /** Initializes the iterator of dependencies. */
    public void init()
    {
        int i, size;

        size = iterators.size();
        for(i = 0; i < size; i++)
        {
            DependencyIterator iterator = iterators.get(i);
            
            iterator.init();
        }
        
        hasValue = true;
    }
    
    /**
     * Checks if the iterator of dependencies is not exhausted.
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasValue()
    {
        return hasValue();
    }
    
    /**
     * Returns the test template.
     * 
     * @return the test template.
     */
    public Program value()
    {
        return template;
    }

    /**
     * Returns the registered dependencies.
     * 
     * @return the registered dependencies.
     */
    public Dependencies getRegisteredDependencies()
    {
        int i, size;
        
        Dependencies deps = new Dependencies();
        
        size = iterators.size();
        for(i = 0; i < size; i++)
        {
            DependencyIterator iterator = iterators.get(i);
            
            deps.add(iterator.getDependency());
        }
        
        return deps;
    }
    
    /**
     * Returns the dependencies presented in the current section.
     * 
     * @return the dependencies presented in the current section.
     */
    public Dependencies getDependencies()
    {
    	int i, size;
        
        Dependencies deps = new Dependencies();
        
        size = iterators.size();
        for(i = 0; i < size; i++)
        {
            DependencyIterator iterator = iterators.get(i);
            deps.add(iterator.getDependencies());
        }
        
        return deps;
    }
    
    /** Makes iteration of dependencies. */
    public void next()
    {
        int i, size;
        
        if(!hasValue)
            { return; }
        
        size = iterators.size();
        for(i = size - 1; i >= 0; i--)
        {
            DependencyIterator iterator = iterators.get(i);
            
            if(iterator.hasValue())
            {
                iterator.next();
                
                if(iterator.hasValue())
                    { return; }
            }
            
            iterator.init();
        }
        
        hasValue = false;
    }
    
    /** Stops the iterator of dependencies. */
    public void stop()
    {
        hasValue = false;
    }

    /** Initializes the iterator of register dependencies. */
    public void initRegisterDependencies()
    {
        int i, size;
        
        size = iterators.size();
        for(i = 0; i < size; i++)
        {
            DependencyIterator iterator = iterators.get(i);
            
            if(iterator.isRegisterDependency())
                { iterator.init(); }
        }
        
        hasRegisterDependencies = true;
    }
    
    /**
     * Checks if the iterator of register dependencies is not exhausted.
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasRegisterDependencies()
    {
        return hasRegisterDependencies;
    }
    
    /** Makes iteration of register dependencies. */
    public void nextRegisterDependencies()
    {
        int i, size;
        
        if(!hasRegisterDependencies)
            { return; }
        
        size = iterators.size();
        for(i = size - 1; i >= 0; i--)
        {
            DependencyIterator iterator = iterators.get(i);
            
            if(!iterator.isRegisterDependency())
                { continue; }
            
            if(iterator.hasValue())
            {
                iterator.next();
                
                if(iterator.hasValue())
                    { return; }
            }
            
            iterator.init();
        }
        
        hasRegisterDependencies = false;
    }
    
    /** Initializes the iterator of content dependencies. */
    public void initContentDependencies()
    {
        int i, size;
        
        size = iterators.size();
        for(i = 0; i < size; i++)
        {
            DependencyIterator iterator = iterators.get(i);
            
            if(!iterator.isRegisterDependency())
                { iterator.init(); }
        }
        
        hasContentDependencies = true;
    }

    /**
     * Checks if the iterator of content dependencies is not exhausted.
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasContentDependencies()
    {
        return hasContentDependencies;
    }
    
    /** Makes iteration of content dependencies. */
    public void nextContentDependencies()
    {
        int i, size;
        
        if(!hasContentDependencies)
            { return; }
        
        size = iterators.size();
        for(i = size - 1; i >= 0; i--)
        {
            DependencyIterator iterator = iterators.get(i);
            
            if(iterator.isRegisterDependency())
                { continue; }
            
            if(iterator.hasValue())
            {
                iterator.next();
                
                if(iterator.hasValue())
                    { return; }
            }
            
            iterator.init();
        }
        
        hasContentDependencies = false;
    }
    
    /** 
     * Returns a copy of the dependency iterator.
     * 
     * @return a copy of the dependency iterator.
     */
    public SectionDependencyIterator clone()
    {
        return new SectionDependencyIterator(this);
    }
}
