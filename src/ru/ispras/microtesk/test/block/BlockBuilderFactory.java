/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BlockFactory.java, May 8, 2013 7:56:22 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IInstruction;

public final class BlockBuilderFactory
{
    private final IModel model;

    public BlockBuilderFactory(IModel model)
    {
        this.model = model;
    }

    public BlockBuilder newBlockBuilder()
    {
        return new BlockBuilder();
    }

    public AbstractCallBuilder newAbstractCallBuilder(String instructionName) throws ConfigurationException
    {
        final IInstruction instruction =
            model.getInstruction(instructionName);

        return new AbstractCallBuilder(instruction);
    }
}
