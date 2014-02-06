/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprNodeNamedConst.java, Aug 20, 2013 8:12:47 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression_;

import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;

public final class ExprNodeNamedConst extends ExprAbstract
{
    private final LetConstant constant;

    ExprNodeNamedConst(LetConstant constant)
    {
        super(NodeKind.NAMED_CONST, constant.getExpr().getValueInfo());
        this.constant = constant;
    }

    public LetConstant getConstant()
    {
        return constant;
    }

    @Override
    public boolean isEquivalent(Expr expr)
    {
        if (this == expr) return true;
        if (expr == null) return false;

        return getValueInfo().equals(expr.getValueInfo());
    }
}
