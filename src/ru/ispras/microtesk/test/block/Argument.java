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

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IArgumentBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilder;

public final class Argument
{
    protected static final class ModeArg
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

    protected Argument(String name, String modeName, Map<String, ModeArg> arguments)
    {
        this.name      = name;
        this.modeName  = modeName;
        this.arguments = arguments;
    }

    public String getName()
    {
        return name;
    }

    public String getModeName()
    {
        return modeName;   
    }

    public void addToInstructionCall(
        IInstructionCallBuilder callBuilder) throws ConfigurationException
    {
        final IArgumentBuilder argumentBuilder = 
            callBuilder.getArgumentBuilder(name);

        final IAddressingModeBuilder modeBuilder =
            argumentBuilder.getModeBuilder(modeName);

        for (ModeArg entry : arguments.values())
            modeBuilder.setArgumentValue(entry.name, entry.value);
    }
}
