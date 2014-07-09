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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class PrimitiveAND extends Primitive
{
    private final Expr                 retExpr;
    private final Map<String, Primitive>  args;
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
        this.attrs   = Collections.unmodifiableMap(attrs);

        for (Map.Entry<String, Primitive> e : args.entrySet())
        {
            final Primitive     target = e.getValue();
            final String referenceName = e.getKey();

            target.addParentReference(this, referenceName);
        }
    }

    private PrimitiveAND(PrimitiveAND source)
    {
        super(source);

        this.retExpr = source.retExpr;
        this.args    = new LinkedHashMap<String, Primitive>(source.args);
        this.attrs   = source.attrs;
    }

    public PrimitiveAND makeCopy()
    {
        return new PrimitiveAND(this);
    }

    public Map<String, Primitive> getArguments()
    {
        return args;
    }

    public Map<String, Attribute> getAttributes()
    {
        return attrs;
    }

    public Expr getReturnExpr()
    {
        return retExpr;
    }

    private static Type getReturnType(Expr retExpr)
    {
        return (null != retExpr) ? retExpr.getValueInfo().getModelType() : null;
    }
}
