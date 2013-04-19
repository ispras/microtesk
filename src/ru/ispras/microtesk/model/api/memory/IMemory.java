/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IMemory.java, Apr 15, 2013 4:57:37 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.memory;

import ru.ispras.microtesk.model.api.rawdata.RawData;

public interface IMemory
{
    public int sizeInBytes();
    public RawData read(int address);
    public void write(int address, RawData data);
}
