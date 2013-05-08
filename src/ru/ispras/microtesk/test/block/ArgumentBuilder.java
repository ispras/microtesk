/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArgumentBuilder.java, May 8, 2013 7:13:12 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

import java.util.HashMap;
import java.util.Map;

public final class ArgumentBuilder
{
    private final AbstractCallBuilder callBuilder;

    private final String name;
    private final String modeName;
    private final Map<String, Argument.ModeArg> arguments;

    protected ArgumentBuilder(
        AbstractCallBuilder callBuilder,
        String name,
        String modeName
        )
    {
        this.callBuilder = callBuilder;
        this.name        = name;
        this.modeName    = modeName;
        this.arguments   = new HashMap<String, Argument.ModeArg>();
    }

    public ArgumentBuilder setArgument(String name, int value)
    {
        arguments.put(name, new Argument.ModeArg(name, value));
        return this;
    }

    public void build()
    {
        callBuilder.setArgument(name, new Argument(name, modeName, arguments));
    }
}
