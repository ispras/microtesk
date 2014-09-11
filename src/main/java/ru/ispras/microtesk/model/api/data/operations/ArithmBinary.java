/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArithmBinary.java, Feb 21, 2014 4:18:21 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import java.util.EnumSet;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorMath;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IBinaryOperator;
import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.model.api.type.Type;

/*
 * TODO: This implementation should be reviewed and rewritten in accordance
 * with the Sim-nML specification (see Kanpur theses).
 * 
 * The current implementation of ArithmPlus has the following restrictions:
 * 
 * 1. Operands are assumed to have the same type (and size).
 * 2. In the case of overflow (when we are working with two big values) data is truncated.
 * According to Sim-nML specification, the resulting data might be extended with additional bits.
 */

public final class ArithmBinary implements IBinaryOperator
{
    private final static Set<TypeId> SUPPORTED_TYPES = EnumSet.of(
        TypeId.INT,
        TypeId.CARD
        //, ETypeID.FLOAT // NOT SUPPORTED IN THIS VERSION
        //, ETypeID.FIX   // NOT SUPPORTED IN THIS VERSION
    );

    private final BitVectorMath.Operations op;

    public ArithmBinary(BitVectorMath.Operations op)
    {
        if (null == op)
            throw new NullPointerException();

        if (op.getOperands() != BitVectorMath.Operands.BINARY)
            throw new IllegalArgumentException();

        this.op = op;
    }

    private static Type getResultType(Type left, Type right)
    {
        // result type is INT if one of the parameters is INT.

        if (right.getTypeId() == TypeId.INT)
            return right;

        return left;
    }

    @Override
    public final Data execute(Data left, Data right)
    {
        final Type  resultType = getResultType(left.getType(), right.getType());
        final BitVector result = op.execute(left.getRawData(), right.getRawData());

        return new Data(result, resultType);
    }

    @Override
    public boolean supports(Type left, Type right)
    {
        if (!SUPPORTED_TYPES.contains(left.getTypeId()))
            return false;

        if (!SUPPORTED_TYPES.contains(right.getTypeId()))
            return false;

        // Restriction of the current version: type and size should match.
        if (left.getBitSize() != right.getBitSize())
            return false;

        return true;
    }
}
