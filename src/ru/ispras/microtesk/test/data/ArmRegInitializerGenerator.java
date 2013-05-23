/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArmRegInitializerGenerator.java, May 23, 2013 4:52:30 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.data;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstruction;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilderEx;
import ru.ispras.microtesk.test.block.Argument;

public class ArmRegInitializerGenerator implements IInitializerGenerator 
{
    private final IModel model;
    
    public ArmRegInitializerGenerator(IModel model)
    {
        assert null != model;
        this.model = model;
    }

    @Override
    public boolean isCompatible(Argument dest)
    {
        return dest.getModeName().equals("REG");
    }
    
    // Signature:
    //
    // op MOV_IMMEDIATE (cond : Condition, sets : setS, src1 : REG, src2 : IMMEDIATE)
    // mode REG (r : index) = r
    // mode IMMEDIATE (r : nibble, c : byte_t) = coerce(card(32), c) >>> (r*2)

    @Override
    public List<ConcreteCall> createInitializingCode(Argument dest, Data data) throws ConfigurationException
    {
        final IInstruction instruction = model.getInstruction("MOV_IMMEDIATE");

        final List<ConcreteCall> result = new ArrayList<ConcreteCall>();
        final byte dataBytes[] = data.getRawData().toByteArray(); 
        
        for (int byteIndex = 0; byteIndex < 4; ++byteIndex)
        {
            final IInstructionCallBuilderEx callBuilder = instruction.createCallBuilder();

            callBuilder.getArgumentBuilder("cond").getModeBuilder("blank");
            callBuilder.getArgumentBuilder("sets").getModeBuilder("setSoff");

            final IAddressingModeBuilder regModeBuilder =
                callBuilder.getArgumentBuilder("src1").getModeBuilder(dest.getModeName());
            
            for (Argument.ModeArg modeArg : dest.getModeArguments().values())
                regModeBuilder.setArgumentValue(modeArg.name, modeArg.value);
            
            final IAddressingModeBuilder immediateModeBuilder =
                callBuilder.getArgumentBuilder("src2").getModeBuilder("IMMEDIATE");
            
            immediateModeBuilder.setArgumentValue("r", byteIndex * 4);
            immediateModeBuilder.setArgumentValue("c", (int) dataBytes[4-byteIndex - 1]);
            
            result.add(new ConcreteCall("MOV_IMMEDIATE", null, callBuilder.getCall()));
        }

        return result;
    }
}
