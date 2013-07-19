/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * StatementCondition.java, Jul 19, 2013 11:55:00 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.List;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class StatementCondition extends Statement
{
    private final Expr                cond;
    private final List<Statement>   ifSmts;
    private final List<Statement> elseSmts;

    StatementCondition(
        Expr cond,
        List<Statement> ifSmts,
        List<Statement> elseSmts
        )
    {
        super(Kind.COND);
        
        assert null != cond;
        assert null != ifSmts;

        this.cond     = cond;
        this.ifSmts   = ifSmts;
        this.elseSmts = elseSmts;
    }

    public Expr getCondition()
    {
        return cond;
    }

    public List<Statement> getIfStatements()
    {
        return ifSmts;
    }

    public List<Statement> getElseStatements()
    {
        return elseSmts;
    }

    @Override
    public String getText()
    {
        return null;
    }
}
