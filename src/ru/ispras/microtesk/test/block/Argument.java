/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * СallArgument.java, May 8, 2013 11:49:25 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

import java.util.Map;

public final class Argument
{
    public static final class ModeArg
    {
        public final String name;
        public final    int value;

        protected ModeArg(String name, int value)
        {
            this.name  = name;
            this.value = value;
        }
    }

    private final String name;
    private final String modeName;
    private final Map<String, ModeArg> arguments;

    private ArgumentKind kind;

    protected Argument(
        String name,
        String modeName,
        Map<String, ModeArg> arguments
        )
    {
        this.name      = name;
        this.modeName  = modeName;
        this.arguments = arguments;
        this.kind      = ArgumentKind.DEFAULT;
    }

    public String getName()
    {
        return name;
    }

    public String getModeName()
    {
        return modeName;   
    }
    
    public Map<String, ModeArg> getModeArguments()
    {
        return arguments;
    }

    public ArgumentKind getKind()
    {
        return kind; 
    }

    public void setKind(ArgumentKind kind)
    {
        this.kind = kind;
    }
}
