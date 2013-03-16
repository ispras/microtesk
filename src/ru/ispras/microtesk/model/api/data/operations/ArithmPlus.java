/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArithmPlus.java, Dec 1, 2012 9:34:51 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import static ru.ispras.microtesk.model.api.rawdata.RawDataAlgorithm.transform;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IBinaryOperator;
import ru.ispras.microtesk.model.api.rawdata.RawData;
import ru.ispras.microtesk.model.api.rawdata.RawDataAlgorithm.IBinaryOperation;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

/*
 * TODO: This implementation should be reviewed and rewritten in accordance
 * with the Sim-nML specification (see Kanpur theses).
 * 
 * The current implementation of ArithmPlus has the following restrictions:
 * 
 * 1. Operands are assumed to have the same type (and size).
 * 2. In the case of overflow (when we are adding two big values) data is truncated.
 * According to Sim-nML specification, the resulting data should be extended with additional bits.
 */

public class ArithmPlus implements IBinaryOperator
{
    private final static Set<ETypeID> SUPPORTED_TYPES = Collections.unmodifiableSet(EnumSet.of(
	     ETypeID.INT,
	     ETypeID.CARD
	     //, ETypeID.FLOAT // NOT SUPPORTED IN THIS VERSION
	     //, ETypeID.FIX   // NOT SUPPORTED IN THIS VERSION
	));
    
    protected static Data plus(Data left, Data right)
    {
        assert left.getType().equals(right.getType()) : "Restriction: types (and sizes) should match.";

        final IBinaryOperation op = new IBinaryOperation()
        {
            private char remainder = 0;

            @Override
            public char run(char lhs, char rhs)
            { 
                final int retvalue = lhs + rhs + remainder;
                remainder = (char)(retvalue >>> RawData.BITS_IN_BYTE);
                return (char) retvalue;
            }
        };

        final Data result = new Data(left.getType());
        transform(left.getRawData(), right.getRawData(), result.getRawData(), op);

        return result;
    }

    @Override
    public Data execute(Data left, Data right)
    {
        return plus(left, right);
    }

    @Override
    public boolean supports(Type left, Type right)
    {
        if (!SUPPORTED_TYPES.contains(left.getTypeID()))
            return false;

        if (!SUPPORTED_TYPES.contains(right.getTypeID()))
            return false;

        // Restriction of the current version: type and size should match.
        final boolean equalSize = 
            left.getBitSize() == right.getBitSize();

        if (!equalSize)
            return false;
        
        final boolean equalType =
            left.getTypeID() == right.getTypeID();
        
        if (!equalType)
        {
            final boolean integerTypes =
                (left.getTypeID() == ETypeID.INT && right.getTypeID() == ETypeID.CARD) || 
                (left.getTypeID() == ETypeID.CARD && right.getTypeID() == ETypeID.INT);
            
            if (!integerTypes)
                return false;
        }
        
        return true;
    }
}
