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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.type.Type;

public final class Data
{
    private BitVector rawData;
    private Type       type;

    public Data(BitVector rawData, Type type)
    {
        this.rawData = rawData;
        this.type = type;
    }

    public Data(Data data)
    {
        this.rawData = data.getRawData().createCopy();
        this.type    = data.getType();
    }

    public Data(Type type)
    {
        this.rawData = BitVector.createEmpty(type.getBitSize());
        this.type    = type;
    }

    public BitVector getRawData()
    {
        return rawData;
    }

    public Type getType()
    {
        return type;
    }
}
