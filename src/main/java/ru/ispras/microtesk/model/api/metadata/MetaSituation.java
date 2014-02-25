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

import ru.ispras.microtesk.model.api.metadata.IMetaSituation;

/**
* The MetaSituation class implementations the IMetaSituation interface.
* 
* @author Andrei Tatarnikov
*/

public class MetaSituation implements IMetaSituation
{
    private final String name;

    public MetaSituation(String name)
    {
        this.name  = name;
    }

    @Override
    public String getName()
    {
        return name;
    }
}
