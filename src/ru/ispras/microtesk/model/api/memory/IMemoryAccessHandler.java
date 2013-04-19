/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IMemoryAccessHandler.java, Nov 9, 2012 6:09:18 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.memory;

import ru.ispras.microtesk.model.api.rawdata.RawData;

public interface IMemoryAccessHandler
{
    public RawData onLoad(IMemory memory, int address);
    public void onStore(IMemory memory, int address, RawData data);
}
