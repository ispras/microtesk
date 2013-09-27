/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactoryModel.java, Sep 27, 2013 12:01:30 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.ir.expression.Location;
import ru.ispras.microtesk.translator.simnml.ir.expression2.ValueInfo.ValueKind;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class ExprFactoryModel extends ExprFactory
{
    ExprFactoryModel(WalkerContext context)
    {
        super(context);
    }

    @Override
    public ExprNodeLocation location(Location location)
    {
        return new ExprNodeLocation(location);
    }

    @Override
    public Expr binary(Where w, String opID, Expr arg1, Expr arg2) throws SemanticException
    {
        // TODO Auto-generated method stub
        
        
        return null;
    }

    @Override
    public Expr unary(Where w, String opID, Expr arg) throws SemanticException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Expr coerce(Where w, Expr src, Type type) throws SemanticException
    {
        return new ExprNodeCoercion(src, type);
    }

    @Override
    public Expr evaluate(Where w, Expr src) throws SemanticException
    {
        if (ValueKind.MODEL == src.getValueInfo().getValueKind())
            return src;

        // TODO Auto-generated method stub
        return null;
    }
}
