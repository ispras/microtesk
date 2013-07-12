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
import ru.ispras.microtesk.translator.simnml.ir.modeop.Argument;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public class PrimitiveAND extends Primitive
{
    private final Expr retExpr;
    private final Map<String, Argument> args;
    private final Map<String, Attribute> attrs;

    PrimitiveAND(
        String name,
        Kind kind,
        Expr retExpr,
        Map<String, Argument> args,
        Map<String, Attribute> attrs
        )
    {
        super(name, kind, false);
        this.retExpr = retExpr;
        this.args    = args;
        this.attrs   = attrs;
    }

    public final Map<String, Argument> getArgs()
    {
        return args;
    }

    public final Map<String, Attribute> getAttrs()
    {
        return attrs;
    }

    @Override
    public TypeExpr getReturnType()
    {
        return (null != retExpr) ? retExpr.getModelType() : null;
    }

    public Expr getReturnExpr()
    {
        return retExpr;
    }
}
