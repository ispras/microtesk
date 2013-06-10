/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * OverflowConstraintFactory.java, May 23, 2013 3:34:45 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.situation.builtin;

import ru.ispras.solver.api.DataFactory;
import ru.ispras.solver.api.interfaces.EDataType;
import ru.ispras.solver.api.interfaces.IDataType;
import ru.ispras.solver.core.syntax.EStandardOperation;
import ru.ispras.solver.core.syntax.ISyntaxElement;
import ru.ispras.solver.core.syntax.Operation;
import ru.ispras.solver.core.syntax.Value;

public abstract class OverflowConstraintFactory implements IConstraintFactory
{
    public static final int BIT_VECTOR_LENGTH = 64;

    public static final IDataType BIT_VECTOR_TYPE; 
    public static final Value     INT_ZERO;
    public static final Value     INT_BASE_SIZE;
    public static final Operation INT_SIGN_MASK;

    static 
    {
        BIT_VECTOR_TYPE = DataFactory.createDataType(EDataType.BIT_VECTOR, BIT_VECTOR_LENGTH);
        INT_ZERO        = new Value(BIT_VECTOR_TYPE.valueOf("0", 10));
        INT_BASE_SIZE   = new Value(BIT_VECTOR_TYPE.valueOf("32", 10));

        INT_SIGN_MASK   = new Operation(
            EStandardOperation.BVLSHL,
            new Operation(EStandardOperation.BVNOT, INT_ZERO),
            INT_BASE_SIZE
            );
    }

    protected final Operation IsValidPos(ISyntaxElement arg)
    {
        return new Operation(
            EStandardOperation.EQ,
            new Operation(EStandardOperation.BVAND, arg, INT_SIGN_MASK),
            INT_ZERO
            );
    }

    protected final Operation IsValidNeg(ISyntaxElement arg)
    {
        return new Operation(
            EStandardOperation.EQ,
            new Operation(EStandardOperation.BVAND, arg, INT_SIGN_MASK),
            INT_SIGN_MASK
            );
    }

    protected final Operation IsValidSignedInt(ISyntaxElement arg)
    {
        return new Operation(
            EStandardOperation.OR,
            IsValidPos(arg),
            IsValidNeg(arg)
            );
    }

    protected final Operation isNotEqual(ISyntaxElement left, ISyntaxElement right)
    {
        return isNot(new Operation(EStandardOperation.EQ, left, right));
    }

    protected final Operation isNot(ISyntaxElement expr)
    {
        return new Operation(EStandardOperation.NOT, expr);
    }
}
