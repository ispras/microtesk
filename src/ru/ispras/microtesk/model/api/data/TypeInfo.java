/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TypeInfo.java, Nov 28, 2012 4:37:29 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data;

import ru.ispras.microtesk.model.api.type.ETypeID;

public @interface TypeInfo
{
    ETypeID typeID();
    int bitSize();
}
