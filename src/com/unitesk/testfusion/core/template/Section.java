/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Section.java,v 1.21 2009/12/01 12:37:33 vorobyev Exp $
 */

package com.unitesk.testfusion.core.template;

import java.util.Collection;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.dependency.Dependencies;
import com.unitesk.testfusion.core.iterator.Iterator;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.core.situation.Situation;
import com.unitesk.testfusion.core.template.iterator.TemplateIterator;

/**
 * Class <code>Section</code> represents a section of test template, which is a
 * part of test template described by the <code>Template</code> class. Each
 * section has it own <code>TemplateIterator</code> object that defines a method
 * of instruction combining. Class <code>Section</code> implements the
 * <code>Iterator</code> iterface to iterate sequences of instructions (programs)
 * annotated by test situations and dependencies.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Section implements Iterator
{
    /** Name of template section. */
    protected String name;

    /** Processor. */
    protected Processor processor;
    
    /** Context of generation. */
    protected GeneratorContext context;
    
    /** Template iterator */
    protected TemplateIterator iterator;

    /** Current test template. */
    protected Program template;
    
    /** Iterator of test situations. */
    protected SectionSituationIterator situationIterator;

    /** Iterator of dependencies. */
    protected SectionDependencyIterator dependencyIterator = new SectionDependencyIterator();
    
    /** Flag that refrects availability of the program. */
    protected boolean hasValue;

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the section.
     * 
     * @param <code>iterator</code> the section iterator.
     */
    public Section(String name, TemplateIterator iterator)
    {
        this.name = name;
        this.iterator = iterator;
    }

    /**
     * Constructor.
     * 
     * @param <code>iterator</code> the section iterator
     */
    public Section(TemplateIterator iterator)
    {
        this(null, iterator);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to template section object.
     */
    protected Section(Section r)
    {
        name               = r.name;
        processor          = r.processor;
        context            = r.context;
        hasValue           = r.hasValue;
        
        if(r.iterator != null)
            { iterator = r.iterator.clone(); }
        
        if(r.template != null)
            { template = r.template.clone(); }
        
        if(r.situationIterator != null)
            { situationIterator  = r.situationIterator.clone(); }
        
        if(r.dependencyIterator != null)
            { dependencyIterator = r.dependencyIterator.clone(); }
    }

    /**
     * Returns processor.
     * 
     * @return processor.
     */
    public Processor getProcessor()
    {
        return processor;
    }
    
    /**
     * Sets processor.
     * 
     * @param <code>processor</code> the processor.
     */
    public void setProcessor(Processor processor)
    {
        this.processor = processor;
    }

    /**
     * Returns context of generation.
     * 
     * @return context of generation.
     */
    public GeneratorContext getContext()
    {
        return context;
    }
    
    /**
     * Sets context of generation.
     * 
     * @param <code>context</code> the context of generation.
     */
    public void setContext(GeneratorContext context)
    {
        this.context = context;
    }
    
    /**
     * Returns the section iterator.
     * 
     * @return the section iterator.
     */
    public TemplateIterator getTemplateIterator()
    {
        return iterator;
    }

    /**
     * Returns the instruction with the given name from the equivalence class.
     * 
     * @param <code>equivalenceClass</code> the name of equivalence class.
     * 
     * @param <code>name</code> the name of instruction.
     * 
     * @return the instruction with the given name from the equivalence class.
     */
    public Instruction getInstruction(String equivalenceClass, String name)
    {
        return iterator.getInstruction(equivalenceClass, name);
    }
    
    /**
     * Registers the instruction into the section.
     * 
     * @param <code>className</code> the equivalence class.
     * 
     * @param <code>instruction</code> the instruction.
     * 
     * @param <code>situation</code> the test situation.
     */
    public void registerInstruction(String className, Instruction instruction, Situation situation)
    {
        iterator.registerInstruction(className, instruction, situation);
    }      

    /**
     * Registers the instruction into the section.
     * 
     * @param <code>instruction</code> the instruction.
     * 
     * @param <code>situation</code> the test situation.
     */
    public void registerInstruction(Instruction instruction, Situation situation)
    {
        registerInstruction(instruction.getName(), instruction, situation);
    }

    /**
     * Registers the instruction into the section at the given position.
     * 
     * @param <code>i</code> the position of the instruction in the section.
     * 
     * @param <code>className</code> the equivalence class.
     *  
     * @param <code>instruction</code> the instruction.
     * 
     * @param <code>situation</code> the test situation.
     */
    public void registerInstruction(int i, String className, Instruction instruction, Situation situation)
    {
        iterator.registerInstruction(i, className, instruction, situation);
    }      

    /**
     * Registers the instruction into the section at the given position.
     * 
     * @param <code>positions</code> the positions of the instruction in the section.
     * 
     * @param <code>className</code> the equivalence class.
     *  
     * @param <code>instruction</code> the instruction.
     * 
     * @param <code>situation</code> the test situation.
     */
    public void registerInstruction(Collection<Integer> positions, String className, Instruction instruction, Situation situation)
    {
        iterator.registerInstruction(positions, className, instruction, situation);
    }      
    
    /**
     * Registers the instruction into the section at the given position.
     * 
     * @param <code>i</code> the position of the instruction in the section.
     * 
     * @param <code>instruction</code> the instruction.
     * 
     * @param <code>situation</code> the test situation.
     */
    public void registerInstruction(int i, Instruction instruction, Situation situation)
    {
        registerInstruction(i, instruction.getName(), instruction, situation);
    }

    /**
     * Registers the instruction into the section at the given positions.
     * 
     * @param <code>positions</code> the positions of the instruction in the section.
     * 
     * @param <code>instruction</code> the instruction.
     * 
     * @param <code>situation</code> the test situation.
     */
    public void registerInstruction(Collection<Integer> positions, Instruction instruction, Situation situation)
    {
        registerInstruction(positions, instruction.getName(), instruction, situation);
    }
    
    /**
     * Registers the section iterator (all instructions registered in it).
     * 
     * @param <code>templateIterator</code> the section iterator.
     */
    public void registerTemplateIterator(TemplateIterator templateIterator)
    {
        iterator.registerTemplateIterator(templateIterator);
    }

    /**
     * Registers the section iterator (all instructions registered in it).
     * 
     * @param <code>className</code> the equivalence class.
     * 
     * @param <code>templateIterator</code> the section iterator.
     */
    public void registerTemplateIterator(String className, TemplateIterator templateIterator)
    {
        iterator.registerTemplateIterator(className, templateIterator);
    }

    /**
     * Registers the section iterator (all instructions registered in it) at the
     * given position.
     * 
     * @param <code>i</code> the position of the instructions in the section.
     * 
     * @param <code>templateIterator</code> the section iterator.
     */
    public void registerTemplateIterator(int i, TemplateIterator templateIterator)
    {
        iterator.registerTemplateIterator(i, templateIterator);
    }

    /**
     * Registers the section iterator (all instructions registered in it) at the
     * given positions.
     * 
     * @param <code>positions</code> the positions of the instructions in the section.
     * 
     * @param <code>templateIterator</code> the section iterator.
     */
    public void registerTemplateIterator(Collection<Integer> positions, TemplateIterator templateIterator)
    {
        iterator.registerTemplateIterator(positions, templateIterator);
    }
    
    /**
     * Registers the section iterator (all instructions registered in it) at the
     * given position.
     * 
     * @param <code>i</code> the position of the instructions in the section.
     * 
     * @param <code>className</code> the equivalence class.
     * 
     * @param <code>templateIterator</code> the section iterator.
     */
    public void registerTemplateIterator(int i, String className, TemplateIterator templateIterator)
    {
        iterator.registerTemplateIterator(i, className, templateIterator);
    }

    /**
     * Registers the section iterator (all instructions registered in it) at the
     * given positions.
     * 
     * @param <code>positions</code> the positions of the instructions in the section.
     * 
     * @param <code>className</code> the equivalence class.
     * 
     * @param <code>templateIterator</code> the section iterator.
     */
    public void registerTemplateIterator(Collection<Integer> positions, String className, TemplateIterator templateIterator)
    {
        iterator.registerTemplateIterator(positions, className, templateIterator);
    }
    
    /**
     * Registers the section (all instructions registered in it).
     * 
     * @param <code>section</code> the section.
     */
    public void registerTemplate(Section section)
    {
        iterator.registerTemplateIterator(section.iterator);
    }
    
    /**
     * Registers the section (all instructions registered in it).
     * 
     * @param <code>className</code> the equivalence class.
     * 
     * @param <code>section</code> the section.
     */
    public void registerTemplate(String className, Section section)
    {
        iterator.registerTemplateIterator(className, section.iterator);
    }

    /**
     * Registers the section (all instructions registered in it) at the given
     * position.
     * 
     * @param <code>i</code> the position of the instructions in the section.
     * 
     * @param <code>section</code> the section.
     */
    public void registerTemplate(int i, Section section)
    {
        iterator.registerTemplateIterator(i, section.iterator);
    }

    /**
     * Registers the section (all instructions registered in it) at the given
     * positions.
     * 
     * @param <code>positions</code> the positions of the instructions in the section.
     * 
     * @param <code>section</code> the section.
     */
    public void registerTemplate(Collection<Integer> positions, Section section)
    {
        iterator.registerTemplateIterator(positions, section.iterator);
    }
    
    /**
     * Registers the section (all instructions registered in it) at the given
     * position.
     * 
     * @param <code>i</code> the position of the instructions in the section.
     * 
     * @param <code>className</code> the equivalence class.
     * 
     * @param <code>section</code> the section.
     */
    public void registerTemplate(int i, String className, Section section)
    {
        iterator.registerTemplateIterator(i, className, section.iterator);
    }

    /**
     * Registers the dependency iterator.
     * 
     * @param <code>iterator</code> the dependency iterator.
     */
    public void registerDependencyIterator(DependencyIterator iterator)
    {
        dependencyIterator.registerDependencyIterator(iterator);
    }    

    /** Clears all registered instructions and dependencies. */
    public void clear()
    {
        iterator.clear();
        dependencyIterator.clear();
    }
    
    /** Initialize the iterator of programs. */
    @Override
    public void init()
    {
    	iterator.init();
        hasValue = iterator.hasValue();
        
        while(hasValue())
        {
            template = iterator.value();
        
            if(iterator.construct(processor, context, template))
            {
                situationIterator = new SectionSituationIterator(template);
                situationIterator.init();
    
                dependencyIterator.process(template);
                dependencyIterator.initRegisterDependencies();
                dependencyIterator.initContentDependencies();
         
                return;
            }
            
            next();
        }
    }
    
    /**
     * Checks if the iterator is not exhausted (template is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean hasValue()
    {
        return hasValue;
    }
    
    /**
     * Returns the current program.
     * 
     * @return the current program.
     */
    @Override
    public Program value()
    {
        return template.clone();
    }

    /**
     * Returns the registered dependencies.
     * 
     * @return the registered dependencies.
     */
    public Dependencies getRegisteredDependencies()
    {
        return dependencyIterator.getRegisteredDependencies();
    }
    
    /**
     * Returns the dependencies presented in the current program.
     * 
     * @return the dependencies presented in the current program.
     */
    public Dependencies getDependencies()
    {
        return dependencyIterator.getDependencies();
    }

    /** Makes iteration. */
    @Override
    public void next()
    {
        if(!hasValue())
            { return; }
        
        // Iterate content dependencies.
        if(dependencyIterator.hasContentDependencies())
        {
            dependencyIterator.nextContentDependencies();
            if(dependencyIterator.hasContentDependencies())
                { return; }
        }
        
        dependencyIterator.initContentDependencies();

        // Iterate test situations.
        if(situationIterator.hasValue())
        {
            situationIterator.next();
            if(situationIterator.hasValue())
                { return; }
        }
        
        situationIterator.init();

        // Iterate register dependencies.
        if(dependencyIterator.hasRegisterDependencies())
        {
            dependencyIterator.nextRegisterDependencies();
            if(dependencyIterator.hasRegisterDependencies())
                { return; }
        }
        
        dependencyIterator.initRegisterDependencies();

        // Iterate test templates.
        while(iterator.hasValue())
        {
            iterator.next();
                
            if(iterator.hasValue())
            {
                template = iterator.value();
                
                if(iterator.construct(processor, context, template))
                {
                    situationIterator = new SectionSituationIterator(template);
                    situationIterator.init();
        
                    dependencyIterator.process(template);
                    dependencyIterator.initRegisterDependencies();
                    dependencyIterator.initContentDependencies();
                    
                    return;
                }
            }
        }
        
        stop();
    }

    /** Uses auxiliary registers. */
    public void useRegisters()
    {
        iterator.useRegisters(processor, context, template);
    }
    
    /** Stops the iterator of programs. */
    @Override
    public void stop()
    {
        hasValue = false;
    }

    /**
     * Returns a copy of the section.
     * 
     * @return a copy of the section.
     */
    @Override
    public Section clone()
    {
        return new Section(this);
    }
}
