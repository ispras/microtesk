/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * CallFactory.java, Apr 16, 2014 1:26:22 PM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.arm.initializer;

import ru.ispras.microtesk.model.api.ICallFactory;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.model.api.instruction.IOperationBuilder;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;

/*
 * A GPR initialization is performed in the following way (a Ruby example):
 * 
 * eor blank, setsoff, reg(0), reg(0), register0
 *     
 * mov blank, setsoff, reg(0), lsl_immediate(0, 8)
 * add_immediate blank, setsoff, reg(0), reg(0), immediate(0, 0b00001111)
 *     
 * mov blank, setsoff, reg(0), lsl_immediate(0, 8)
 * add_immediate blank, setsoff, reg(0), reg(0), immediate(0, 0b00000111)
 *     
 * mov blank, setsoff, reg(0), lsl_immediate(0, 8)
 * add_immediate blank, setsoff, reg(0), reg(0), immediate(0, 0b00000011)
 *     
 * mov blank, setsoff, reg(0), lsl_immediate(0, 8)
 * add_immediate blank, setsoff, reg(0), reg(0), immediate(0, 0b00000001)
 */

final class CallFactory
{
    private final IModel model;

    public CallFactory(IModel model)
    {
        if (null == model)
            throw new NullPointerException();

        this.model = model;
    }

    /*
     * Format:
     *     op MOV (cond : Condition, sets : setS, src1 : REG, src2 : DATA_PROCESSING)
     *     mode REG (r : index)
     *     mode LSL_IMMEDIATE (r : index, amount : Bit5)
     * 
     * Example:
     *     mov blank, setsoff, reg(0), lsl_immediate(0, 8)
     */

    public final ConcreteCall createMOV(Argument dest) throws ConfigurationException
    {
        if (null == dest)
            throw new NullPointerException();

        if (dest.getKind() != Argument.Kind.MODE)
            throw new IllegalArgumentException(String.format(
                "%s is not a mode.", dest.getName()));

        final Primitive mode = (Primitive) dest.getValue();
        final Argument r = mode.getArguments().get("r");
        
        final int registerIndex;
        
        if (r.getKind() == Argument.Kind.IMM)
            registerIndex = (Integer) r.getValue();
        else if (r.getKind() == Argument.Kind.IMM_RANDOM)
            registerIndex = ((RandomValue) r.getValue()).getValue();
        else
            throw new IllegalArgumentException("Unsupported kind: " + r.getKind());

        return createMOV(registerIndex);
    }

    public final ConcreteCall createMOV(int registerIndex) throws ConfigurationException
    {
        final IOperationBuilder rootBuilder =
            createRootBuilder("MOV", "blank", "setSoff");

        rootBuilder.setArgument("src1", createREG(registerIndex));
        
        final IAddressingModeBuilder modeBuilder =
            model.getCallFactory().newMode("LSL_IMMEDIATE");

        modeBuilder.setArgumentValue("r", registerIndex);
        modeBuilder.setArgumentValue("amount", 8);

        rootBuilder.setArgument("src2", modeBuilder.getProduct());

        return createCall(rootBuilder.build());
    }

    /*
     * Format: 
     *    op ADD_IMMEDIATE(cond : Condition, sets : setS, src1 : REG, src2 : REG, src3 : IMMEDIATE)
     *    mode REG (r : index)
     *    mode IMMEDIATE (r : nibble, c : byte_t)
     *
     * Example:
     *    add_immediate blank, setsoff, reg(0), reg(0), immediate(0, 0b00000001)
     */

    public final ConcreteCall createADD_IMMEDIATE(Argument dest, byte value) throws ConfigurationException
    {
        if (null == dest)
            throw new NullPointerException();

        if (dest.getKind() != Argument.Kind.MODE)
            throw new IllegalArgumentException(String.format(
                "%s is not a mode.", dest.getName()));

        final Primitive mode = (Primitive) dest.getValue();
        final Argument r = mode.getArguments().get("r");
        
        final int registerIndex;
        if (r.getKind() == Argument.Kind.IMM)
            registerIndex = (Integer) r.getValue();
        else if (r.getKind() == Argument.Kind.IMM_RANDOM)
            registerIndex = ((RandomValue) r.getValue()).getValue();
        else
            throw new IllegalArgumentException("Unsupported kind: " + r.getKind());

        return createADD_IMMEDIATE(registerIndex, value);
    }

    protected final ConcreteCall createADD_IMMEDIATE(int registerIndex, byte value) throws ConfigurationException
    {
        final IOperationBuilder rootBuilder =
            createRootBuilder("ADD_IMMEDIATE", "blank", "setSoff");

        rootBuilder.setArgument("src1", createREG(registerIndex));
        rootBuilder.setArgument("src2", createREG(registerIndex));

        final IAddressingModeBuilder immediateModeBuilder =
            model.getCallFactory().newMode("IMMEDIATE");

        immediateModeBuilder.setArgumentValue("r", 0);
        immediateModeBuilder.setArgumentValue("c", (int) value);
        
        rootBuilder.setArgument("src3", immediateModeBuilder.getProduct());

        return createCall(rootBuilder.build());
    }

    /*
     * Format:
     *     op EOR (cond : Condition, sets : setS, src1 : REG, src2 : REG, src3 : DATA_PROCESSING)
     *     mode REG (r : index)
     *     
     * Example:
     *     eor blank, setsoff, reg(0), reg(0), register0
     */

    public final ConcreteCall createEOR(Argument dest) throws ConfigurationException
    {
        if (null == dest)
            throw new NullPointerException();

        if (dest.getKind() != Argument.Kind.MODE)
            throw new IllegalArgumentException(String.format(
                "%s is not a mode.", dest.getName()));

        final Primitive mode = (Primitive) dest.getValue();
        final Argument r = mode.getArguments().get("r");

        final int registerIndex;
        if (r.getKind() == Argument.Kind.IMM)
            registerIndex = (Integer) r.getValue();
        else if (r.getKind() == Argument.Kind.IMM_RANDOM)
            registerIndex = ((RandomValue) r.getValue()).getValue();
        else
            throw new IllegalArgumentException("Unsupported kind: " + r.getKind());

        return createEOR(registerIndex);
    }

    public final ConcreteCall createEOR(int registerIndex) throws ConfigurationException
    {
        final IOperationBuilder rootBuilder =
            createRootBuilder("EOR", "blank", "setSoff");
        
        rootBuilder.setArgument("src1", createREG(registerIndex));
        rootBuilder.setArgument("src2", createREG(registerIndex));
        
        final IAddressingModeBuilder modeBuilder =
            model.getCallFactory().newMode(String.format("REGISTER%d", registerIndex));

        rootBuilder.setArgument("src3", modeBuilder.getProduct());

        return createCall(rootBuilder.build());
    }
    
    private IAddressingMode createREG(int index) throws ConfigurationException
    {
        final ICallFactory factory = model.getCallFactory();
        final IAddressingModeBuilder builder = factory.newMode("REG");

        builder.setArgumentValue("r", index);
        return builder.getProduct();
    }

    private IOperationBuilder createRootBuilder(
        String name, String cond, String sets) throws ConfigurationException
    {
        final ICallFactory factory = model.getCallFactory(); 
        final IOperationBuilder builder = factory.newOp(name, "#root");
        
        builder.setArgument("cond", factory.newMode(cond).getProduct());
        builder.setArgument("sets", factory.newMode(sets).getProduct());

        return builder;
    }
    
    private ConcreteCall createCall(IOperation rootOperation)
    {
        return new ConcreteCall(
            model.getCallFactory().newCall(rootOperation));
    }
}
