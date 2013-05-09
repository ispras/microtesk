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

public final class BlockBuilderFactory
{
    public BlockBuilderFactory()
    {
    }

    public BlockBuilder newBlockBuilder()
    {
        return new BlockBuilder();
    }

    public AbstractCallBuilder newAbstractCallBuilder(String instructionName)
    {
        return new AbstractCallBuilder(instructionName);
    }
}
