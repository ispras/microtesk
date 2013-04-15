/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MemoryBase.java, Nov 1, 2012 1:33:06 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.metadata.IMetaLocationStore;
import ru.ispras.microtesk.model.api.metadata.MetaLocationStore;
import ru.ispras.microtesk.model.api.type.Type;

public class MemoryBase
{
    private final EMemoryKind         kind;
    private final String              name;
    private final List<Location> locations;
    private final Type                type;

    public MemoryBase(EMemoryKind kind, String name, Type type)
    {
        this(kind, name, type, 1);
    }

    public MemoryBase(EMemoryKind kind, String name, Type type, int length)
    {
        this.kind = kind;
        this.name = name;
        this.type = type;
        this.locations = createLocations(type, length);
    }
    
    private static List<Location> createLocations(Type type, int length)
    {
        final ArrayList<Location> result = new ArrayList<Location>();

        for (int index = 0; index < length; ++index)
            result.add(new Location(type));

        return Collections.unmodifiableList(result);
    }
    
    public final EMemoryKind getMemoryKind()
    {
        return kind;
    }
    
    public final String getName()
    {
        return name;
    }
    
    public final IMetaLocationStore getMetaData()
    {
        return new MetaLocationStore(name, getLength());
    }

    public final Type getType()
    {
        return type;
    }

    public final int getLength()
    {
        return locations.size();
    }

    public final Location access(int index)
    {
        return locations.get(index);
    }

    public final Location access()
    {
        return access(0);
    }
    
    public Data load(int index)
    {
        // TODO
        
        return null;
    }
    
    public void store(int index, Data data)
    {
        // TODO
    }
}
