/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaOperation.java, Jun 23, 2014 4:22:40 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.metadata;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * The MetaOperation class stores information on the given operation.
 * 
 * @author Andrei Tatarnikov
 */

public final class MetaOperation implements MetaData
{
    private final String name;
    private final Collection<MetaArgument> args;
    private final Map<String, MetaShortcut> shortcuts;

    public MetaOperation(
        String name,
        Collection<MetaArgument> args
        )
    {
        this(name, args, Collections.<String, MetaShortcut>emptyMap());
    }

    public MetaOperation(
        String name,
        Collection<MetaArgument> args,
        Map<String, MetaShortcut> shortcuts
        )
    {
        if (null == name)
            throw new NullPointerException();

        if (null == args)
            throw new NullPointerException();

        if (null == shortcuts)
            throw new NullPointerException();

        this.name = name;
        this.args = args;
        this.shortcuts = shortcuts;
    }

    /**
     * Returns the operation name.
     * 
     * @return The operation name.
     */

    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Returns an Iterable object for the collection of operation arguments. 
     * 
     * @return Iterable object.
     */

    public Iterable<MetaArgument> getArguments()
    {
        return args;
    }

    /**
     * Returns a collection of shortcuts applicable to the given
     * operation in different contexts.
     * 
     * @return A collection of shortcuts.
     */

    public Iterable<MetaShortcut> getShortcuts()
    {
        return shortcuts.values();
    }

    /**
     * Returns a shortcut for the given operation that can be used 
     * in the specified context.
     * 
     * @param contextName Context name.
     * @return Shortcut for the given context or {@code null} if no
     * such shortcut exists.
     */

    public MetaShortcut getShortcut(String contextName)
    {
        return shortcuts.get(contextName);
    }
}
