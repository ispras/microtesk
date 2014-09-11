/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitUnary.java, Feb 21, 2014 2:43:17 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import java.util.EnumSet;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorMath;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IUnaryOperator;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

public final class BitUnary implements IUnaryOperator
{
    private final static Set<ETypeID> SUPPORTED_TYPES = EnumSet.of(
        ETypeID.INT,
        ETypeID.CARD
    );

    private final BitVectorMath.Operations op;

    public BitUnary(BitVectorMath.Operations op)
    {
        if (null == op)
            throw new NullPointerException();

        if (op.getOperands() != BitVectorMath.Operands.UNARY)
            throw new IllegalArgumentException();

        this.op = op;
    }

    @Override
    public Data execute(Data data)
    {
        final BitVector result = op.execute(data.getRawData());
        return new Data(result, data.getType());
    }

    @Override
    public boolean supports(Type type)
    {
        return SUPPORTED_TYPES.contains(type.getTypeId());
    }
}
