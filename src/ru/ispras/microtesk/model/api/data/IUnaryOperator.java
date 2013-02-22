/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IUnaryOperator.java, Nov 9, 2012 3:24:24 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data;

import ru.ispras.microtesk.model.api.type.Type;

public interface IUnaryOperator
{
    public Data execute(Data arg);
    public boolean supports(Type argType);
}
