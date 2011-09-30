/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Dependency.java,v 1.7 2009/11/18 14:50:23 kamkin Exp $
 */

package com.unitesk.testfusion.core.dependency;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.iterator.Iterator;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Operand;
import com.unitesk.testfusion.core.situation.Situation;

/**
 * The abstract class that represents dependency between instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Dependency implements Iterator
{
    /** The constant indicates that a dependency is constructor-based. */
    public final static int CONSTRUCTOR = 0x0;
    
    /** The constant indicates that a dependency is constraint-based. */
    public final static int CONSTRAINT  = 0x1;
    
    /** Kind of the dependency (constructor or constraint). */
    protected int kind;
    
    /** Type of the dependency. */
    protected DependencyType type;

    /** Dependent operand. */
    protected Operand deps;
    
    /** Determinant operand. */
    protected Operand from;
    
    /**
     * Constructor.
     * 
     * @param <code>kind</code> the kind of the dependency.
     * 
     * @param <code>type</code> the type of the dependency.
     * 
     * @param <code>deps</code> the dependent operand.
     * 
     * @param <code>from</code> the determinant operand.
     */
    public Dependency(int kind, DependencyType type, Operand deps, Operand from)
    {
        this.kind = kind;
        this.type = type;
        this.deps = deps;
        this.from = from;
    }
    
    /**
     * Constructor.
     * 
     * @param <code>type</code> the type of the dependency.
     * 
     * @param <code>deps</code> the dependent operand.
     * 
     * @param <code>from</code> the determinant operand.
     */
    public Dependency(DependencyType type, Operand deps, Operand from)
    {
        this(CONSTRUCTOR, type, deps, from);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>type</code> the type of the dependency.
     * 
     * @param <code>from</code> the determinant operand.
     */
    public Dependency(DependencyType type, Operand from)
    {
        this(type, null, from);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>type</code> the type of the dependency.
     */
    public Dependency(DependencyType type)
    {
        this(type, null);
    }
        
    /**
     * Creates the dependency.
     * 
     * @param  <code>from</code> the determinant operand.
     * 
     * @return the created dependency.
     */
    protected abstract Dependency createDependency(Operand from);
    
    /**
     * Creates the dependency.
     * 
     * @param  <code>deps</code> the dependent operand.
     * 
     * @param  <code>from</code> the determinant operand.
     * 
     * @return the created dependency.
     */
    public Dependency createDependency(Operand deps, Operand from)
    {
        Dependency dep = createDependency(from);

        dep.deps = deps;
        dep.from = from;
        
        deps.registerForwardDependency(dep);
        from.registerBackwardDependency(dep);
        
        return dep;
    }
    
    /**
     * Returns the name of the dependency.
     *  
     * @return the name of the dependency.
     */
    public String getName()
    {
        return type.getName();
    }
    
    /**
     * Checks whether the dependency is constructor-based or not.
     *  
     * @return <code>true</code> if the dependency is constructor-based;
     *         <code>false</code> otherwise.
     */
    public boolean isConstructor()
    {
        return kind == CONSTRUCTOR;
    }

    /**
     * Checks whether the situation is constraint-based or not.
     *  
     * @return <code>true</code> if the situation is constraint-based;
     *         <code>false</code> otherwise.
     */
    public boolean isConstraint()
    {
        return kind == CONSTRUCTOR;
    }
    
    /**
     * Returns the type of the dependency.
     * 
     * @return the type of the dependency.
     */
    public DependencyType getDependencyType()
    {
        return type;
    }
    
    /**
     * Checks if this is the register dependency.
     * 
     * @return <code>true</code> if this is the register dependency;
     *         <code>false</code> otherwise.
     */
    public boolean isRegisterDependency()
    {
        return type.isRegisterDependency();
    }

    /**
     * Checks if this is the fixed dependency, i.e. the determinant and the
     * dependent operands are fixed.
     * 
     * @return <code>true</code> if this is the fixed dependency;
     *         <code>false</code> otherwise.
     */
    public boolean isFixed()
    {
        return deps != null && deps.isFixed() &&
               from != null && from.isFixed();
    }

    /**
     * Returns the determinant operand.
     * 
     * @return the determinant operand.
     */
    public Operand getDeterminantOperand()
    {
        return from;
    }
    
    /**
     * Returns the dependent operand.
     * 
     * @return the dependent operand.
     */
    public Operand getDependentOperand()
    {
        return deps;
    }
    
    /**
     * Returns the determinant instruction.
     * 
     * @return the determinant instruction.
     */
    public Instruction getDeterminantInstruction()
    {
        return from.getInstruction();
    }
    
    /**
     * Returns the dependent instruction.
     * 
     * @return the dependent instruction.
     */
    public Instruction getDependentInstruction()
    {
        return deps.getInstruction();
    }
    
    /**
     * Returns the determinant test situation.
     * 
     * @return the determinant test situation.
     */
    public Situation getDeterminantSituation()
    {
        return getDeterminantInstruction().getSituation();
    }

    /**
     * Returns the dependent test situation.
     * 
     * @return the dependent test situation.
     */
    public Situation getDependentSituation()
    {
        return getDependentInstruction().getSituation();
    }
    
    /**
     * Checks if the dependency is active or not.
     * 
     * @return <code>true</code> if the dependency is active; <code>false</code>
     *         otherwise.
     */
    public abstract boolean isActive();

    /**
     * Precondition of the <code>construct()</code> method.
     * 
     * @return <code>true</code> if the precondition is satisfied;
     *         <code>false</code> otherwise.
     */
    public abstract boolean precondition();

    /**
     * Postcondition of <code>construct()</code> method.
     * 
     * @return <code>true</code> if the postcondition is satisfied;
     *         <code>false</code> otherwise.
     */
    public abstract boolean postcondition();

    /**
     * Checks if the current dependency is consistent or not.
     *
     * @param  <code>operand</code> the operand.
     * 
     * @return <code>true</code> if the test situation is consistent;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isConsistent(Operand operand);

    /**
     * Constructs the current dependency.
     * 
     * @param <code>situation</code> the test situation.
     * 
     * @param <code>deps</code> the set of dependencies.
     * 
     * @param <code>context</code> the context of generation.
     */
    public abstract boolean construct(Situation situation, Dependencies deps, GeneratorContext context);

    /** Initializes the iterator of dependencies. */
    public abstract void init();
    
    /**
     * Checks if the iterator is not exhausted (the dependency is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public abstract boolean hasValue();

    /**
     * Returns the current dependency.
     * 
     * @return the current dependency.
     */
    public Dependency value()
    {
        return this;
    }
    
    /** Makes iteration. */
    public abstract void next();
    
    /** Stops the iterator. */
    public abstract void stop();
    
    /**
     * Returns a string representation of the dependency.
     * 
     * @return a string representation of the dependency.
     */
    public String toString()
    {
        return deps + "-" + from;
    }
    
    /**
     * Returns a copy of the dependency.
     * 
     * @param  <code>from</code> the determinant operand.
     * 
     * @return a copy of the dependency.
     */
    protected abstract Dependency clone(Operand from);
    
    /**
     * Returns a copy of the dependency.
     * 
     * @return a copy of the dependency.
     */
    public Dependency clone()
    {
        return clone(null);
    }
    
    /**
     * Return a copy of the dependency.
     * 
     * @param  <code>deps</code> the dependent operand.
     * 
     * @param  <code>from</code> the determinant operand.
     * 
     * @return a copy of the dependency.
     */
    public Dependency clone(Operand deps, Operand from)
    {
        Dependency dep = clone(from);

        dep.deps = deps;
        dep.from = from;
        
        if(deps != null)
            { deps.registerForwardDependency(dep); }
        
        if(from != null)
            { from.registerBackwardDependency(dep); }
        
        return dep;
    }
}
