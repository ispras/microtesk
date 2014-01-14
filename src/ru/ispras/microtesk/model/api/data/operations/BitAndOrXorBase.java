/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitAndOrXorBase.java, Nov 30, 2012 5:56:17 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IBinaryOperator;
import ru.ispras.fortress.data.types.bitvector.BitVectorAlgorithm.IBinaryOperation;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

import static ru.ispras.fortress.data.types.bitvector.BitVectorAlgorithm.transform;

public abstract class BitAndOrXorBase implements IBinaryOperator
{
    private final static Set<ETypeID> SUPPORTED_TYPES = Collections.unmodifiableSet(EnumSet.of(
        ETypeID.INT,
        ETypeID.CARD
    ));
    
    public final IBinaryOperation op;
    
    protected BitAndOrXorBase(IBinaryOperation op)
    {
        assert null != op;
        this.op = op;        
    }

    private static Type getResultType(Type left, Type right)
    {
        // result type is INT if one of the parameters is INT.

        assert left.getBitSize() == right.getBitSize() : "RESTRICTION: equal size";

        if (right.getTypeID() == ETypeID.INT)
            return right;

        return left;
    }    

    @Override
    public final Data execute(Data left, Data right)
    {
        final Type resultType = getResultType(left.getType(), right.getType());
        final Data     result = new Data(resultType);

        transform(left.getRawData(), right.getRawData(), result.getRawData(), op);
        return result;
    }

    @Override
    public final boolean supports(Type left, Type right)
    {
        if (!SUPPORTED_TYPES.contains(left.getTypeID()))
            return false;

        if (!SUPPORTED_TYPES.contains(right.getTypeID()))
            return false;

        // Restriction of the current version: size should be equal
        final boolean sameSize = left.getBitSize() == right.getBitSize();
        if (!sameSize)
            return false;

        return true;
    }
}
