/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprNodeCondition.java, Jan 13, 2014 5:05:57 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

public final class ExprNodeCondition extends ExprAbstract
{
    private final Expr cond;
    private final Expr left;
    private final Expr right;

    ExprNodeCondition(Expr cond, Expr left, Expr right, ValueInfo resultValueInfo)
    {
        super(NodeKind.CONDITION, resultValueInfo);

        this.cond  = cond;
        this.left  = left;
        this.right = right;
    }

    @Override
    public boolean isEquivalent(Expr expr)
    {
        if (this == expr) return true;
        if (expr == null) return false;

        if (getValueInfo().isConstant() && getValueInfo().equals(expr.getValueInfo()))
            return true;

        if (!getValueInfo().hasEqualType(expr.getValueInfo()))
            return false;

        if (getClass() != expr.getClass())
            return false;

        final ExprNodeCondition other = (ExprNodeCondition) expr;

        if (!cond.isEquivalent(other.cond))
            return false;

        if (!left.isEquivalent(other.left))
            return false;

        if (!right.isEquivalent(other.right))
            return false;

        return true;
    }

    public Expr getCondition()
    {
        return cond;
    }

    public Expr getLeft()
    {
        return left;
    }

    public Expr getRight()
    {
        return right;
    }
}
