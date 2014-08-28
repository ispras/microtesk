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
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaInstruction;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.test.template.Primitive.Kind;

public final class PrimitiveBuilder
{
    private interface Strategy
    {
        String getName();
        String getNextArgumentName();
        void checkValidArgument(Argument arg);
        void checkAllArgumentsSet(Set<String> argNames);
    }

    private final Strategy strategy;
    private Kind kind;
    private final Map<String, Argument> args;

    PrimitiveBuilder(MetaInstruction metaData)
    {
        this(new StrategyInstruction(metaData), Kind.INSTR);
    }

    PrimitiveBuilder(MetaOperation metaData)
    {
        this(new StrategyOperation(metaData), Kind.OP);
    }

    PrimitiveBuilder(MetaAddressingMode metaData)
    {
        this(new StrategyAddressingMode(metaData), Kind.MODE);
    }

    private PrimitiveBuilder(Strategy strategy, Kind kind)
    {
        this.strategy = strategy;
        this.kind = kind;
        this.args = new HashMap<String, Argument>();
    }

    private void putArgument(Argument arg)
    {
        args.put(arg.getName(), arg);
    }

    public Primitive build()
    {
        if (null == kind)
            throw new IllegalStateException("Kind is not specified.");
  
        checkAllArgumentsSet(Collections.unmodifiableSet(args.keySet()));
        return new Primitive(kind, getName(), args);
    }

    ///////////////////////////////////////////////////////////////////////////
    // For Array-based syntax

    public void addArgument(int value)
    {
        final String name = getNextArgumentName();
        setArgument(name, value);
    }

    public void addArgument(RandomValue value)
    {
        final String name = getNextArgumentName();
        setArgument(name, value);
    }

    public void addArgument(Primitive value)
    {
        final String name = getNextArgumentName();
        setArgument(name, value);
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

    public void setArgument(String name, RandomValue value)
    {
        if (null == name)
            throw new NullPointerException();

        if (null == value)
            throw new NullPointerException();

        final Argument arg = new Argument(name, Argument.Kind.IMM_RANDOM, value);
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
        strategy.checkAllArgumentsSet(argNames); 
    }

    private static final class StrategyInstruction implements Strategy
    {
        private final MetaInstruction metaData;

        private StrategyInstruction(MetaInstruction metaData)
        {
            if (null == metaData)
                throw new NullPointerException();

            this.metaData = metaData;
        }

        @Override
        public String getName()
        {
            return metaData.getName();
        }

        @Override
        public String getNextArgumentName()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void checkValidArgument(Argument arg)
        {
            // TODO Auto-generated method stub
        }

        @Override
        public void checkAllArgumentsSet(Set<String> argNames)
        {
            // TODO Auto-generated method stub
        }
    }

    private static final class StrategyOperation implements Strategy
    {
        private final MetaOperation metaData;

        StrategyOperation(MetaOperation metaData)
        {
            if (null == metaData)
                throw new NullPointerException();

            this.metaData = metaData;
        }

        @Override
        public String getName()
        {
            return metaData.getName();
        }

        @Override
        public String getNextArgumentName()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void checkValidArgument(Argument arg)
        {
            // TODO Auto-generated method stub
        }

        @Override
        public void checkAllArgumentsSet(Set<String> argNames)
        {
            // TODO Auto-generated method stub
        }
    }

    private static final class StrategyAddressingMode implements Strategy
    {
        private final MetaAddressingMode metaData;

        StrategyAddressingMode(MetaAddressingMode metaData)
        {
            if (null == metaData)
                throw new NullPointerException();

            this.metaData = metaData;
        }

        @Override
        public String getName()
        {
            return metaData.getName();
        }

        @Override
        public String getNextArgumentName()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void checkValidArgument(Argument arg)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void checkAllArgumentsSet(Set<String> argNames)
        {
            // TODO Auto-generated method stub
            
        }
    }
}
