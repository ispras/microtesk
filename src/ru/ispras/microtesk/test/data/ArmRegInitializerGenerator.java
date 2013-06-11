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
import ru.ispras.microtesk.test.block.Argument;

public final class ArmRegInitializerGenerator extends ArmInitializerGenerator 
{
    public ArmRegInitializerGenerator(IModel model)
    {
        super(model);
    }

    @Override
    public boolean isCompatible(Argument dest)
    {
        return dest.getModeName().equals("REG");
    }

    @Override
    public List<ConcreteCall> createInitializingCode(Argument dest, Data data) throws ConfigurationException
    {
        final List<ConcreteCall> result = new ArrayList<ConcreteCall>();
        result.add(createEOR(dest));

        final byte dataBytes[] = data.getRawData().toByteArray(); 
        for (int byteIndex = 0; byteIndex < 4; ++byteIndex)
        {
            result.add(createMOV(dest));
            result.add(createADD_IMMEDIATE(dest, dataBytes[byteIndex]));
        }

        return result;
    }
}
