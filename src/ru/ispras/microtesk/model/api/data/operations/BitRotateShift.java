/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitRotateShift.java, Nov 30, 2012 6:13:29 PM Andrei Tatarnikov
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

public final class BitRotateShift implements IBinaryOperator
{
    private final static Set<ETypeID> SUPPORTED_TYPES = EnumSet.of(
        ETypeID.INT,
        ETypeID.CARD
    );

    private final BitVectorMath.Operations unsignedOp;
    private final BitVectorMath.Operations   signedOp;

    public BitRotateShift(BitVectorMath.Operations unsignedOp, BitVectorMath.Operations signedOp)
    {
        if (null == unsignedOp)
            throw new NullPointerException();

        if (unsignedOp.getOperands() != BitVectorMath.Operands.BINARY)
            throw new IllegalArgumentException();

        if (null == signedOp)
            throw new NullPointerException();
        
        if (signedOp.getOperands() != BitVectorMath.Operands.BINARY)
            throw new IllegalArgumentException();

        this.unsignedOp = unsignedOp;
        this.signedOp   = signedOp;
    }

    public BitRotateShift(BitVectorMath.Operations op)
    {
        this(op, op);   
    }

    @Override
    public final Data execute(Data lhs, Data rhs)
    {
        final BitVector result;

        if (lhs.getType().getTypeID() == ETypeID.CARD)
            result = unsignedOp.execute(lhs.getRawData(), rhs.getRawData());
        else
            result = signedOp.execute(lhs.getRawData(), rhs.getRawData());

        return new Data(result, lhs.getType());
    }

    @Override
    public final boolean supports(Type lhs, Type rhs)
    {
        if (!SUPPORTED_TYPES.contains(lhs.getTypeID()))
            return false;

        if (!SUPPORTED_TYPES.contains(rhs.getTypeID()))
            return false;

        // The right operand is too big to be a distance. Distance
        // will be converted to int. If it exceeds the size of int,
        // it will be truncated and we will receive incorrect results.

        if (rhs.getBitSize() > Integer.SIZE)
            return false;

        return true;
    }
}
