/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: GeneratorContext.java,v 1.13 2009/12/04 15:19:59 vorobyev Exp $
 */

package com.unitesk.testfusion.core.context;

import com.unitesk.testfusion.core.dependency.Dependencies;
import com.unitesk.testfusion.core.model.OperandType;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.core.model.ResetableObject;
import com.unitesk.testfusion.core.model.register.Register;
import com.unitesk.testfusion.core.model.register.RegisterBlock;

/**
 * Generation context contains information that is used by generator engine.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class GeneratorContext implements ResetableObject
{
	/** Test data program. */
	protected Program dataProgram;
	
    /** Register context. */
    protected RegisterContext registerContext = new RegisterContext();
    
    /** All TLB indexes. */
    protected TLBIndexSet allTLBIndexes = new TLBIndexSet();

    /** Free TLB indexes. */
    protected TLBIndexSet freeTLBIndexes = new TLBIndexSet();
    
    /** Number of test action. */
    protected int testActionNumber;
    
    /** Test action */
    protected Program testAction;
    
    /** Dependencies of the test action. */
    protected Dependencies testActionDeps;
    
    /** Position of the instruction. */
    protected int instructionPosition;

    /** Default constructor. */
    public GeneratorContext() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> reference to a generator context object.
     */
    protected GeneratorContext(GeneratorContext r)
    {
    	dataProgram         = r.dataProgram.clone();
        registerContext     = r.registerContext.clone();
        allTLBIndexes       = r.allTLBIndexes.clone();
        freeTLBIndexes      = r.freeTLBIndexes.clone();
        testActionNumber    = r.testActionNumber;
        testActionDeps      = r.testActionDeps;
        instructionPosition = r.instructionPosition;
    }
    
    /**
     * Returns the test data program.
     * 
     * @return the test data program.
     */
    public Program getDataProgram()
    {
    	return dataProgram;
    }
    
    /**
     * Sets the test data program.
     *
     * @param <code>dataProgram</code> the test data program.
     */
    public void setDataProgram(Program dataProgram)
    {
    	this.dataProgram = dataProgram;
    }
    
    /**
     * Returns the number of test action.
     * 
     * @return the number of test action.
     */
    public int getTestActionNumber()
    {
        return testActionNumber;
    }

    /**
     * Sets the number of test action.
     * 
     * @param <code>testActionNumber</code> the number of test action.
     */
    public void setTestActionNumber(int testActionNumber)
    {
        this.testActionNumber = testActionNumber;
    }

    /**
     * Returns the test action.
     * 
     * @return the test action.
     */
    public Program getTestAction()
    {
        return testAction;
    }

    /**
     * Sets the test action.
     * 
     * @param <code>testAction</code> the test action.
     */
    public void setTestAction(Program testAction)
    {
        this.testAction = testAction;
    }

    /**
     * Returns the dependencies of the test action.
     * 
     * @return the dependencies of the test action.
     */
    public Dependencies getDependencies()
    {
        return testActionDeps;
    }

    /**
     * Sets the dependencies of the test action.
     * 
     * @param <code>deps</code> the dependencies of the test action.
     */
    public void setDependencies(Dependencies deps)
    {
        this.testActionDeps = deps;
    }

    /**
     * Returns the position of the current instruction in the test action.
     * 
     * @return the position of the current instruction in the test action.
     */
    public int getPosition()
    {
        return instructionPosition;
    }

    /**
     * Sets the position of the current instruction in the test action.
     * 
     * @param <code>position</code> the position of the current instruction
     *        in the test action. 
     */
    public void setPosition(int position)
    {
        this.instructionPosition = position;
    }

    /**
     * Returns default operand type.
     * 
     * @return default operand type.
     */
    public OperandType getDefaultOperandType()
    {
        return registerContext.getDefaultOperandType();
    }
    
    /**
     * Sets default operand type.
     * 
     * @param <code>defaultOperandType</code> default operand type.
     */
    public void setDefaultOperandType(OperandType defaultOperandType)
    {
        registerContext.setDefaultOperandType(defaultOperandType);
    }
    
    /**
     * Initializes the operand type.
     * 
     * @param <code>type</code> the operand type.
     * 
     * @param <code>registerSet</code> the set of registers.
     * 
     * @param <code>allocationStrategy</code> the allocation strategy.
     */
    public void initOperandType(OperandType type, RegisterSet registerSet, int allocationStrategy)
    {
        registerContext.initOperandType(type, registerSet, allocationStrategy);
    }

    /**
     * Initializes the operand type.
     * 
     * @param <code>type</code> the operand type.
     * 
     * @param <code>registerSet</code> the set of registers.
     */
    public void initOperandType(OperandType type, RegisterSet registerSet)
    {
        registerContext.initOperandType(type, registerSet);
    }
    
    /**
     * Initializes TLB indexes.
     * 
     * @param <code>tlbIndexes</code> TLB indexes.
     */
    public void initTLBIndexes(TLBIndexSet tlbIndexes)
    {
        allTLBIndexes.clear();
        allTLBIndexes.addAll(tlbIndexes);
    }
    
    /**
     * Returns the number of unused registers.
     * 
     * @return the number of unused registers.
     */
    public int getCapacity()
    {
        return registerContext.getCapacity();
    }

    /**
     * Returns the number of unused registers of the given type.
     * 
     * @param  <code>type</code> the register type.
     * 
     * @return the number of unused registers of the given type.
     */
    public int getCapacity(OperandType type)
    {
        return registerContext.getCapacity(type);
    }

    /**
     * Returns unused register.
     * 
     * @return unused register.
     */
    public Register getRegister()
    {
        return registerContext.getRegister();
    }

    /**
     * Returns unused register of the given type.
     * 
     * @param  <code>type</code> the register type.
     * 
     * @return unused register.
     */
    public Register getRegister(OperandType type)
    {
        return registerContext.getRegister(type);
    }

    /**
     * Calculates register block that contains the given register.
     * 
     * @param <code>register</code> the register.
     * 
     * @return the register block.
     */
    public RegisterBlock getRegisterBlock(OperandType type, Register register)
    {
        return registerContext.getRegisterBlock(type, register);
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
    {
        return registerContext.isUsedRegister(register);
    }
    
    /**
     * Marks the register as being used.
     * 
     * @param <code>register</code> the register.
     */
    public void useRegister(Register register)
    {
        registerContext.useRegister(register);
    }
    
    /**
     * Free register that has been used.
     * 
     * @param <code>register</code> the register.
     */
    public void freeRegister(Register register)
    {
        registerContext.freeRegister(register);
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
        return registerContext.isDefinedRegister(register);
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
        registerContext.defineRegister(register, value);
    }

    /**
     * Returns the value of the register. 
     * 
     * @param  <code>register</code> the register.
     * 
     * @return the value of the register.
     */
    public long getRegisterValue(Register register)
    {
        return registerContext.getRegisterValue(register);
    }

    /**
     * Returns number of unused TLB indexes.
     * 
     * @return number of unused TLB indexes.
     */
    public int getTLBCapacity()
    {
        return freeTLBIndexes.size();
    }
    
    /**
     * Returns unused index of TLB.
     * 
     * @return unused index of TLB.
     */
    public int getTLBIndex()
    {
        return freeTLBIndexes.randomTLBIndex();
    }
    
    /**
     * Mark the TLB index as being used.
     * 
     * @param <code>index</code> the TLB index.
     */
    public void useTLBIndex(int index)
    {
        freeTLBIndexes.remove(new Integer(index));
    }

    /**
     * Checks if TLB index is used or not.
     * 
     * @param <code>index</code> TLB index.
     * 
     * @return <code>true</code> if TLB index is used; <code>false</code>
     *         otherwise.
     */
    public boolean isUsedTLBIndex(int index)
    {
        return !freeTLBIndexes.contains(index);
    }
    
    /**
     * Generates label.
     * 
     * @param <code>number</code> the label number.
     * 
     * @return label.
     */
    public String getLabel(int number)
    {
        return "" + number;
    }
    
    /** Resets the state of the context. */
    @Override
    public void reset()
    {
        registerContext.reset();
        
        freeTLBIndexes.clear();
        freeTLBIndexes.addAll(allTLBIndexes);

        testAction = null;
        testActionDeps = null;
        
        testActionNumber = 0;
        instructionPosition = 0;
    }
    
    /**
     * Returns the string representation of the context.
     * 
     * @return the string representation of the context.
     */
    public String toString()
    {
        return registerContext.toString();
    }
    
    /**
     * Returns a copy of the context.
     * 
     * @return a copy of the context.
     */
    @Override
    public GeneratorContext clone()
    {
        return new GeneratorContext(this);
    }
}
