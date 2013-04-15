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
    public RawData onLoad(IMemoryArray memory, int index);
    public void onStore(IMemoryArray memory, int index, RawData data);
}
