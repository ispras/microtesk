/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitAnd.java, Nov 30, 2012 5:27:20 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import ru.ispras.formula.data.types.bitvector.BitVectorAlgorithm.IBinaryOperation;

public class BitAnd extends BitAndOrXorBase
{
    private final static class AndOp implements IBinaryOperation
    {
        @Override
        public byte run(byte lhs, byte rhs) { return (byte) (lhs & rhs); }
    };
    
    public BitAnd()
    {
        super(new AndOp());
    }
}
