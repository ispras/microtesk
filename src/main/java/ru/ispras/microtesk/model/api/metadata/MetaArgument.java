/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaArgument.java, Nov 15, 2012 2:57:18 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

import java.util.Collection;
import ru.ispras.microtesk.model.api.metadata.IMetaArgument;

/**
* The MetaArgument class implementations the IMetaArgument interface.
* 
* @author Andrei Tatarnikov
*/

public class MetaArgument implements IMetaArgument
{
    private final String name;
    private final Collection<String> typeNames; 

    public MetaArgument(String name, Collection<String> typeNames)
    {
        this.name  = name;
        this.typeNames = typeNames;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Iterable<String> getTypeNames()
    {
        return typeNames;
    }
}
