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
import ru.ispras.microtesk.translator.simnml.ir.location.Location;

public final class StatementAssignment extends Statement
{
    private final Location left;
    private final Expr right;

    StatementAssignment(Location left, Expr right)
    {
        super(Kind.ASSIGN);

        assert null != left;
        assert null != right;

        this.left  = left;
        this.right = right;
    }

    public Location getLeft()
    {
        return left;
    }

    public Expr getRight()
    {
        return right;
    }
}
