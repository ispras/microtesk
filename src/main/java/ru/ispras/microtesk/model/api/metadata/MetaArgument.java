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
import ru.ispras.microtesk.model.api.metadata.IMetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.IMetaArgument;

/**
* The MetaArgument class implementations the IMetaArgument interface.
* 
* @author Andrei Tatarnikov
*/

public class MetaArgument implements IMetaArgument
{
    private final String name;
    private final Collection<IMetaAddressingMode> modes; 
    
    public MetaArgument(String name, Collection<IMetaAddressingMode> modes)
    {
        this.name  = name;
        this.modes = modes;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Iterable<IMetaAddressingMode> getAddressingModes()
    {
        return modes;
    }
}