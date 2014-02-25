/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaAddressingMode.java, Nov 15, 2012 2:47:49 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

import java.util.Collection;
import ru.ispras.microtesk.model.api.metadata.IMetaAddressingMode;

/**
* The MetaAddressingMode class implementations the IMetaAddressingMode interface.
* 
* @author Andrei Tatarnikov
*/

public class MetaAddressingMode implements IMetaAddressingMode
{
    private final String name;
    private final Collection<String> argumentNames; 
    
    public MetaAddressingMode(String name, Collection<String> argumentNames)
    {
        this.name = name;
        this.argumentNames = argumentNames;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Iterable<String> getArgumentNames()
    {
        return argumentNames;
    }
}
