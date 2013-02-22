/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaSituation.java, Nov 15, 2012 3:28:10 PM Andrei Tatarnikov
 */ 

package ru.ispras.microtesk.model.api.metadata;

import java.util.ArrayList;
import java.util.Collection;
import ru.ispras.microtesk.model.api.metadata.IMetaArgument;
import ru.ispras.microtesk.model.api.metadata.IMetaAttribute;
import ru.ispras.microtesk.model.api.metadata.IMetaSituation;

/**
* The MetaSituation class implementations the IMetaSituation interface.
* 
* @author Andrei Tatarnikov
*/

public class MetaSituation implements IMetaSituation
{
    private final String                      name;
    private final Collection<IMetaArgument>   args;
    private final Collection<IMetaAttribute> attrs;

    public MetaSituation(String name, Collection<IMetaArgument> args, Collection<IMetaAttribute> attrs)
    {
        this.name  = name;
        this.args  = args;
        this.attrs = attrs;
    }

    public MetaSituation(String name, Collection<IMetaArgument> args)
    {
        this(name, args, new ArrayList<IMetaAttribute>());
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Iterable<IMetaArgument> getArguments()
    {
        return args;
    }

    @Override
    public Iterable<IMetaAttribute> getAttributes()
    {
        return attrs;
    }
}
