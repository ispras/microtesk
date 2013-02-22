/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaAttribute.java, Nov 15, 2012 3:25:42 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

import ru.ispras.microtesk.model.api.metadata.EMetaAttributeType;
import ru.ispras.microtesk.model.api.metadata.IMetaAttribute;

/**
* The MetaAttribute class implementations the IMetaAttribute interface.
* 
* @author Andrei Tatarnikov
*/

public class MetaAttribute implements IMetaAttribute
{
    private final String name;
    private final EMetaAttributeType type;

    public MetaAttribute(String name, EMetaAttributeType type)
    {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public EMetaAttributeType getType()
    {
        return type;
    }
}
