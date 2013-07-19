/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * StatementFactory.java, Jul 19, 2013 11:40:51 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.List;

import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression.LocationExpr;

public final class StatementFactory
{
    public Statement createAssignment(LocationExpr left, Expr right)
    {
        return new StatementAssignment(left, right);
    }

    public Statement createCondition(Expr cond, List<Statement> isSmts, List<Statement> elseSmts)
    {
        return new StatementCondition(cond, isSmts, elseSmts);
    }

    public Statement createAttributeCall()
    {
        // TODO: implement
        return null;
    }
}
