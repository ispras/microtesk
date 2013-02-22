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

import ru.ispras.microtesk.model.api.rawdata.RawDataAlgorithm.IBinaryOperation;

public class BitAnd extends BitAndOrXorBase
{
    private final static class AndOp implements IBinaryOperation
    {
        @Override
        public char run(char lhs, char rhs) { return (char) (lhs & rhs); }
    };
    
    public BitAnd()
    {
        super(new AndOp());
    }
}
