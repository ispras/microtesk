/*
 * Copyright (c) {year} ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelMain.java, Mar 14, 2013 3:22:32 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.debug;

import java.util.Map;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IArgumentBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBlockBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilder;

public abstract class AbstractModelISA
{
    private static IInstructionCallBlockBuilder currentBlockBuilder = null;

    protected final static class Mode
    {
        private final String name;
        private final Map<String, Integer> parameters;

        public Mode(String name, Map<String, Integer> parameters)
        {
            this.name = name;
            this.parameters = parameters;
        }

        public void visit(IArgumentBuilder argBuilder) throws ConfigurationException
        {
            final IAddressingModeBuilder modeBuilder =
                argBuilder.getModeBuilder(name);

            for (Map.Entry<String, Integer> e : parameters.entrySet())
                modeBuilder.setArgumentValue(e.getKey(), e.getValue());
        }        
    }
    
    protected static final class Argument
    {
        private final String name;
        private final Mode mode;

        public Argument(String name, Mode mode)
        {
            assert null != name;
            assert null != mode;

            this.name = name;
            this.mode = mode;
        }

        public String getName()
        {
            return name;
        }

        public Mode getMode()
        {
            return mode;
        }
    }

    protected static void setupCall(String name, Argument ... args) throws ConfigurationException
    {
        assert null != currentBlockBuilder: "No block builder";
        final IInstructionCallBuilder callBuilder = currentBlockBuilder.addCall(name);

        for (Argument arg : args)
        {
            final IArgumentBuilder argBuilder = callBuilder.getArgumentBuilder(arg.getName());
            arg.getMode().visit(argBuilder);
        }
    }

    public static void setCurrentBlockBuilder(IInstructionCallBlockBuilder blockBuilder)
    {
        currentBlockBuilder = blockBuilder;
    }   
}
