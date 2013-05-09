/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TestEngine.java, May 8, 2013 11:00:02 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test;

import ru.ispras.microtesk.test.block.BlockBuilderFactory;

public final class TestEngine
{
    private final BlockBuilderFactory blockBuilderFactory;

    public static TestEngine getInstance()
    {
        return new TestEngine();
    }

    private TestEngine()
    {
        this.blockBuilderFactory = new BlockBuilderFactory();
    }

    public BlockBuilderFactory getBlockBuilders()
    {
        return blockBuilderFactory;
    }
}
