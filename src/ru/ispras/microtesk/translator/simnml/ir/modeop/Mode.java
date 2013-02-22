/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Mode.java, Dec 19, 2012 2:33:19 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.modeop;

import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.expression2.Expr;
import ru.ispras.microtesk.translator.simnml.ir.type.TypeExpr;

public final class Mode extends Primitive<Mode>
{
    private final Expr retExpr;

    public Mode(String name, List<Mode> ors)
    {
        super(name, ors);
        this.retExpr = null;
    }

    public Mode(
        String name,
        Map<String, Argument> args,
        Map<String, Attribute> attrs,
        Expr retExpr
        )
    {
        super(name, args, attrs);
        this.retExpr = retExpr;
    }

    public TypeExpr getReturnType()
    {
        if (isOrRule())
        {
            assert !getOrs().isEmpty();
            return getOrs().get(0).getReturnType();
        }
        
        if (null == retExpr)
            return null;

        return retExpr.getModelType();
    }

    public Expr getReturnExpr()
    {
        assert !isOrRule();
        return retExpr;
    }
}
