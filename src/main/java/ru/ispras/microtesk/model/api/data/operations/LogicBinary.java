/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LogicBinary.java, Feb 21, 2014 5:25:36 PM Andrei Tatarnikov
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

public final class LogicBinary implements IBinaryOperator
{
    private final static Set<TypeId> SUPPORTED_TYPES = EnumSet.of(
        TypeId.INT,
        TypeId.CARD
    );

    private final BitVectorMath.Operations unsignedOp;
    private final BitVectorMath.Operations   signedOp;

    public LogicBinary(BitVectorMath.Operations unsignedOp, BitVectorMath.Operations signedOp)
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

    public LogicBinary(BitVectorMath.Operations op)
    {
        this(op, op);   
    }

    @Override
    public final Data execute(Data lhs, Data rhs)
    {
        final BitVector result;

        if (lhs.getType().getTypeId() == TypeId.CARD)
            result = unsignedOp.execute(lhs.getRawData(), rhs.getRawData());
        else
            result = signedOp.execute(lhs.getRawData(), rhs.getRawData());

        return new Data(result, Type.BOOL(result.getBitSize()));
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
