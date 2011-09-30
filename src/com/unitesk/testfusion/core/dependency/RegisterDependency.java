/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: RegisterDependency.java,v 1.32 2009/08/19 16:50:45 kamkin Exp $
 */

package com.unitesk.testfusion.core.dependency;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.iterator.BooleanIterator;
import com.unitesk.testfusion.core.model.ContentType;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Operand;
import com.unitesk.testfusion.core.model.OperandType;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.register.Register;
import com.unitesk.testfusion.core.situation.RegisterSituation;
import com.unitesk.testfusion.core.situation.Situation;

/**
 * Register dependency between two instructions. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RegisterDependency extends Dependency
{
    /** Iterator of the dependency. */
    protected BooleanIterator iterator = new BooleanIterator();
    
    /** Processor. */
    protected Processor processor;
    
    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>from</code> the determinant operand.
     */
    public RegisterDependency(Processor processor, Operand from)
    {
        super(new RegisterDependencyType(from.getOperandType()), from);
        
        this.processor = processor;
    }
    
    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>type</code> the operand type.
     */
    public RegisterDependency(Processor processor, OperandType type)
    {
        super(new RegisterDependencyType(type), null);
        
        this.processor = processor;
    }

    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>type</code> the operand type.
     * 
     * @param <code>contentType</code> the content type
     */
    public RegisterDependency(Processor processor, OperandType type, ContentType contentType)
    {
        super(new RegisterDependencyType(type, contentType), null);
        
        this.processor = processor;
    }
    
    protected RegisterDependency(RegisterDependency r, Operand from)
    {
        super(r.type.clone(), from);
        
        this.processor = r.processor;
        this.iterator  = r.iterator.clone();
    }
    
    /**
     * Creates an instance of the dependency.
     * 
     * @param  <code>from</code> the determinant operand.
     * 
     * @return an instance of the dependency.
     */
    @Override
    protected Dependency createDependency(Operand from)
    {
        return new RegisterDependency(processor, from);
    }
    
    /**
     * Checks if the dependency is active.
     * 
     * @return <code>true</code> if the dependency is active; <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean isActive()
    {
        // If dependency is fixed, then it is active.
        return iterator.booleanValue() || isFixed();
    }
    
    /**
     * Precondition of the <code>construct()</code> method.
     * 
     * @return <code>true</code>.
     */
    @Override
    public boolean precondition()
    {
        return true;
    }

    /**
     * Postcondition of <code>construct()</code> method.
     * 
     * @return <code>true</code> if the postcondition is satisfied;
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean postcondition()
    {
        if(deps != null && from != null)
        {
            Register registerDeps = deps.getRegister();
            Register registerFrom = from.getRegister();
    
            return isActive() == registerDeps.equals(registerFrom);
        }
        
        return true;
    }

    /**
     * Checks if there is at least one active forward dependency
     * 
     * @param <code>operand</code> operand to be checked.
     * 
     * @return <code>true</code> if check is successful; <code>false</code>
     *         otherwise.
     */
    protected boolean checkActiveDependencies(Operand operand)
    {
        int i, size;

        size = operand.countForwardDependency();
        for(i = 0; i < size; i++)
        {
            Dependency dependency = operand.getForwardDependency(i);
            
            if(!(dependency instanceof RegisterDependency))
                { continue; }
            
            if(dependency.isActive())
                { return true; }
        }
        
        return false;
    }
    
    /**
     * Checks that there is at most one active forward dependency.
     * 
     * @param <code>operand</code> operand to be checked.
     * 
     * @return <code>true</code> if check is successful; <code>false</code>
     *         otherwise.
     */
    protected boolean checkIrredundancy(Operand operand)
    {
        int i, j, size;

        size = operand.countForwardDependency();
        for(i = 0; i < size; i++)
        {
            Dependency dependency = operand.getForwardDependency(i);
            
            if(!(dependency instanceof RegisterDependency))
                { continue; }
            
            RegisterDependency dependency1 = (RegisterDependency)dependency;
            
            if(!dependency1.isActive())
                { continue; }

            if(dependency1.isFixed())
                { continue; }
            
            // Active forward dependency is found.

            Operand fromOperand = dependency1.getDeterminantOperand();
            
            // Active transitive dependency is found.
            if(fromOperand != null && checkActiveDependencies(fromOperand))
                { return false; }
            
            for(j = 0; j < size; j++)
            {
                if(i == j)
                    { continue; }

                dependency = operand.getForwardDependency(j);
                
                if(!(dependency instanceof RegisterDependency))
                    { continue; }
                
                RegisterDependency dependency2 = (RegisterDependency)dependency;
                
                // Set of dependencies is not consistent:
                // second active forward dependency is found.
                if(dependency2.isActive())
                    { return false; }
            }
        }

        return true;
    }

    /**
     * Checks that there are no redefinitons of the register.
     * 
     * @param <code>operand</code> operand to be checked.
     * 
     * @return <code>true</code> if check is successful; <code>false</code>
     *         otherwise.
     */
    protected boolean checkRedefinion(Operand operand)
    {
        int i, j, size;

        size = operand.countBackwardDependency();
        for(i = 0; i < size; i++)
        {
            Dependency dependency = operand.getBackwardDependency(i);
            
            if(!(dependency instanceof RegisterDependency))
                { continue; }

            RegisterDependency dependency1 = (RegisterDependency)dependency;
            
            if(!dependency1.isActive())
                { continue; }

            Operand operand1 = dependency.getDependentOperand();
            ContentType content1 = operand1.getContentType();
            Instruction instruction1 = operand1.getInstruction();
            
            // Skip dependency, if operand is not output.
            if(!operand1.isOutput())
                { continue; }
            
            // Potential redefinion is found.
            
            for(j = 0; j < size; j++)
            {
                if(i == j)
                    { continue; }

                dependency = operand.getBackwardDependency(j);
                
                if(!(dependency instanceof RegisterDependency))
                    { continue; }
                
                RegisterDependency dependency2 = (RegisterDependency)dependency;
                
                if(dependency2.isActive())
                {
                    Operand operand2 = dependency2.getDependentOperand();
                    ContentType content2 = operand2.getContentType();
                    Instruction instruction2 = operand2.getInstruction();
                    
                    if(operand2.isInput())
                    {
                        // Redefinition of the kind "redefine-use" is found.
                        if(instruction2.getPosition() > instruction1.getPosition())
                        {
                            // Redefined content type can not be converted to the used type.
                            if(!content1.isCompatibleTo(content2))
                                { return false; }
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Checks that there are no two dependent operands such that their types
     * are two incompatible sub-types of the determinant operand's type.
     * 
     * @param <code>operand</code> operand to be checked.
     * 
     * @return <code>true</code> if check is successful; <code>false</code>
     *         otherwise.
     */
    protected boolean checkSubtypes(Operand operand)
    {
        int i, j, size;

        OperandType operandType = operand.getOperandType();
        
        size = operand.countBackwardDependency();
        for(i = 0; i < size; i++)
        {
            Dependency dependency1 = operand.getBackwardDependency(i);
            
            if(!dependency1.isRegisterDependency() || !dependency1.isActive())
                { continue; }

            Operand operand1 = dependency1.getDependentOperand();
            OperandType operandType1 = operand1.getOperandType();
            
            if(operandType1.isBlockConsistingOf(operandType) || !operandType1.isProperSubtypeOf(operandType))
                { continue; }

            for(j = 0; j < size; j++)
            {
                if(i == j)
                    { continue; }

                Dependency dependency2 = operand.getBackwardDependency(j);
                
                if(!dependency2.isRegisterDependency() || !dependency2.isActive())
                    { continue; }
                
                Operand operand2 = dependency2.getDependentOperand();
                OperandType operandType2 = operand2.getOperandType();
                
                if(!operandType2.isProperSubtypeOf(operandType))
                    { continue; }
                
                // Skip dependencies, if operands types are not compatible.
                if(!operandType1.isSubtypeOf(operandType2) && !operandType2.isSubtypeOf(operandType1))
                    { return false; }
            }
        }

        return true;
    }
    
    /**
     * Checks if the current dependency is consistent or not.
     *
     * @param  <code>operand</code> the operand.
     * 
     * @return <code>true</code> if the test situation is consistent;
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean isConsistent(Operand operand)
    {
        // Checks that there is at most one active forward dependency and
        // there are no transitive dependencies.
        if(!checkIrredundancy(operand))
            { return false; }

        // Checks that there are no two dependent operands such that their types
        // are two incompatible sub-types of the determinant operand's type.
        if(!checkSubtypes(operand))
            { return false; }

        // Checks that there are no inconsistent redefinions of the type "redefine-use".
        if(!checkRedefinion(operand))
            { return false; }

        return true;
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
    @Override
    public boolean construct(Situation situation, Dependencies forwardDependencies, GeneratorContext context)
    {
        int i, size;
        
        RegisterSituation registerSituation = (RegisterSituation)situation;
        
        size = forwardDependencies.size();
        for(i = 0; i < size; i++)
        {
            Dependency dependency = forwardDependencies.get(i);
            
            if(!dependency.isRegisterDependency())
                { continue; }
            
            RegisterDependency registerDependency = (RegisterDependency)dependency;
            
            if(registerDependency.isActive())
            {
                Operand operandFrom = registerDependency.getDeterminantOperand();
                Register registerFrom = operandFrom.getRegister();
                
                return registerSituation.construct(registerFrom, context);
            }
        }
        
        return false;
    }

    private OperandType getSmallestSubtype(Operand operand)
    {
        int i, size;

        OperandType operandType = operand.getOperandType();
        
        size = operand.countBackwardDependency();
        for(i = 0; i < size; i++)
        {
            Dependency dependency = operand.getBackwardDependency(i);
            
            if(!dependency.isRegisterDependency() || !dependency.isActive())
                { continue; }
            
            Operand dependentOperand = dependency.getDependentOperand();
            OperandType dependentOperandType = dependentOperand.getOperandType();
            
            // Block sub-types are skiped, to have ability to construct
            // implicit dependencies of the "register-block" type.
            if(dependentOperandType.isSubtypeOf(operandType) && !dependentOperandType.isBlock())
                { operandType = dependentOperandType; }
        }
        
        return operandType;
    }
    
    /**
     * Constructs the register of the operand.
     * 
     * @param <code>operand</code> the operand to be initialized by a register.
     * 
     * @param <code>context</code> the context of generation.
     */
    public void construct(Operand operand, GeneratorContext context)
    {
        if(!type.isApplicableTo(operand))
            { return; }
        
        RegisterSituation registerSituation = new RegisterSituation(operand.getOperandType());
        
        boolean isConstructed = construct(registerSituation, operand.getForwardDependencies(), context);
        
        if(!isConstructed)
            { registerSituation.construct(getSmallestSubtype(operand), context); }
        
        Register register = registerSituation.getRegister();

        operand.setRegister(register);
        context.useRegister(register);
    }

    /** Initializes the iterator. */
    @Override
    public void init()
    {
        iterator.init();
    }

    /**
     * Returns the current dependency.
     * 
     * @return the current dependency.
     */
    @Override
    public Dependency value()
    {
        return this;
    }

    /**
     * Checks if the iterator is not exhausted (the dependency is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean hasValue()
    {
        return iterator.hasValue();
    }

    /** Makes iteration. */
    @Override
    public void next()
    {
        iterator.next();
        
        // If dependency is fixed, then we skip redundant value.
        if(isFixed())
            { iterator.next(); }
    }
    
    /** Stop the iterator. */
    @Override
    public void stop()
    {
        iterator.stop();
    }
    
    /**
     * Return a string representation of the dependency.
     * 
     * @return a string representation of the dependency.
     */
    @Override
    public String toString()
    {
        return isActive() ? super.toString() : "";
    }
    
    /**
     * Returns a copy of the dependency.
     * 
     * @param  <code>from</code> the determinant operand.
     * 
     * @return a copy of the dependency.
     */
    @Override
    protected Dependency clone(Operand from)
    {
        return new RegisterDependency(this, from);
    }
}
