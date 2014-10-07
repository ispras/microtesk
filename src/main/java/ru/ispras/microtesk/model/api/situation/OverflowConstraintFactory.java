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

package ru.ispras.microtesk.model.api.situation;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.microtesk.model.api.situation.IConstraintFactory;

public abstract class OverflowConstraintFactory implements IConstraintFactory
{
    public static final int BIT_VECTOR_LENGTH = 64;

    public static final DataType BIT_VECTOR_TYPE; 
    public static final NodeValue INT_ZERO;
    public static final NodeValue INT_BASE_SIZE;
    public static final NodeOperation INT_SIGN_MASK;

    static 
    {
        BIT_VECTOR_TYPE = DataType.BIT_VECTOR(BIT_VECTOR_LENGTH);
        INT_ZERO = new NodeValue(Data.newBitVector(0, BIT_VECTOR_LENGTH));
        INT_BASE_SIZE = new NodeValue(Data.newBitVector(32, BIT_VECTOR_LENGTH));

        INT_SIGN_MASK = new NodeOperation(
            StandardOperation.BVLSHL,
            new NodeOperation(StandardOperation.BVNOT, INT_ZERO),
            INT_BASE_SIZE
            );
    }

    protected static NodeOperation IsValidPos(Node arg)
    {
        return new NodeOperation(
            StandardOperation.EQ,
            new NodeOperation(StandardOperation.BVAND, arg, INT_SIGN_MASK),
            INT_ZERO
            );
    }

    protected static NodeOperation IsValidNeg(Node arg)
    {
        return new NodeOperation(
            StandardOperation.EQ,
            new NodeOperation(StandardOperation.BVAND, arg, INT_SIGN_MASK),
            INT_SIGN_MASK
            );
    }

    protected static Node IsValidSignedInt(Node arg)
    {
        return Node.OR(IsValidPos(arg), IsValidNeg(arg));
    }

    protected static Node isNotEqual(Node left, Node right)
    {
        return isNot(new NodeOperation(StandardOperation.EQ, left, right));
    }

    protected static Node isNot(Node expr)
    {
        return Node.NOT(expr);
    }
}
