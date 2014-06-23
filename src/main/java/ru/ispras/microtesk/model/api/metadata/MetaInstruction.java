/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaInstruction.java, Nov 15, 2012 3:02:03 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

import java.util.Collection;

import ru.ispras.microtesk.model.api.metadata.IMetaInstruction;

/**
* The MetaInstruction class implementations the IMetaInstruction interface.
* 
* @author Andrei Tatarnikov
*/

public class MetaInstruction implements IMetaInstruction
{
    private final String name;
    private final Collection<MetaArgument> args;
    private final Collection<MetaSituation> situations;

    public MetaInstruction(
        String name,
        Collection<MetaArgument> args,
        Collection<MetaSituation> situations
        )
    {
        this.name = name;
        this.args = args;
        this.situations = situations;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Iterable<MetaArgument> getArguments()
    {
        return args;
    }

    @Override
    public final Iterable<MetaSituation> getSituations()
    {
        return situations;
    }
}
