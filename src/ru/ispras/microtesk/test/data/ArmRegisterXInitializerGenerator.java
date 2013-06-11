/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArmRegisterXInitializerGenerator.java, May 23, 2013 7:31:40 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.test.block.Argument;

public final class ArmRegisterXInitializerGenerator extends ArmInitializerGenerator 
{
    public ArmRegisterXInitializerGenerator(IModel model)
    {
        super(model);
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
        final List<ConcreteCall> result = new ArrayList<ConcreteCall>();
        final int registerIndex = getRegisterIndex(dest.getModeName());

        result.add(createEOR(registerIndex));

        final byte dataBytes[] = data.getRawData().toByteArray(); 
        for (int byteIndex = 0; byteIndex < 4; ++byteIndex)
        {
            result.add(createMOV(registerIndex));
            result.add(createADD_IMMEDIATE(registerIndex, dataBytes[byteIndex]));
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
