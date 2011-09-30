/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: RegisterSituation.java,v 1.13 2009/07/08 08:26:10 kamkin Exp $
 */

package com.unitesk.testfusion.core.situation;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.generator.Random;
import com.unitesk.testfusion.core.model.OperandType;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.register.Register;
import com.unitesk.testfusion.core.model.register.RegisterBlock;

/**
 * Test situation for register operand.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RegisterSituation extends EmptySituation
{
    /** Register. */
    protected Register register;
    
    /** Type of the register. */
    protected OperandType type; 

    /**
     * Constructor.
     * 
     * @param <code>type</code> the register type.
     */
    public RegisterSituation(OperandType type)
    {
        this.type = type;
    }

    /** Default constructor. */
    public RegisterSituation()
    {
        this(OperandType.REGISTER);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to register situation objet.
     */
    protected RegisterSituation(RegisterSituation r)
    {
        type = r.type;
        register = r.register;
    }
    
    /**
     * Returns the register.
     * 
     * @return the register.
     */
    public Register getRegister()
    {
        return register;
    }
    
    /**
     * Constructs the register situation.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     */
    @Override
    public boolean construct(Processor processor, GeneratorContext context)
    {
        return construct(type, context);
    }

    /**
     * Constructs the register situation.
     * 
     * @param <code>operandType</code> the operand type.
     * 
     * @param <code>context</code> the context of generation.
     */
    public boolean construct(OperandType operandType, GeneratorContext context)
    {
        return construct(context.getRegister(operandType), context);
    }
    
    /**
     * Sets the register.
     * 
     * @param <code>register</code> the register.
     */
    public boolean construct(Register fromRegister, GeneratorContext context)
    {
        OperandType depsType = type;
        OperandType fromType = fromRegister.getRegisterType();
        
        if(depsType.isBlock() == fromType.isBlock())
        {
            register = fromRegister;
        }
        else if(fromType.isBlock())
        {
            RegisterBlock block = (RegisterBlock)fromRegister;
            
            register = block.getRegister(Random.int32_non_negative_less(block.size()));
        }
        else if(depsType.isBlock())
        {
            register = context.getRegisterBlock(depsType, fromRegister);
        }
        
        return register != null;
    }
    
    /**
     * Returns a copy of the register situation.
     * 
     * @return a copy of the register situation.
     */
    @Override
    public RegisterSituation clone()
    {
        return new RegisterSituation(this);
    }
}
