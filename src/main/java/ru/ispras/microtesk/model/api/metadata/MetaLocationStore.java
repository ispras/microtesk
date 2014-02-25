/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaLocationStore.java, Nov 15, 2012 2:53:11 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

import ru.ispras.microtesk.model.api.metadata.IMetaLocationStore;

/**
* The MetaLocationStore class implementations the IMetaLocationStore interface.
* 
* @author Andrei Tatarnikov
*/

public class MetaLocationStore implements IMetaLocationStore
{
    private final String name;
    private final int count;
    
    public MetaLocationStore(String name, int count)
    {
        this.name  = name;
        this.count = count;
    }
    
    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getCount()
    {
        return count;
    }
}
