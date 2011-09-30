/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ConstraintDependency.java,v 1.4 2009/11/19 09:15:34 kamkin Exp $
 */

package com.unitesk.testfusion.core.dependency;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.model.Operand;
import com.unitesk.testfusion.core.situation.Situation;

/**
 * The abstract class that represents a dependency between instructions
 * (operands of instructions) desribed by means of constraints.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class ConstraintDependency extends Dependency
{
    /**
     * Constructor.
     * 
     * @param <code>type</code> the type of the dependency.
     * 
     * @param <code>deps</code> the dependent operand.
     * 
     * @param <code>from</code> the determinant operand.
     */
    public ConstraintDependency(DependencyType type, Operand deps, Operand from)
    {
        super(CONSTRAINT, type, deps, from);
    }

    /**
     * Constructor.
     * 
     * @param <code>type</code> the type of the dependency.
     * 
     * @param <code>from</code> the determinant operand.
     */
    public ConstraintDependency(DependencyType type, Operand from)
    {
        this(type, null, from);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>type</code> the type of the dependency.
     */
    public ConstraintDependency(DependencyType type)
    {
        this(type, null);
    }

    /**
     * Constructs the current dependency.
     * 
     * @param <code>situation</code> the test situation.
     * 
     * @param <code>deps</code> the set of dependencies.
     * 
     * @param <code>context</code> the context of generation.
     */
    public boolean construct(Situation situation, Dependencies deps, GeneratorContext context)
    {
        return true;
    }
}
