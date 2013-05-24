/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArmRegisterXInitializerGenerator.java, May 23, 2013 7:31:40 PM Andrei
 * Tatarnikov
 */

package ru.ispras.microtesk.test.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstruction;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilderEx;
import ru.ispras.microtesk.test.block.Argument;

public class ArmRegisterXInitializerGenerator implements IInitializerGenerator 
{
    private final IModel model;

    public ArmRegisterXInitializerGenerator(IModel model)
    {
        assert null != model;
        this.model = model;
    }

    @Override
    public boolean isCompatible(Argument dest)
    {
        final Matcher matcher = Pattern.compile("^REGISTER[\\d]{1,2}$").matcher(dest.getModeName());
        return matcher.matches();
    }

    @Override
    public List<ConcreteCall> createInitializingCode(
        Argument dest, Data data) throws ConfigurationException
    {
        final IInstruction  instruction = model.getInstruction("MOV_IMMEDIATE");
        final List<ConcreteCall> result = new ArrayList<ConcreteCall>();

        final byte dataBytes[] = data.getRawData().toByteArray(); 
        for (int byteIndex = 0; byteIndex < 4; ++byteIndex)
        {
            final IInstructionCallBuilderEx callBuilder = instruction.createCallBuilder();

            callBuilder.getArgumentBuilder("cond").getModeBuilder("blank");
            callBuilder.getArgumentBuilder("sets").getModeBuilder("setSoff");

            callBuilder.getArgumentBuilder("src1").getModeBuilder("REG").
                setArgumentValue("r", getRegisterIndex(dest.getModeName()));

            final IAddressingModeBuilder immediateModeBuilder =
                callBuilder.getArgumentBuilder("src2").getModeBuilder("IMMEDIATE");

            immediateModeBuilder.setArgumentValue("r", byteIndex * 4);
            immediateModeBuilder.setArgumentValue("c", (int) dataBytes[4-byteIndex - 1]);

            result.add(new ConcreteCall("MOV_IMMEDIATE", null, callBuilder.getCall()));
        }

        return result;
    }

    private int getRegisterIndex(String regName)
    {
        final Matcher matcher = Pattern.compile("\\d{1,2}$").matcher(regName);
        matcher.find();
        return Integer.parseInt(matcher.group());
    }
}
