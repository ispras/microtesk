/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Location.java, Sep 30, 2014 5:27:07 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.test.preparator;

import ru.ispras.microtesk.model.api.memory.MemoryKind;

public final class Location
{
    private final String name;
    private final MemoryKind kind;
    private final int index;

    public Location(String name, MemoryKind kind)
    {
        this(name, kind, 0);
    }

    public Location(String name, MemoryKind kind, int index)
    {
        if (null == name)
            throw new NullPointerException();

        if (null == kind)
            throw new NullPointerException();

        if (index < 0)
            throw new IllegalArgumentException();

        this.name = name;
        this.kind = kind;
        this.index = index;
    }

    public String getName()
    {
        return name;
    }

    public MemoryKind getKind()
    {
        return kind;
    }

    public int getIndex()
    {
        return index;
    }
}
