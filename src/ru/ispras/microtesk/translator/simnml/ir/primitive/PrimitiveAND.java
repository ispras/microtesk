/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PrimitiveAND.java, Jul 9, 2013 12:42:09 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public class PrimitiveAND extends Primitive
{
    private final Expr retExpr;
    private final Map<String, Primitive> args;
    private final Map<String, Attribute> attrs;

    PrimitiveAND(
        String name,
        Kind kind,
        Expr retExpr,
        Map<String, Primitive> args,
        Map<String, Attribute> attrs
        )
    {
        super(
            name,
            kind,
            false,
            getReturnType(retExpr),
            null == attrs ? null : attrs.keySet()
            );

        this.retExpr = retExpr;
        this.args    = args;
        this.attrs   = attrs;
    }

    public final Map<String, Primitive> getArgs()
    {
        return args;
    }

    public final Map<String, Attribute> getAttrs()
    {
        return attrs;
    }

    public Expr getReturnExpr()
    {
        return retExpr;
    }
    
    private static final TypeExpr getReturnType(Expr retExpr)
    {
        return (null != retExpr) ? retExpr.getModelType() : null;
    }
}
