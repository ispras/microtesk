/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitNot.java, Nov 30, 2012 3:46:12 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IUnaryOperator;
import ru.ispras.formula.data.types.bitvector.BitVectorAlgorithm.IUnaryOperation;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

import static ru.ispras.formula.data.types.bitvector.BitVectorAlgorithm.transform;

public final class BitNot implements IUnaryOperator
{
    private final static Set<ETypeID> SUPPORTED_TYPES = Collections.unmodifiableSet(EnumSet.of(
         ETypeID.INT,
         ETypeID.CARD
    ));

    protected static Data bitnot(Data arg)
    {
        final Data result = new Data(arg);

        final IUnaryOperation op = new IUnaryOperation()
        {
            @Override
            public byte run(byte v) { return (byte) ~v; }
        };

        transform(arg.getRawData(), result.getRawData(), op);
        return result;
    }

    @Override
    public Data execute(Data arg)
    {
        return bitnot(arg);
    }

    @Override
    public boolean supports(Type argType)
    {
        return SUPPORTED_TYPES.contains(argType.getTypeID());
    }
}
