/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitOr.java, Nov 30, 2012 6:01:07 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import ru.ispras.formula.data.types.bitvector.BitVectorAlgorithm.IBinaryOperation;

public class BitOr extends BitAndOrXorBase
{
    private final static class OrOp implements IBinaryOperation
    {
        @Override
        public byte run(byte lhs, byte rhs) { return (byte) (lhs | rhs); }
    };

    public BitOr()
    {
        super(new OrOp());
    }
}

