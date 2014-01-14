/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitXor.java, Nov 30, 2012 6:02:21 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import ru.ispras.fortress.data.types.bitvector.BitVectorAlgorithm.IBinaryOperation;

public class BitXor extends BitAndOrXorBase
{
    private final static class XorOp implements IBinaryOperation
    {
        @Override
        public byte run(byte lhs, byte rhs) { return (byte) (lhs ^ rhs); }
    };

    public BitXor()
    {
        super(new XorOp());
    }
}
