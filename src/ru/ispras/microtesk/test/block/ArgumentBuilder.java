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
    interface Setter
    {
        void setArgument(String name, Argument argument);
    }

    private final Setter setter;

    private final String name;
    private final String modeName;
    private final Map<String, Argument.ModeArg> arguments;

    protected ArgumentBuilder(
        Setter setter,
        String name,
        String modeName
        )
    {
        assert null != setter;
        
        this.setter    = setter;
        this.name      = name;
        this.modeName  = modeName;
        this.arguments = new HashMap<String, Argument.ModeArg>();
    }

    public ArgumentBuilder setArgument(String name, int value)
    {
        assert !arguments.containsKey(name);
        arguments.put(name, new Argument.ModeArg(name, value));
        return this;
    }

    public ArgumentBuilder setRandomArgument(String name)
    {
        assert !arguments.containsKey(name);
        arguments.put(name, new Argument.ModeArg(name));
        return this;
    }

    public Argument build()
    {
        final Argument argument = new Argument(name, modeName, arguments);
        setter.setArgument(name, argument);
        return argument;
    }
}
