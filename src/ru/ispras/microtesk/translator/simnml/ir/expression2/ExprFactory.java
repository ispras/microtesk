/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactory.java, Aug 14, 2013 12:00:36 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import java.util.EnumMap;
import java.util.Map;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedConstant;
import ru.ispras.microtesk.translator.simnml.ir.expression.Location;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public abstract class ExprFactory extends WalkerFactoryBase
{
    private final static Map<ValueInfo.ValueKind, ExprFactory> factories =
        new EnumMap<ValueInfo.ValueKind, ExprFactory>(ValueInfo.ValueKind.class);

    protected ExprFactory(WalkerContext context)
    {
        super(context);
    }
    
    public static ExprFactory getInstance(ValueInfo.ValueKind targetKind, WalkerContext context)
    {
        if (factories.containsKey(targetKind))
            return factories.get(targetKind);

        final ExprFactory result;
        switch (targetKind)
        {
        case MODEL:
            result = new ExprFactoryModel(context);
            break;

        case NATIVE:
            result = new ExprFactoryNative(context);
            break;

        default:
            assert false : "Unknown target value kind!";
            result = null;
            break;
        }

        if (null != result)
            factories.put(targetKind, result);

        return result;
    }

    public final Expr namedConstant(Where w, String name) throws SemanticException
    {
        if (!getIR().getConstants().containsKey(name))
            getReporter().raiseError(w, new UndefinedConstant(name));

        final LetConstant constant = getIR().getConstants().get(name);
        return new ExprNodeNamedConst(constant);
    }

    public final Expr constant(Where w, String text, int radix) throws SemanticException
    {
        final ExprFactoryCreator creator = new ConstantCreator(this, w, text, radix); 
        return creator.create();
    }

    public abstract Expr location(Location location);
    public abstract Expr binary(Where w, String opID, Expr arg1, Expr arg2) throws SemanticException;
    public abstract Expr unary (Where w, String opID, Expr arg) throws SemanticException;
    public abstract Expr coerce(Where w, Expr src, Type type) throws SemanticException;
    public abstract Expr evaluate(Where w, Expr src) throws SemanticException;
}
