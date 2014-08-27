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

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.test.template.Primitive.Kind;

public abstract class PrimitiveBuilder
{
    private Kind kind;
    private String name;
    private final Map<String, Argument> args;

    PrimitiveBuilder()
    {
        this(null, null);
    }

    PrimitiveBuilder(Kind kind, String name)
    {
        this.kind = kind;
        this.name = name;
        this.args = new HashMap<String, Argument>();
    }

    private void putArgument(Argument arg)
    {
        args.put(arg.getName(), arg);
    }

    public final Primitive build()
    {
        return new Primitive(kind, name, args);
    }

    public final void setKind(Kind kind)
    {
        this.kind = kind;
    }

    public final void setName(String name)
    {
        this.name = name;
    }

    ///////////////////////////////////////////////////////////////////////////
    // For Array-based syntax

    public final void addArgument(int value)
    {
        final String name = getNextArgumentName();
        setArgument(name, value);
    }

    public final void addArgument(RandomValue value)
    {
        final String name = getNextArgumentName();
        setArgument(name, value);
    }

    public final void addArgument(Primitive value)
    {
        final String name = getNextArgumentName();
        setArgument(name, value);
    }

    ///////////////////////////////////////////////////////////////////////////
    // For Hash-based syntax

    public final void setArgument(String name, int value)
    {
        if (null == name)
            throw new NullPointerException();

        final Argument arg = new Argument(name, Argument.Kind.IMM, value);
        checkValidArgument(arg);
        putArgument(arg);
    }

    public final void setArgument(String name, RandomValue value)
    {
        if (null == name)
            throw new NullPointerException();

        if (null == value)
            throw new NullPointerException();

        final Argument arg = new Argument(name, Argument.Kind.IMM_RANDOM, value);
        checkValidArgument(arg);
        putArgument(arg);
    }

    public final void setArgument(String name, Primitive value)
    {
        if (null == name)
            throw new NullPointerException();

        if (null == value)
            throw new NullPointerException();

        if ((value.getKind() != Primitive.Kind.MODE) &&
            (value.getKind() != Primitive.Kind.OP))
        {
            throw new IllegalArgumentException(
                "Unknown kind: " + value.getKind());
        }

        final Argument.Kind kind = value.getKind() == Primitive.Kind.MODE ?
            Argument.Kind.MODE : Argument.Kind.OP;

        final Argument arg = new Argument(name, kind, value);
        checkValidArgument(arg);
        putArgument(arg);
    }

    public abstract String getNextArgumentName();
    public abstract void checkValidArgument(Argument arg);
}
