/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: RegisterIterator.java,v 1.8 2009/08/06 10:27:41 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.register;

import com.unitesk.testfusion.core.dependency.DependencyType;
import com.unitesk.testfusion.core.dependency.RegisterDependency;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Operand;
import com.unitesk.testfusion.core.model.OperandType;
import com.unitesk.testfusion.core.template.DependencyIterator;

/**
 * Iterator of register dependencies between instructions of test template.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class RegisterIterator extends DependencyIterator
{
    /** None dependencies. */
    public static final int NONE = 0x0000;
    
    /** Define-use dependencies only. */
    public static final int DEFINE_USE = 0x0001;
    
    /** Define-define dependencies only. */
    public static final int DEFINE_DEFINE = 0x0002;
    
    /** Use-use dependencies only. */
    public static final int USE_USE = 0x0004;
    
    /** Use-define dependencies only. */
    public static final int USE_DEFINE = 0x0008;
    
    /** All kind of dependencies. */
    public static final int ALL = DEFINE_USE | DEFINE_DEFINE | USE_USE | USE_DEFINE;
    
    /** Types of dependencies to be iterated. */
    protected int flags;
    
    /** Minimum number of dependencies. */
    protected int minNumber = 0;
    
    /** Maximum number of dependencies. */
    protected int maxNumber = Integer.MAX_VALUE;
    
    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the register dependency.
     */
    public RegisterIterator(RegisterDependency dependency)
    {
        super(dependency);
    }

    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the register dependency.
     * 
     * @param <code>flags</code> the types of dependencies.
     * 
     * @param <code>minNumber</code> the minimum number of dependencies.
     * 
     * @param <code>maxNumber</code> the maximum number of dependencies.
     */
    public RegisterIterator(RegisterDependency dependency, int flags, int minNumber, int maxNumber)
    {
        this(dependency);
        
        this.flags = flags;
        
        this.minNumber = minNumber;
        this.maxNumber = maxNumber;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to register iterator.
     */
    protected RegisterIterator(RegisterIterator r)
    {
        super(r);

        flags = r.flags;
        
        minNumber = r.minNumber;
        maxNumber = r.maxNumber;
    }

    /**
     * Checks if define-use dependencies are iterated.
     * 
     * @return <code>true<code> if define-use dependencies are iterated.
     */
    public boolean isDefineUse()
    {
        return (flags & DEFINE_USE) != 0;
    }
    
    /**
     * Checks if define-define dependencies are iterated.
     * 
     * @return <code>true<code> if define-use dependencies are iterated.
     */
    public boolean isDefineDefine()
    {
        return (flags & DEFINE_DEFINE) != 0;
    }    

    /**
     * Checks if use-use dependencies are iterated.
     * 
     * @return <code>true<code> if use-use dependencies are iterated.
     */
    public boolean isUseUse()
    {
        return (flags & USE_USE) != 0;
    }
    
    /**
     * Checks if use-define dependencies are iterated.
     * 
     * @return <code>true<code> if use-define dependencies are iterated.
     */
    public boolean isUseDefine()
    {
        return (flags & USE_DEFINE) != 0;
    }
    
    /**
     * Enables/disables iteration of define-use dependencies.
     * 
     * @param <code>enable<code> the enabling status.
     */
    public void setDefineUse(boolean enable)
    {
        if(enable)
            { flags |= DEFINE_USE; }
        else
            { flags &= ~DEFINE_USE; }
    }
    
    /**
     * Enables/disables iteration of define-define dependencies.
     * 
     * @param <code>enable<code> the enabling status.
     */
    public void setDefineDefine(boolean enable)
    {
        if(enable)
            { flags |= DEFINE_DEFINE; }
        else
            { flags &= ~DEFINE_DEFINE; }
    }

    /**
     * Enables/disables iteration of use-use dependencies.
     * 
     * @param <code>enable<code> the enabling status.
     */
    public void setUseUse(boolean enable)
    {
        if(enable)
            { flags |= USE_USE; }
        else
            { flags &= ~USE_USE; }
    }

    /**
     * Enables/disables iteration of use-define dependencies.
     * 
     * @param <code>enable<code> the enabling status.
     */
    public void setUseDefine(boolean enable)
    {
        if(enable)
            { flags |= USE_DEFINE; }
        else
            { flags &= ~USE_DEFINE; }
    }
    
    /**
     * Returns the minimum number of dependencies.
     * 
     * @return the minimum number of dependencies.
     */
    public int getMinNumber()
    {
        return minNumber;
    }
    
    /**
     * Sets the minimum number of dependencies.
     * 
     * @param <code>minNumber</code> the minimum number of dependencies.
     */
    public void setMinNumber(int minNumber)
    {
        this.minNumber = minNumber;
    }

    /**
     * Returns the maximum number of dependencies.
     * 
     * @return the maximum number of dependencies.
     */
    public int getMaxNumber()
    {
        return minNumber;
    }
    
    /**
     * Sets the maximum number of dependencies.
     * 
     * @param <code>maxNumber</code> the maximum number of dependencies.
     */
    public void setMaxNumber(int maxNumber)
    {
        this.maxNumber = maxNumber;
    }
    
    /**
     * Checks if the instances of the register dependencies are consistent or
     * not.
     * 
     * @return <code>true</code> if the instances are consistent;
     *         <code>false</code> otherwise.
     */
    protected boolean isConsistent()
    {
        int i, j, size1, size2;

        // Number of this-type register dependencies.
        int number; 
        
        DependencyType dependencyType = dependency.getDependencyType();
        OperandType dependencyOperandType = dependencyType.getOperandType();
        
        size1 = template.countInstruction();
        for(i = number = 0; i < size1; i++)
        {
            Instruction instruction = template.getInstruction(i);
            
            size2 = instruction.countOperand();
            for(j = 0; j < size2; j++)
            {
                Operand operand = instruction.getOperand(j);
                OperandType operandType = operand.getOperandType();
                
                // Dependencies of the given operand are not consistent.
                if(!dependency.isConsistent(operand))
                    { return false; }
                
                // Consider dependencies of this type only.
                if(!operandType.isSubtypeOf(dependencyOperandType))
                    { continue; }
        
                // If there is an active register dependency, take it into account.
                if(operand.isRegisterDependent())
                {
                    if(++number > maxNumber)
                        { return false; }
                }
            }
        }
        
        return number >= minNumber;
    }
    
    /** Makes iteration of dependencies. */
    public abstract void next();
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public abstract RegisterIterator clone();
}
