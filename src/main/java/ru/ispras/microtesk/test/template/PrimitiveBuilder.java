/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PrimitiveBuilder.java, Aug 27, 2014 11:08:31 AM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.test.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.instruction.AddressingModeImm;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.model.api.metadata.MetaInstruction;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.test.template.Primitive.Kind;

public final class PrimitiveBuilder
{
    private interface Strategy
    {
        String getName();
        String getDescription();
        String getNextArgumentName();
        void checkValidArgument(Argument arg);
        void checkAllArgumentsAssigned(Set<String> argNames);
    }

    private final Strategy strategy;
    private final Kind kind;
    private final Map<String, Argument> args;
    private final String contextName;

    PrimitiveBuilder(MetaInstruction metaData)
    {
        this(new StrategyInstruction(metaData), Kind.INSTR, null);
    }

    PrimitiveBuilder(MetaOperation metaData, String contextName)
    {
        this(new StrategyOperation(metaData, contextName), Kind.OP, contextName);
    }

    PrimitiveBuilder(MetaAddressingMode metaData)
    {
        this(new StrategyAddressingMode(metaData), Kind.MODE, null);
    }

    private PrimitiveBuilder(Strategy strategy, Kind kind, String contextName)
    {
        this.strategy = strategy;
        this.kind = kind;
        this.args = new HashMap<String, Argument>();
        this.contextName = contextName;
    }

    private void putArgument(Argument arg)
    {
        args.put(arg.getName(), arg);
    }

    public Primitive build()
    {
        checkAllArgumentsSet(Collections.unmodifiableSet(args.keySet()));
        return new Primitive(kind, getName(), args, contextName);
    }

    ///////////////////////////////////////////////////////////////////////////
    // For Array-based syntax

    public String addArgument(int value)
    {
        final String name = getNextArgumentName();
        setArgument(name, value);
        return name;
    }

    public String addArgument(RandomValueBuilder value)
    {
        final String name = getNextArgumentName();
        setArgument(name, value);
        return name;
    }

    public String addArgument(Primitive value)
    {
        final String name = getNextArgumentName();
        setArgument(name, value);
        return name;
    }

    ///////////////////////////////////////////////////////////////////////////
    // For Hash-based syntax

    public void setArgument(String name, int value)
    {
        if (null == name)
            throw new NullPointerException();

        final Argument arg = new Argument(name, Argument.Kind.IMM, value);
        checkValidArgument(arg);
        putArgument(arg);
    }

    public void setArgument(String name, RandomValueBuilder value)
    {
        if (null == name)
            throw new NullPointerException();

        if (null == value)
            throw new NullPointerException();

        final Argument arg = 
            new Argument(name, Argument.Kind.IMM_RANDOM, value.build());

        checkValidArgument(arg);
        putArgument(arg);
    }

    public void setArgument(String name, Primitive value)
    {
        if (null == name)
            throw new NullPointerException();

        if (null == value)
            throw new NullPointerException();

        if ((value.getKind() != Primitive.Kind.MODE) &&
            (value.getKind() != Primitive.Kind.OP))
        {
            throw new IllegalArgumentException(
                "Unsupported primitive kind: " + value.getKind());
        }

        final Argument.Kind kind = value.getKind() == Primitive.Kind.MODE ?
            Argument.Kind.MODE : Argument.Kind.OP;

        final Argument arg = new Argument(name, kind, value);
        checkValidArgument(arg);
        putArgument(arg);
    }

    private String getName()
    {
        return strategy.getName();
    }

    private String getNextArgumentName()
    {
        return strategy.getNextArgumentName();
    }

    private void checkValidArgument(Argument arg)
    {
        strategy.checkValidArgument(arg);
    }

    private void checkAllArgumentsSet(Set<String> argNames)
    {
        strategy.checkAllArgumentsAssigned(argNames); 
    }
    
    private static final String ERR_UNASSIGNED_ARGUMENT = 
        "The %s argument of %s is not assigned.";

    private static final String ERR_NO_MORE_ARGUMENTS = 
        "Too many arguments: %s has only %d arguments.";

    private static final String ERR_UNDEFINED_ARGUMENT =
        "The %s argument is not defined for %s.";
    
    private static final String ERR_TYPE_NOT_ACCEPTED =
        "The %s type is not accepted for the %s argument of %s.";
    
    private static final String ERR_ARG_NOT_PRIMITIVE =
        "The %s argument of %s is not a primitive (OP or MODE).";

    private static final class StrategyInstruction implements Strategy
    {
        private final MetaInstruction metaData;

        private int argumentCount;
        private final Iterator<MetaArgument> argumentIterator;

        private StrategyInstruction(MetaInstruction metaData)
        {
            if (null == metaData)
                throw new NullPointerException();

            this.metaData = metaData;

            this.argumentCount = 0;
            this.argumentIterator = metaData.getArguments().iterator();
        }

        @Override
        public String getName()
        {
            return metaData.getName();
        }

        @Override
        public String getDescription()
        {
            return String.format("the %s instruction", getName());
        }

        @Override
        public String getNextArgumentName()
        {
            if (!argumentIterator.hasNext())
                throw new IllegalStateException(String.format(
                    ERR_NO_MORE_ARGUMENTS, getDescription(), argumentCount));

            final MetaArgument argument = argumentIterator.next();
            argumentCount++;

            return argument.getName();
        }

