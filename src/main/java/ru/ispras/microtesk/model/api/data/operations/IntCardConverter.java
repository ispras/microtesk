/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IntCardConverter.java, Nov 30, 2012 8:14:29 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.ERadix;
import ru.ispras.microtesk.model.api.data.IValueConverter;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.type.Type;

public class IntCardConverter implements IValueConverter
{
    @Override
    public Data fromLong(Type type, long value)
    {
        final BitVector rawData = BitVector.valueOf(value, type.getBitSize());
        return new Data(rawData, type);
    }

    @Override
    public long toLong(Data data)
    {
        assert data.getRawData().getBitSize() <= Long.SIZE;
        return data.getRawData().longValue();
    }

    @Override
    public Data fromInt(Type type, int value)
    {
        final BitVector rawData = BitVector.valueOf(value, type.getBitSize());
        return new Data(rawData, type);
    }

    @Override
    public int toInt(Data data)
    {
        assert data.getRawData().getBitSize() <= Integer.SIZE;
        return data.getRawData().intValue();
    }
    
    @Override
    public Data fromString(Type type, String value, ERadix radix)
    {
        assert false : "NOT IMPLEMENTED";
        return null;
    }
}