/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IValueConverter.java, Nov 30, 2012 10:47:07 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data;

import ru.ispras.fortress.data.types.Radix;
import ru.ispras.microtesk.model.api.type.Type;

public interface IValueConverter
{
    public Data fromLong(Type type, long value);
    public long toLong(Data data);

    public Data fromInt(Type type, int value);
    public int toInt(Data data);
    
    public Data fromString(Type type, String value, Radix radix);
}
