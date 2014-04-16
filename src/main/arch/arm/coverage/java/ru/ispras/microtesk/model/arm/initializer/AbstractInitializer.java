/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * AbstractInitializer.java, Apr 16, 2014 1:26:22 PM Andrei Tatarnikov
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

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstruction;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilderEx;
import ru.ispras.microtesk.test.block.Argument;
import ru.ispras.microtesk.test.data.ConcreteCall;
import ru.ispras.microtesk.test.data.IInitializerGenerator;

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

public abstract class AbstractInitializer implements IInitializerGenerator
{
    private final IModel model;

    public AbstractInitializer(IModel model)
    {
        assert null != model;
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

    protected final ConcreteCall createMOV(Argument dest) throws ConfigurationException
    {
        final int registerIndex = dest.getModeArguments().get("r").value;
        return createMOV(registerIndex);
    }

    protected final ConcreteCall createMOV(int registerIndex) throws ConfigurationException
    {
        final IInstructionCallBuilderEx callBuilder = createCallBuilder("MOV", "blank", "setSoff");

        callBuilder.getArgumentBuilder("src1")
                   .getModeBuilder("REG")
                   .setArgumentValue("r", registerIndex);

        final IAddressingModeBuilder modeBuilder =
             callBuilder.getArgumentBuilder("src2").getModeBuilder("LSL_IMMEDIATE");

        modeBuilder.setArgumentValue("r", registerIndex);
        modeBuilder.setArgumentValue("amount", 8);

        return new ConcreteCall("MOV", null, callBuilder.getCall());
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

    protected final ConcreteCall createADD_IMMEDIATE(Argument dest, byte value) throws ConfigurationException
    {
        final int registerIndex = dest.getModeArguments().get("r").value;
        return createADD_IMMEDIATE(registerIndex, value);
    }

    protected final ConcreteCall createADD_IMMEDIATE(int registerIndex, byte value) throws ConfigurationException
    {
        final IInstructionCallBuilderEx callBuilder = createCallBuilder("ADD_IMMEDIATE", "blank", "setSoff");

        callBuilder.getArgumentBuilder("src1")
                   .getModeBuilder("REG")
                   .setArgumentValue("r", registerIndex);

        callBuilder.getArgumentBuilder("src2")
                   .getModeBuilder("REG")
                   .setArgumentValue("r", registerIndex);
        
        final IAddressingModeBuilder immediateModeBuilder =
            callBuilder.getArgumentBuilder("src3").getModeBuilder("IMMEDIATE");

        immediateModeBuilder.setArgumentValue("r", 0);
        immediateModeBuilder.setArgumentValue("c", (int) value);

        return new ConcreteCall("ADD_IMMEDIATE", null, callBuilder.getCall());
    }

    /*
     * Format:
     *     op EOR (cond : Condition, sets : setS, src1 : REG, src2 : REG, src3 : DATA_PROCESSING)
     *     mode REG (r : index)
     *     
     * Example:
     *     eor blank, setsoff, reg(0), reg(0), register0
     */

    protected final ConcreteCall createEOR(Argument dest) throws ConfigurationException
    {
        final int registerIndex = dest.getModeArguments().get("r").value;
        return createEOR(registerIndex);
    }
    
    protected final ConcreteCall createEOR(int registerIndex) throws ConfigurationException
    {
        final IInstructionCallBuilderEx callBuilder = createCallBuilder("EOR", "blank", "setSoff");

        callBuilder.getArgumentBuilder("src1")
                   .getModeBuilder("REG")
                   .setArgumentValue("r", registerIndex);

        callBuilder.getArgumentBuilder("src2")
                   .getModeBuilder("REG")
                   .setArgumentValue("r", registerIndex);

        callBuilder.getArgumentBuilder("src3")
                   .getModeBuilder(String.format("REGISTER%d", registerIndex));

        return new ConcreteCall("EOR", null, callBuilder.getCall());
    }

    private IInstructionCallBuilderEx createCallBuilder(
        String name, String cond, String sets) throws ConfigurationException
    {
        final IInstruction instruction = model.getInstruction(name);
        final IInstructionCallBuilderEx callBuilder = instruction.createCallBuilder();

        callBuilder.getArgumentBuilder("cond").getModeBuilder(cond);
        callBuilder.getArgumentBuilder("sets").getModeBuilder(sets);

        return callBuilder;
    }
}
