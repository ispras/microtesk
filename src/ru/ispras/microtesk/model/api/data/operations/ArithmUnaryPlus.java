/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArithmUnaryPlus.java, Nov 30, 2012 2:43:43 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IUnaryOperator;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

public class ArithmUnaryPlus implements IUnaryOperator
{
    // Sim-nML spec: these operators (unary +,-) are used only for
    // INT, FLOAT and FIX data types.

    private final static Set<ETypeID> SUPPORTED_TYPES = Collections.unmodifiableSet(EnumSet.of(
        ETypeID.INT
        //, ETypeID.FLOAT // NOT SUPPORTED IN THIS VERSION
        //, ETypeID.FIX   // NOT SUPPORTED IN THIS VERSION
    ));

    @Override
    public Data execute(Data arg)
    {
        // Unary plus does nothing. 
        return arg;
    }

    @Override
    public boolean supports(Type argType)
    {
        return SUPPORTED_TYPES.contains(argType.getTypeID());
    }
}
