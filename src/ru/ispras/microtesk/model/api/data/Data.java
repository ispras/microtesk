/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Data.java, Nov 28, 2012 4:37:01 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data;

import ru.ispras.microtesk.model.api.rawdata.RawData;
import ru.ispras.microtesk.model.api.rawdata.RawDataStore;
import ru.ispras.microtesk.model.api.type.Type;

public final class Data
{
    private RawData rawData;
    private Type       type;

    public Data(RawData rawData, Type type)
    {
        this.rawData = rawData;
        this.type = type;
    }
    
    public Data(Data data)
    {
        this.rawData = new RawDataStore(data.getRawData());
        this.type    = data.getType();
    }
    
    public Data(Type type)
    {
        this.rawData = new RawDataStore(type.getBitSize());
        this.type    = type;
    }

    public RawData getRawData()
    {
        return rawData;
    }

    public Type getType()
    {
        return type;
    }
}
