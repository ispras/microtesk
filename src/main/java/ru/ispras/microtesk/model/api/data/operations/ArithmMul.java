/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArithmMinus.java, Mar 17, 2013, 1:10:12 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IBinaryOperator;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

public class ArithmMul implements IBinaryOperator
{
    private final static Set<ETypeID> SUPPORTED_TYPES = Collections.unmodifiableSet(EnumSet.of(
        ETypeID.INT,
        ETypeID.CARD
        //, ETypeID.FLOAT // NOT SUPPORTED IN THIS VERSION
        //, ETypeID.FIX   // NOT SUPPORTED IN THIS VERSION
    ));
 
    @Override
    public Data execute(Data left, Data right)
    {
        assert left.getType().equals(right.getType()) :
            "Restriction: types (and sizes) should match.";

        final int result = left.getRawData().intValue() * right.getRawData().intValue();
        final Type resultType = left.getType();

        return new Data(
            BitVector.valueOf(result, resultType.getBitSize()),
            resultType
            );
    }

    @Override
    public boolean supports(Type left, Type right)
    {
        if (!SUPPORTED_TYPES.contains(left.getTypeId()))
            return false;

        if (!SUPPORTED_TYPES.contains(right.getTypeId()))
            return false;

        // Restriction of the current version: type and size should match.
        final boolean equalSize = 
            left.getBitSize() == right.getBitSize();

        if (!equalSize)
            return false;
        
        final boolean equalType =
            left.getTypeId() == right.getTypeId();
        
        if (!equalType)
        {
            final boolean integerTypes =
                (left.getTypeId() == ETypeID.INT && right.getTypeId() == ETypeID.CARD) || 
                (left.getTypeId() == ETypeID.CARD && right.getTypeId() == ETypeID.INT);
            
            if (!integerTypes)
                return false;
        }

        return true;
    }
}
