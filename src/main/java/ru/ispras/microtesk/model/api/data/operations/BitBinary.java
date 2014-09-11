/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitBinary.java, Feb 21, 2014 2:01:52 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import java.util.EnumSet;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorMath;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IBinaryOperator;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

public final class BitBinary implements IBinaryOperator
{
    private final static Set<ETypeID> SUPPORTED_TYPES = EnumSet.of(
        ETypeID.INT,
        ETypeID.CARD
    );

    private final BitVectorMath.Operations op;

    public BitBinary(BitVectorMath.Operations op)
    {
        if (null == op)
            throw new NullPointerException();

        if (op.getOperands() != BitVectorMath.Operands.BINARY)
            throw new IllegalArgumentException();

        this.op = op;
    }

    private static Type getResultType(Type lhs, Type rhs)
    {
        // result type is INT if one of the parameters is INT.

        if (rhs.getTypeId() == ETypeID.INT)
            return rhs;

        return lhs;
    }    

    @Override
    public final Data execute(Data lhs, Data rhs)
    {
        final Type  resultType = getResultType(lhs.getType(), rhs.getType());
        final BitVector result = op.execute(lhs.getRawData(), rhs.getRawData());

        return new Data(result, resultType);
    }

    @Override
    public boolean supports(Type lhs, Type rhs)
    {
        if (!SUPPORTED_TYPES.contains(lhs.getTypeId()))
            return false;

        if (!SUPPORTED_TYPES.contains(rhs.getTypeId()))
            return false;

        // Restriction of the current version: size should be equal
        if (lhs.getBitSize() != rhs.getBitSize())
            return false;

        return true;
    }
}
