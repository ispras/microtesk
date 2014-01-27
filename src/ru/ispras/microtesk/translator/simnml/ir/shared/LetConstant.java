/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LetConstant.java, Aug 20, 2013 6:47:22 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.shared;

import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class LetConstant
{
    private final String name;
    private final Expr   expr;

    LetConstant(String name, Expr expr)
    {
        assert null != name;
        assert null != expr;

        this.name = name;
        this.expr = expr;
    }

    public String getName()
    {
        return name;
    }

    public Expr getExpr()
    {
        return expr;
    }
    
    @Override
    public String toString()
    {
        return String.format(
            "LetConstant [name=%s, value=%s]", name, expr.getValueInfo());
    }
}
