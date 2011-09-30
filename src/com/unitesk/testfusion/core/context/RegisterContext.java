/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: RegisterContext.java,v 1.6 2009/08/19 16:50:39 kamkin Exp $
 */

package com.unitesk.testfusion.core.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.unitesk.testfusion.core.model.IllegalOperandTypeException;
import com.unitesk.testfusion.core.model.OperandType;
import com.unitesk.testfusion.core.model.ResetableObject;
import com.unitesk.testfusion.core.model.register.Register;
import com.unitesk.testfusion.core.model.register.RegisterBlock;

/**
 * Register context contains information on registers.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RegisterContext implements ResetableObject
{
    private static final long serialVersionUID = 1L;

    /** Strategy of register allocation: may return used register. */
    public static final int MAY_RETURN_USED_REGISTER = 0;

    /** Strategy of register allocation: should not return used register. */
    public static final int SHOULD_NOT_RETURN_USED_REGISTER = 1;
    
    /** Default strategy of register allocation. */
    public static final int DEFAULT_ALLOCATION_STRATEGY = SHOULD_NOT_RETURN_USED_REGISTER;

    /** Maps register type to strategy of register allocation. */
    protected HashMap<OperandType, Integer> allocationStrategies = new HashMap<OperandType, Integer>();
    
    /** Maps register type to register set. */
    protected HashMap<OperandType, RegisterSet> allRegisters = new HashMap<OperandType, RegisterSet>();
    
    /** Maps register type to set of available registers. */
    protected HashMap<OperandType, RegisterSet> freeRegisters = new HashMap<OperandType, RegisterSet>();
    
    /** Maps registers to values. */
    protected HashMap<Register, Long> definedRegisters = new HashMap<Register, Long>();
    
    /** Maps register type to its immediate children. */
    protected HashMap<OperandType, HashSet<OperandType>> subtypes = new HashMap<OperandType, HashSet<OperandType>>();

    /** Default register type. */
    protected OperandType defaultOperandType = OperandType.REGISTER;
    
    /** Default constructor. */
    public RegisterContext() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> reference to an register context object.
     */
    protected RegisterContext(RegisterContext r)
    {
        allocationStrategies = new HashMap<OperandType, Integer>();
        for(Entry<OperandType, Integer> entry : r.allocationStrategies.entrySet())
            { allocationStrategies.put(entry.getKey(), entry.getValue()); }
        
        allRegisters = new HashMap<OperandType, RegisterSet>();
        for(Entry<OperandType, RegisterSet> entry : r.allRegisters.entrySet())
            { allRegisters.put(entry.getKey(), entry.getValue().clone()); }
 
        freeRegisters = new HashMap<OperandType, RegisterSet>();
        for(Entry<OperandType, RegisterSet> entry : r.freeRegisters.entrySet())
            { freeRegisters.put(entry.getKey(), entry.getValue().clone()); }
        
        definedRegisters = new HashMap<Register, Long>();
        for(Entry<Register, Long> entry : r.definedRegisters.entrySet())
            { definedRegisters.put(entry.getKey(), entry.getValue()); }
        
        subtypes = new HashMap<OperandType, HashSet<OperandType>>();
        subtypes.putAll(r.subtypes);
        
        defaultOperandType = r.defaultOperandType;
    }

    /**
     * Initializes the operand type.
     * 
     * @param <code>operandType</code> the operand type.
     * 
     * @param <code>registerSet</code> the set of registers.
     * 
     * @param <code>allocationStrategy</code> the strategy of register allocation.
     */
    public void initOperandType(OperandType operandType, RegisterSet registerSet, int allocationStrategy)
    {
        OperandType basicType = operandType.getBasicOperandType();
        
        allocationStrategies.put(operandType, allocationStrategy);
        
        allRegisters.put(operandType, registerSet);

        if(basicType == null)
            { return; }

        HashSet<OperandType> children = subtypes.get(basicType);
        
        if(children == null)
            { children = new HashSet<OperandType>(); }
        
        children.add(operandType);
        
        subtypes.put(basicType, children);
    }

    /**
     * Initializes the operand type.
     * 
     * @param <code>operandType</code> the operand type.
     * 
     * @param <code>registerSet</code> the set of registers.
     */
    public void initOperandType(OperandType operandType, RegisterSet registerSet)
    {
        initOperandType(operandType, registerSet, DEFAULT_ALLOCATION_STRATEGY);
    }

    /**
     * Returns default operand type.
     * 
     * @return default operand type.
     */
    public OperandType getDefaultOperandType()
    {
        return defaultOperandType;
    }
    
    /**
     * Sets default operand type.
     * 
     * @param <code>defaultOperandType</code> default operand type.
     */
    public void setDefaultOperandType(OperandType defaultOperandType)
    {
        this.defaultOperandType = defaultOperandType;
    }
    
    /**
     * Returns the number of unused registers.
     * 
     * @return the number of unused registers.
     */
    public int getCapacity()
        throws IllegalOperandTypeException
    {
        return getCapacity(defaultOperandType);
    }

    /**
     * Returns the number of unused registers of the given type.
     * 
     * @param  <code>type</code> the register type.
     * 
     * @return the number of unused registers of the given type.
     */
    public int getCapacity(OperandType type)
        throws IllegalOperandTypeException
    {
        RegisterSet registerSet = freeRegisters.get(type);
        
        if(registerSet == null)
            { new IllegalOperandTypeException(type); }
        
        return registerSet.size();
    }

    /**
     * Returns unused register.
     * 
     * @return unused register.
     */
    public Register getRegister()
        throws IllegalOperandTypeException, CanNotFindRegisterException
    {
        return getRegister(defaultOperandType);
    }

    /**
     * Returns unused register of the given type.
     * 
     * @param  <code>type</code> the register type.
     * 
     * @return unused register.
     */
    public Register getRegister(OperandType type)
        throws IllegalOperandTypeException, CanNotFindRegisterException
    {
        RegisterSet freeRegisterSet = freeRegisters.get(type);
        RegisterSet allRegisterSet = allRegisters.get(type);

        if(freeRegisterSet == null)
            { throw new IllegalOperandTypeException(type); }

        if(!freeRegisterSet.isEmpty())
            { return freeRegisterSet.randomRegister(); }
        
        int allocationStrategy = allocationStrategies.get(type);

        if(allocationStrategy == SHOULD_NOT_RETURN_USED_REGISTER)
            { throw new CanNotFindRegisterException(type); }
        
        return allRegisterSet.randomRegister();
    }

    /**
     * Calculates register block that contains the given register.
     * 
     * @param <code>type</code> type of the register block.
     * 
     * @param <code>register</code> the register.
     * 
     * @return the register block that contains the given register.
     */
    public RegisterBlock<Register> getRegisterBlock(OperandType type, Register register)
        throws IllegalOperandTypeException, CanNotFindRegisterException
    {
        RegisterSet registerSet = allRegisters.get(type);
        
        if(registerSet == null)
            { throw new IllegalOperandTypeException(type); }

        for(Register current : registerSet)
        {
            RegisterBlock<Register> block = (RegisterBlock<Register>)current;
            
            if(block.contains(register))
                { return block; }
        }
        
        throw new CanNotFindRegisterException(type);
    }
    
    /**
     * Checks if the register is used or not.
     * 
     * @param  <code>register</code> the register.
     * 
     * @return <code>true</code> if the register is used; <code>false</code>
     *         otherwise.
     */
    public boolean isUsedRegister(Register register)
        throws IllegalOperandTypeException
    {
        OperandType type = register.getRegisterType();
        RegisterSet registerSet = freeRegisters.get(type);
        
        if(registerSet == null)
            { throw new IllegalOperandTypeException(type); }
        
        return !registerSet.contains(register);
    }

    private void useRegisterForDescendants(OperandType type, Register register)
    {
        HashSet<OperandType> children = subtypes.get(type);
        
        if(children == null)
            { return; }
        
        for(OperandType child : children)
        {
            RegisterSet allRegisterSet = allRegisters.get(child);
            RegisterSet freeRegisterSet = freeRegisters.get(child); 
            
            Register registerToBeUsed = register;
            
            if(child.isBlockConsistingOf(type))
                { registerToBeUsed = getRegisterBlock(child, register); }
            
            if(allRegisterSet.contains(registerToBeUsed))
            {
                freeRegisterSet.remove(registerToBeUsed);
                
                useRegisterForDescendants(child, registerToBeUsed);
            }
        }
    }
    
    private void useRegisterForAncestors(OperandType type, Register register)
    {
        int i, size;
        OperandType basicType = type.getBasicOperandType();
        
        if(type.isRoot())
            { return; }
        
        RegisterSet freeRegisterSet = freeRegisters.get(basicType);
        
        if(type.isBlockConsistingOf(basicType))
        {
            RegisterBlock<Register> block = (RegisterBlock<Register>)register;
            
            size = type.getBlockSize();
            for(i = 0; i < size; i++)
            {
                Register registerToBeUsed = block.getRegister(i);
                
                freeRegisterSet.remove(registerToBeUsed);
                
                useRegisterForAncestors(basicType, registerToBeUsed);
            }
        }
        else
        {
            freeRegisterSet.remove(register);
            
            useRegisterForAncestors(basicType, register);
        }
    }
    
    /**
     * Marks the register as being used.
     * 
     * @param <code>register</code> the register.
     */
    public void useRegister(Register register)
        throws IllegalOperandTypeException
    {
        OperandType type = register.getRegisterType();

        // Process operand type
        RegisterSet freeRegisterSet = freeRegisters.get(type);
        
        if(freeRegisterSet == null)
            { throw new IllegalOperandTypeException(type); }
        
        freeRegisterSet.remove(register);
        
        // Process descendants
        useRegisterForDescendants(type, register);
        
        // Process ancestors
        useRegisterForAncestors(type, register);
    }
    
    private void freeRegisterForDescendants(OperandType type, Register register)
    {
        HashSet<OperandType> children = subtypes.get(type);
        
        if(children == null)
            { return; }
        
        for(OperandType child : children)
        {
            RegisterSet allRegisterSet = allRegisters.get(child);
            RegisterSet freeRegisterSet = freeRegisters.get(child); 
            
            if(allRegisterSet.contains(register))
            {
                if(!freeRegisterSet.contains(register))
                    { freeRegisterSet.add(register); }
                
                useRegisterForDescendants(child, register);
            }
        }
    }
    
    private void freeRegisterForAncestors(OperandType type, Register register)
    {
        int i, size;
        OperandType basicType = type.getBasicOperandType();
        
        if(type.isRoot())
            { return; }
        
        RegisterSet freeRegisterSet = freeRegisters.get(basicType);
        
        if(type.isBlockConsistingOf(basicType))
        {
            RegisterBlock<Register> block = (RegisterBlock<Register>)register;
            
            size = type.getBlockSize();
            for(i = 0; i < size; i++)
            {
                Register registerToBeFreed = block.getRegister(i);
                
                if(!freeRegisterSet.contains(registerToBeFreed))
                    { freeRegisterSet.add(registerToBeFreed); }
                
                freeRegisterForAncestors(basicType, registerToBeFreed);
            }
        }
        else
        {
            if(!freeRegisterSet.contains(register))
                { freeRegisterSet.add(register); }
            
            freeRegisterForAncestors(basicType, register);
        }
    }
    
    /**
     * Free register that has been used.
     * 
     * @param <code>register</code> the register.
     */
    public void freeRegister(Register register)
        throws IllegalOperandTypeException
    {
        OperandType type = register.getRegisterType();
        
        // Process operand type
        RegisterSet freeRegisterSet = freeRegisters.get(type);
        
        if(freeRegisterSet == null)
            { throw new IllegalOperandTypeException(type); }
        
        if(!freeRegisterSet.contains(register))
            { freeRegisterSet.add(register); }
        
        // Process descendants
        freeRegisterForDescendants(type, register);
        
        // Process ancestors
        freeRegisterForAncestors(type, register);
    }

    /**
     * Checks if the register is initialized (e.g., dependent from output
     * register of previous instruction) or not.
     * 
     * @return <code>true</code> if the register is defined; <code>false</code>
     *         otherwise.
     */
    public boolean isDefinedRegister(Register register)
    {
        return definedRegisters.containsKey(register);
    }

    /**
     * Marks the register as being initialized.
     * 
     * @param <code>register</code> the register.
     * 
     * @param <code>value</code> the register value.
     */
    public void defineRegister(Register register, long value)
    {
        definedRegisters.put(register, value);
    }

    /**
     * Returns the value of the register. 
     * 
     * @param  <code>register</code> the register.
     * 
     * @return the value of the register.
     */
    public long getRegisterValue(Register register)
        throws UndefinedRegisterException
    {
        Long value = definedRegisters.get(register);
        
        if(value == null)
            { throw new UndefinedRegisterException(register); }
        
        return value.longValue();
    }

    /** Resets state of the context. */
    @Override
    public void reset()
    {
        freeRegisters.clear();

        for(Entry<OperandType, RegisterSet> entry : allRegisters.entrySet())
            { freeRegisters.put(entry.getKey(), entry.getValue().clone()); }
        
        definedRegisters.clear();
    }
    
    /**
     * Returns the string representation of the register context object.
     * 
     * @return the string representation of the register context object.
     */
    public String toString()
    {
        return freeRegisters.toString();
    }
    
    /**
     * Returns a copy of the register context object.
     * 
     * @return a copy of the register context object.
     */
    @Override
    public RegisterContext clone()
    {
        return new RegisterContext(this);
    }
}
