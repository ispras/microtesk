/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * StatementAssignment.java, Jul 19, 2013 11:49:42 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression.LocationExpr;

public final class StatementAssignment extends Statement
{
    private final LocationExpr left;
    private final Expr right;

    StatementAssignment(LocationExpr left, Expr right)
    {
        super(Kind.ASSIGN);

        assert null != left;
        assert null != right;

        this.left  = left;
        this.right = right;
    }

    public LocationExpr getLeft()
    {
        return left;
    }

    public Expr getRight()
    {
        return right;
    }

    @Override
    public String getText()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
