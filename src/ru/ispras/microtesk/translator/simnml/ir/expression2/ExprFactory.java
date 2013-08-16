/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExpressionFactory.java, Aug 14, 2013 12:00:36 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression.Location;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public class ExprFactory extends WalkerFactoryBase
{
    public ExprFactory(WalkerContext context)
    {
        super(context);
    }

    public Expr namedConst(Where w, String name) throws SemanticException
    {
        return null;
    }

    public Expr intConst(Where w, String text, int radix) throws SemanticException
    {
        return null;
    }

    public Expr location(Where w, Location location) throws SemanticException
    {
        return null;
    }

    public Expr binary(Where w, String opID, Expr arg1, Expr arg2) throws SemanticException
    {
        return null;        
    }

    public Expr unary(Where w, String opID, Expr arg) throws SemanticException
    {
        return null;
    }

    public Expr coerce(Where w, Expr src, Type type) throws SemanticException
    {
        return null;
    }
}
