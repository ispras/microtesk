/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Template.java,v 1.15 2009/08/19 16:50:51 kamkin Exp $
 */

package com.unitesk.testfusion.core.template;

import java.util.ArrayList;

import com.unitesk.testfusion.core.dependency.Dependencies;
import com.unitesk.testfusion.core.iterator.Iterator;
import com.unitesk.testfusion.core.iterator.ProductIterator;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.core.situation.EmptySituation;
import com.unitesk.testfusion.core.situation.Situation;
import com.unitesk.testfusion.core.template.iterator.ProductTemplateIterator;
import com.unitesk.testfusion.core.util.IntegerPairMap;

/**
 * Class <code>Template</code> represents a test template, which specifies an
 * order of instructions in a test actions. Test template consists of one or
 * more template sections described by class <code>Section</code>. Each section
 * has it own <code>TemplateIterator</code> object that defines a method of
 * instruction combining. Test template can be used as a section in an other
 * test template. Like a section, test template implement the
 * <code>Iterator</code> iterface to iterate sequences of instructions
 * (programs) annotated by test situations and dependencies.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Template extends Section implements Iterator
{
    /** Product iterator for combining programs of template sections. */
    protected ProductIterator iterator = new ProductIterator();

    /** Cross-section dependencies. */
    protected IntegerPairMap<ArrayList<DependencyIterator>> crossDependencies = new IntegerPairMap<ArrayList<DependencyIterator>>();
    
    /** Iterator of cross-section dependencies. */
    protected ProductIterator crossDependencyIterator = new ProductIterator();
    
    /** Flag that refrects availability of the program. */
    protected boolean hasValue;
    
    /** Default constructor. */
    public Template()
    {
        this("");
    }
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the template.
     */
    public Template(String name)
    {
        super(name, null);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to template object.
     */
    protected Template(Template r)
    {
        super(r);
        
        iterator = r.iterator.clone();
        crossDependencies = r.crossDependencies.clone();
        crossDependencyIterator = r.crossDependencyIterator.clone();
        hasValue = r.hasValue;
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

        size = iterator.size();
        for(i = 0; i < size; i++)
        {
            Section section = (Section)iterator.iterator(i);
            
            deps.add(section.getRegisteredDependencies());
        }

        return deps.uniques();
    }
    
    /**
     * Adds the section to the template.
     * 
     * @param <code>section</code> the section to be added.
     */
    public void registerSection(Section section)
    {
        iterator.registerIterator(section);
    }
    
    /**
     * Adds the single-instruction section to the template.
     * 
     * @param <code>instruction</code> the instruction.
     * 
     * @param <code>situation</code> the test situation.
     */
    public void registerInstruction(Instruction instruction, Situation situation)
    {
        Section section = new Section(new ProductTemplateIterator(1));
        
        section.registerInstruction(instruction, situation);

        registerSection(section);
    }
    
    /**
     * Adds the single-instruction section to the template.
     * 
     * @param <code>instruction</code> the instruction.
     */
    public void registerInstruction(Instruction instruction)
    {
        registerInstruction(instruction, new EmptySituation());
    }

    /**
     * Register the global dependency, which is defined for all instructions
     * of the template.  
     * 
     * @param <code>dependency</code> the global dependency.
     */
    public void registerGlobalDependencyIterator(DependencyIterator dependency)
    {
        registerDependencyIterator(dependency);
    }

    /**
     * Register the cross-section dependency between the given sections.
     * 
     * @param <code>i</code> the index of determinant section.
     * 
     * @param <code>j</code> the index of dependent section.
     * 
     * @param <code>iterator</code> the cross-section dependency iterator.
     */
    public void registerCrossDependency(int i, int j, DependencyIterator iterator)
    {
        if(i >= j)
            { throw new IllegalArgumentException("Incorrect order of sections"); }
        
        ArrayList<DependencyIterator> iterators = crossDependencies.get(i, j);
        
        if(iterators == null)
            { iterators = new ArrayList<DependencyIterator>(); }
        
        iterators.add(iterator);
        
        crossDependencies.put(i, j, iterators);
    }
    
    /**
     * Initializes global dependencies.
     * 
     * @param <code>template</code> current template.
     */
    protected void initGlobalDependencies()
    {
        dependencyIterator.process(value());
        dependencyIterator.initRegisterDependencies();
        dependencyIterator.initContentDependencies();
    }
    
    /**
     * Initializes cross-section dependencies.
     */
    protected void initCrossDependencies()
    {
        int i, j, size = iterator.size();

        int[] start = new int[size];
        int[] end   = new int[size];

        for(i = j = 0; i < size; i++)
        {
            Program section = (Program)iterator.value(i);
            
            start[i] = j;
            j = start[i] + section.countInstruction();
            end[i] = j - 1;
        }

        Program template = value();
        crossDependencyIterator = new ProductIterator();
        
        for(i = 0; i < size - 1; i++)
        for(j = i + 1; j < size; j++)
        {
            ArrayList<DependencyIterator> iterators = crossDependencies.get(i, j);
            
            if(iterators != null)
            {
                CrossDependencyIterator iterator = new CrossDependencyIterator(start[i], end[i], start[j], start[j]);
                
                iterator.registerDependencyIterators(iterators);
                
                iterator.process(template);
                crossDependencyIterator.registerIterator(iterator);
            }
        }
    }
    
    /** Initializes the iterator of programs. */
    public void init()
    {
        iterator.init();
        
        if(hasValue = iterator.hasValue())
        {
            initGlobalDependencies();
            initCrossDependencies();
        }
    }
    
    /** Stops the iterator of programs. */
    public void stop()
    {
        iterator.stop();

        hasValue = false;
    }
    
    /**
     * Checks if the iterator is not exhausted (template is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasValue()
    {
        return hasValue;
    }
    
    /**
     * Returns the current program.
     * 
     * @return the current program.
     */
    public Program value()
    {
        int i, size;
        
        Program template = new Program();
        
        size = iterator.size();
        for(i = 0; i < size; i++)
            { template.append((Program)iterator.value(i)); }
        
        return template;
    }
    
    /**
     * Returns the dependencies presented in the current program.
     * 
     * @return the dependencies presented in the current program.
     */
    public Dependencies getDependencies()
    {
        int i, size;
        Dependencies deps = new Dependencies();

        size = iterator.size();
        for(i = 0; i < size; i++)
        {
            Section section = (Section)iterator.iterator(i);
            
            deps.add(section.getDependencies());
        }
        
        return deps;
    }
    
    /** Makes iteration. */
    public void next()
    {
        if(!hasValue)
            { return; }
    
        // Iterate cross-dependencies.
        if(crossDependencyIterator.hasValue())
        {
            crossDependencyIterator.next();
            
            if(crossDependencyIterator.hasValue())
                { return; }
        }
        
        crossDependencyIterator.init();
        
        // Iterate global dependencies.
        if(dependencyIterator.hasContentDependencies())
        {
            dependencyIterator.nextContentDependencies();
            
            if(dependencyIterator.hasContentDependencies())
                { return; }
        }
        
        dependencyIterator.initContentDependencies();

        if(dependencyIterator.hasRegisterDependencies())
        {
            dependencyIterator.nextRegisterDependencies();

            if(dependencyIterator.hasRegisterDependencies())
                { return; }
        }
        
        dependencyIterator.initRegisterDependencies();

        // Iterate templates, test situations and dependencies.
        if(iterator.hasValue())
        {
            iterator.next();
            
            if(iterator.hasValue())
            {
                initGlobalDependencies();
                initCrossDependencies();
                
                return;
            }
        }
    
        hasValue = false;
    }
    
    /** Uses auxiliary registers. */
    public void useRegisters()
    {
        int i, size;
        
        size = iterator.size();
        for(i = 0; i < size; i++)
        {
            Section section = (Section)iterator.iterator(i);
            
            section.useRegisters();
        }
    }
    
    /**
     * Returns a copy of the template.
     * 
     * @return a copy of the template.
     */
    public Template clone()
    {
        return new Template(this);
    }
}
