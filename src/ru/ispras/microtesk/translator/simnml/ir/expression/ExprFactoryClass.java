/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactoryClass.java, Jan 22, 2013 5:54:10 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public final class ExprFactoryClass
{
    private ExprFactoryClass() {}

    public static ExprFactory createFactory(
        EExprKind targetKind,
        WalkerContext context
        )
    {
        final ExprFactory result;

        switch (targetKind)
        {
        case JAVA_STATIC:
            result = new ExprFactoryJavaStatic(context);
            break;

        case JAVA:
            result = new ExprFactoryJava(context);
            break;

        case MODEL:
            result = new ExprFactoryModel(context);
            break;

        default:
            assert false : "Unsupported target expression kind.";
            result = null;
        }

        return result;
        //return new ExprFactoryDebug(targetKind, result);
    }
}

final class ExprFactoryDebug implements ExprFactory
{
    private final EExprKind targetKind;
    private final ExprFactory factory;

    public ExprFactoryDebug(EExprKind targetKind, ExprFactory factory)
    {
        this.targetKind = targetKind;
        this.factory = factory;
    }

    private void trace(String method, String text, Expr result)
    {
        final String FORMAT =
            "### TRACE (ExprFactory, %s, %s): %s -> %s\n";

        System.out.printf(
            FORMAT,
            targetKind.name(),
            method,
            text,
            result != null ? result.getText() : "<unknown>"
        );
    }

    @Override
    public Expr namedConst(Where w, String name) throws SemanticException
    {
        final Expr result = factory.namedConst(w, name);

        trace(
            "namedConst",
            name,
            result
            );

        return result;
    }

    @Override
    public Expr intConst(Where w, String text, int radix) throws SemanticException
    {
        final Expr result = factory.intConst(w, text, radix);

        trace(
            "intConst",
            text,
            result
            );

        return result;
    }

    @Override
    public Expr location(Where w, LocationExpr location) throws SemanticException
    {
        final Expr result = factory.location(w, location);

        trace(
            "location",
            location.getText(),
            result
            );

        return result;
    }

    @Override
    public Expr binary(Where w, String opID, Expr arg1, Expr arg2) throws SemanticException
    {
        final Expr result = factory.binary(w, opID, arg1, arg2);

        trace(
            "binary",
            arg1.getText() + opID + arg2.getText(),
            result
            );

        return result;
    }

    @Override
    public Expr unary(Where w, String opID, Expr arg) throws SemanticException
    {
        final Expr result = factory.unary(w, opID, arg);
        
        trace(
            "unary",
            opID + arg.getText(),
            result
            );
        
        return result;
    }

    @Override
    public Expr evaluate(Where w, Expr src) throws SemanticException
    {
        final Expr result = factory.evaluate(w, src);
        
        trace(
            "evaluate",
            src.getText(),
            result
            );
        
        return result;
    }

    @Override
    public Expr coerce(Where w, Expr src, TypeExpr type) throws SemanticException
    {
        final Expr result = factory.coerce(w, src, type);
        
        trace(
            "coerce",
            src.getText(),
            result
            );
        
        return result;
    }
}
