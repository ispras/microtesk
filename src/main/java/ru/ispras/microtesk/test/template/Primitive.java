/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Primitive.java, Aug 26, 2014 8:11:50 PM Andrei Tatarnikov
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

import java.util.Map;

public final class Primitive
{
    public static enum Kind
    {
        OP,
        MODE,
        INSTR
    };

    private final Kind kind;
    private final String name;
    private final String typeName;
    private final boolean isRoot;
    private final Map<String, Argument> args;
    private final String contextName;

    Primitive(
        Kind kind,
        String name,
        String typeName,
        boolean isRoot,
        Map<String, Argument> args,
        String contextName
        )
    {
        if (null == kind)
            throw new NullPointerException();

        if (null == name)
            throw new NullPointerException();
        
        if (null == typeName)
            throw new NullPointerException();

        if (null == args)
            throw new NullPointerException();

        this.kind = kind;
        this.name = name;
        this.typeName = typeName;
        this.isRoot = isRoot;
        this.args = args;
        this.contextName = contextName;
    }

    public Kind getKind()
    {
        return kind;
    }

    public String getName()
    {
        return name;
    }

    public String getTypeName()
    {
        return typeName; 
    }

    public boolean isRoot()
    {
        return isRoot;
    }

    public Map<String, Argument> getArguments()
    {
        return args;
    }

    public String getContextName()
    {
        return contextName;
    }
}
