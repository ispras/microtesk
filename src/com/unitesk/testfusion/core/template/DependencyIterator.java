/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: DependencyIterator.java,v 1.14 2009/06/10 13:46:49 kamkin Exp $
 */

package com.unitesk.testfusion.core.template;

import java.util.ArrayList;
import java.util.HashMap;

import com.unitesk.testfusion.core.dependency.Dependencies;
import com.unitesk.testfusion.core.dependency.Dependency;
import com.unitesk.testfusion.core.dependency.DependencyType;
import com.unitesk.testfusion.core.iterator.Iterator;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Operand;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.core.situation.Situation;

/**
 * Iterator of test template dependencies. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class DependencyIterator implements Iterator
{
    /** Dependency. */
    protected Dependency dependency;
    
    /** Instances of dependency to be iterated. */
    protected Dependencies dependencies = new Dependencies();
    
    /** List of determinant operands. */
    protected ArrayList<Operand> determinantOperands = new ArrayList<Operand>();
    
    /** List of dependent operands. */
    protected ArrayList<Operand> dependentOperands = new ArrayList<Operand>();
    
    /** Map of operands to dependencies. */
    protected HashMap<Operand, Dependencies> operandDependencies = new HashMap<Operand, Dependencies>();
    
    /** Test template. */
    protected Program template;
    
    /** Flags that indicates if the iterator of dependencies is not exhaused. */
    protected boolean hasValue;
    
    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the dependency.
     */
    public DependencyIterator(Dependency dependency)
    {
        this.dependency = dependency;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to register iterator.
     */
    protected DependencyIterator(DependencyIterator r)
    {
        dependency = r.dependency;
        dependencies = r.dependencies;
        determinantOperands = r.determinantOperands;
        dependentOperands = r.dependentOperands;
        operandDependencies = r.operandDependencies;
        template = r.template;
        hasValue = r.hasValue;
    }

    /**
     * Returns the dependency.
     * 
     * @return the dependency.
     */
    public Dependency getDependency()
    {
        return dependency;
    }
    
    /**
     * Indicates if the dependency is register dependency.
     * 
     * @return <code>true</code> if the dependency is register dependency;
     *         <code>false</code> otherwise.
     */
    public boolean isRegisterDependency()
    {
        return dependency.isRegisterDependency();
    }

    /** Clears dependencies. */
    public void clear()
    {
        dependencies.clear();

        determinantOperands.clear();
        dependentOperands.clear();
    }

    /**
     * Processes the pair of instructions. If one of the instructions has null
     * situation, the instructions can not be dependent.
     * 
     * @param <code>deps</code> the dependent instruction.
     * 
     * @param <code>from</code> the determinant instruction.
     */
    protected void processInstructionPair(Instruction instructionDeps, Instruction instructionFrom)
    {
        int i, j, size1, size2;

        Situation situationDeps = instructionDeps.getSituation();
        Situation situationFrom = instructionFrom.getSituation();
        
        // Do not process special instructions
        if(situationDeps == null || situationFrom == null)
            { return; }

        size1 = instructionDeps.countOperand();
        size2 = instructionFrom.countOperand();
        
        for(i = 0; i < size1; i++)
        for(j = 0; j < size2; j++)
        {
            Operand operandDeps = instructionDeps.getOperand(i);
            Operand operandFrom = instructionFrom.getOperand(j);
            
            DependencyType type = dependency.getDependencyType();
            
            if(type.isApplicableTo(operandDeps, operandFrom))
            {
                Dependency instance = dependency.createDependency(operandDeps, operandFrom); 
                
                dependencies.add(instance);
                
                Dependencies operandDepsDependencies = operandDependencies.get(operandDeps);
                
                if(operandDepsDependencies == null)
                    { operandDepsDependencies = new Dependencies(); }
                
                operandDepsDependencies.add(instance);
                
                operandDependencies.put(operandDeps, operandDepsDependencies);
                
                if(!determinantOperands.contains(operandFrom))
                    { determinantOperands.add(operandFrom); }
                
                if(!dependentOperands.contains(operandDeps))
                    { dependentOperands.add(operandDeps); }
            }
        }
    }
    
    /**
     * Processes test template (pre-initialization of the dependency iterator).
     * 
     * @param <code>template</code> the test template.
     */
    public void process(Program template)
    {
        this.template = template;

        clear();
        
        for(int i = 0; i < template.countInstruction() - 1; i++)
        for(int j = i + 1; j < template.countInstruction(); j++)
        {
            Instruction instructionDeps = template.getInstruction(j);
            Instruction instructionFrom = template.getInstruction(i);

            processInstructionPair(instructionDeps, instructionFrom);
        }
    }

    /**
     * Processes test template (pre-initialization of the cross-section
     * dependency iterator).
     * 
     * @param <code>template</code> the test template.
     * 
     * @param <code>start1</code> the start position of the determinant section.
     * 
     * @param <code>end1</code> the end position of the determinant section.
     * 
     * @param <code>start2</code> the start position of the dependent section.
     * 
     * @param <code>end2</code> the end position of the dependent section.
     */
    public void process(Program template, int start1, int end1, int start2, int end2)
    {
        this.template = template;
        
        clear();
        
        for(int i = start1; i <= end1; i++)
        for(int j = start2; j <= end2; j++)
        {
            Instruction instructionDeps = template.getInstruction(j);
            Instruction instructionFrom = template.getInstruction(i);

            processInstructionPair(instructionDeps, instructionFrom);
        }
    }
    
    /** Initializes the iterator of dependencies. */
    public void init()
    {
        hasValue = true;
        for(int i = 0; i < dependencies.size(); i++)
        {
            Dependency dep = dependencies.get(i);
            
            dep.init();
        }
        
        while(hasValue() && !isConsistent())
            {  step(); }
    }
    
    /**
     * Checks if the iterator of dependencies is not exhausted.
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasValue()
    {
        return hasValue;
    }
    
    /**
     * Returns the dependencies.
     * 
     * @return the dependencies.
     */
    public Dependencies value()
    {
        return getDependencies();
    }
    
    /**
     * Returns the dependencies.
     * 
     * @return the dependencies.
     */
    public Dependencies getDependencies()
    {
        return dependencies;
    }

    /**
     * Returns the dependencies for the given operand.
     * 
     * @return the dependencies for the given operand.
     */
    protected Dependencies getDependencies(Operand operand)
    {
        return operandDependencies.get(operand);
    }
    
    /**
     * Returns the list of determinant operands.
     * 
     * @return the list of determinant operands.
     */
    protected ArrayList<Operand> getDeterminantOperands()
    {
        return determinantOperands;
    }
    
    /**
     * Returns the list of dependent operands.
     * 
     * @return the list of dependency operands.
     */
    protected ArrayList<Operand> getDependentOperands()
    {
        return dependentOperands;
    }
        
    /**
     * Checks if the instances of the dependencies are consistent or
     * not.
     * 
     * @return <code>true</code> if the instances are consistent;
     *         <code>false</code> otherwise.
     */
    protected boolean isConsistent()
    {
        int i, j, size1, size2;
        
        size1 = template.countInstruction();
        for(i = 0; i < size1; i++)
        {
            Instruction instruction = template.getInstruction(i);
            
            size2 = instruction.countOperand();
            for(j = 0; j < size2; j++)
            {
                Operand operand = instruction.getOperand(j);
                
                if(!dependency.isConsistent(operand))
                    { return false; }
            }
        }
        
        return true;
    }
    
    private void step()
    {
        if(!hasValue)
            { return; }
        
        for(int i = dependencies.size() - 1; i >= 0; i--)
        {
            Dependency dep = dependencies.get(i);
            
            if(dep.precondition() && dep.hasValue())
            {
                dep.next();
                
                if(dep.hasValue())
                    { return; }
            }
            
            dep.init();
        }
        
        hasValue = false;
    }
    
    /** Makes iteration. */
    public void next()
    {
        do { step(); } while(hasValue() && !isConsistent());
    }
    
    /** Stops the iterator of dependencies. */
    public void stop()
    {
        hasValue = false;
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public abstract DependencyIterator clone();
}