        @Override
        public void checkValidArgument(Argument arg)
        {
            final MetaArgument metaArgument =
                metaData.getArgument(arg.getName());

            if (null == metaArgument)
                throw new IllegalStateException(String.format(
                    ERR_UNDEFINED_ARGUMENT, arg.getName(), getDescription()));

            final String typeName;
            if (arg.isImmediate())
            {
                typeName = AddressingModeImm.NAME;
            }
            else
            {
                if (!(arg.getValue() instanceof Primitive))
                    throw new IllegalArgumentException(String.format(
                        ERR_ARG_NOT_PRIMITIVE,
                        arg.getName(),
                        getDescription()));

                typeName = ((Primitive) arg.getValue()).getName();
            }

            if (!metaArgument.isTypeAccepted(typeName))
                throw new IllegalStateException(String.format(
                    ERR_TYPE_NOT_ACCEPTED,
                    typeName,
                    arg.getName(),
                    getDescription()));
        }

        @Override
        public void checkAllArgumentsAssigned(Set<String> argNames)
        {
            for (MetaArgument arg : metaData.getArguments())
            {
                if (!argNames.contains(arg.getName()))
                    throw new IllegalStateException(String.format(
                         ERR_UNASSIGNED_ARGUMENT,
                         arg.getName(),
                         getDescription()));
            }
        }
    }

    private static final class StrategyOperation implements Strategy
    {
        private final MetaOperation metaData;
        private final String contextName;

        private int argumentCount;
        private final Iterator<MetaArgument> argumentIterator;

        StrategyOperation(MetaOperation metaData, String contextName)
        {
            if (null == metaData)
                throw new NullPointerException();

            this.metaData = metaData;
            this.contextName = contextName;

            this.argumentCount = 0;
            this.argumentIterator = metaData.getArguments().iterator();
        }

        @Override
        public String getName()
        {
            return metaData.getName();
        }

        @Override
        public String getDescription()
        {
            final String basicDescription =
               String.format("the %s operation", getName());

            if (null == contextName)
                return basicDescription;

            return String.format("%s (shortcut for context %s)",
                basicDescription, contextName);
        }

        @Override
        public String getNextArgumentName()
        {
            if (!argumentIterator.hasNext())
                throw new IllegalStateException(String.format(
                    ERR_NO_MORE_ARGUMENTS, getDescription(), argumentCount));

            final MetaArgument argument = argumentIterator.next();
            argumentCount++;

            return argument.getName();
        }

        @Override
        public void checkValidArgument(Argument arg)
        {
            final MetaArgument metaArgument =
                metaData.getArgument(arg.getName());

            if (null == metaArgument)
                throw new IllegalStateException(String.format(
                    ERR_UNDEFINED_ARGUMENT, arg.getName(), getDescription()));

            final String typeName;
            if (arg.isImmediate())
            {
                typeName = AddressingModeImm.NAME;
            }
            else
            {
                if (!(arg.getValue() instanceof Primitive))
                    throw new IllegalArgumentException(String.format(
                        ERR_ARG_NOT_PRIMITIVE,
                        arg.getName(),
                        getDescription()));
                
                typeName = ((Primitive) arg.getValue()).getName();
            }

            if (!metaArgument.isTypeAccepted(typeName))
                throw new IllegalStateException(String.format(
                    ERR_TYPE_NOT_ACCEPTED,
                    typeName,
                    arg.getName(),
                    getDescription()));
        }

        @Override
        public void checkAllArgumentsAssigned(Set<String> argNames)
        {
            for (MetaArgument arg : metaData.getArguments())
            {
                if (!argNames.contains(arg.getName()))
                    throw new IllegalStateException(String.format(
                        ERR_UNASSIGNED_ARGUMENT,
                        arg.getName(),
                        getDescription()));
            }
        }
    }

    private static final class StrategyAddressingMode implements Strategy
    {
        private static final String ERR_WRONG_ARGUMENT_KIND =
            "The %s argument of %s is %s, but it must be an immediate value.";

        private final MetaAddressingMode metaData;

        private int argumentCount;
        private final Iterator<String> argumentNameIterator;

        StrategyAddressingMode(MetaAddressingMode metaData)
        {
            if (null == metaData)
                throw new NullPointerException();

            this.metaData = metaData;

            this.argumentCount = 0;
            this.argumentNameIterator = metaData.getArgumentNames().iterator();
        }

        @Override
        public String getName()
        {
            return metaData.getName();
        }

        @Override
        public String getDescription()
        {
            return String.format("the %s addressing mode");
        }

        @Override
        public String getNextArgumentName()
        {
            if (!argumentNameIterator.hasNext())
                throw new IllegalStateException(String.format(
                    ERR_NO_MORE_ARGUMENTS, getDescription(), argumentCount));

            final String argumentName = argumentNameIterator.next();
            argumentCount++;

            return argumentName;
        }

        @Override
        public void checkValidArgument(Argument arg)
        {
            if (!metaData.isArgumentDefined(arg.getName()))
                throw new IllegalStateException(String.format(
                    ERR_UNDEFINED_ARGUMENT, arg.getName(), getDescription()));

            if (!arg.isImmediate())
                throw new IllegalStateException(String.format(
                    ERR_WRONG_ARGUMENT_KIND,
                    arg.getName(),
                    getDescription(),
                    arg.getKind()));
        }

        @Override
        public void checkAllArgumentsAssigned(Set<String> argNames)
        {
            for (String argName : metaData.getArgumentNames())
            {
                if (!argNames.contains(argName))
                    throw new IllegalStateException(String.format(
                        ERR_UNASSIGNED_ARGUMENT, argName, getDescription()));
            }
        }
    }
}
