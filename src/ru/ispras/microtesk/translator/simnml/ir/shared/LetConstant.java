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
    private final Expr   expression;

    LetConstant(String name, Expr expression)
    {
        assert null != name;
        assert null != expression;

        this.name = name;
        this.expression = expression;
    }

    public String getName()
    {
        return name;
    }

    public Expr getExpression()
    {
        return expression;
    }

    @Override
    public String toString()
    {
        return String.format(
            "LetConstant [name=%s, expression=%s]", name, expression.getText());
    }
}
