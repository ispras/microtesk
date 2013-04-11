/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Label.java, Apr 11, 2013 4:47:19 PM Andrei Tatarnikov
 */


package ru.ispras.microtesk.model.api.memory;

public final class Label
{
    private final String name;
    private final MemoryBase memory;
    private final int index;

    public Label(String name, MemoryBase memory)
    {
        this(name, memory, 0);
    }

    public Label(String name, MemoryBase memory, int index)
    {
        this.name   = name;
        this.memory = memory;
        this.index  = index;
    }

    public String getName()
    {
        return name;
    }

    public Location access()
    {
        return memory.access(index);
    }
}
