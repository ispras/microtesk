/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelISA.java, Dec 1, 2012 11:46:09 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple;

import java.util.Collections;
import java.util.Map;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IArgumentBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBlockBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilder;

class ModelISA
{
    private ModelISA() {}

    private static IInstructionCallBlockBuilder currentBlockBuilder = null;

    private final static class Mode
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
    
    private static final class Argument
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

    private static void setupCall(String name, Argument ... args) throws ConfigurationException
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

    public static void mov(Mode op1, Mode op2) throws ConfigurationException
    {
        setupCall("Mov", new Argument("op1", op1), new Argument("op2", op2));        
    }

    public static void add(Mode op1, Mode op2) throws ConfigurationException
    {
        setupCall("Add", new Argument("op1", op1), new Argument("op2", op2)); 
    }

    public static void sub(Mode op1, Mode op2) throws ConfigurationException
    {
        setupCall("Sub", new Argument("op1", op1), new Argument("op2", op2)); 
    }

    public static Mode reg(int i)
    {
        return new Mode("REG", Collections.singletonMap("i", i));
    }

    public static Mode ireg(int i)
    {
        return new Mode("IREG", Collections.singletonMap("i", i));
    }

    public static Mode mem(int i)
    {
        return new Mode("MEM", Collections.singletonMap("i", i));
    }

    public static Mode imm(int i)
    {
        return new Mode("IMM", Collections.singletonMap("i", i));
    }
}
