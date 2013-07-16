/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Primitive.java, Jul 9, 2013 11:32:13 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Argument;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public abstract class Primitive
{
    public static enum Kind
    {
        MODE,
        OP
    }

    private final String      name;
    private final Kind        kind;
    private final boolean isOrRule;

    Primitive(String name, Kind kind, boolean isOrRule)
    {
        this.name     = name;
        this.kind     = kind;
        this.isOrRule = isOrRule;
    }

    static final Primitive createOp(
        String name, Map<String, Argument> args, Map<String, Attribute> attrs)
    {
        return new PrimitiveAND(name, Kind.OP, null, args, attrs);
    }

    static Primitive createMode(
        String name, Expr retExpr, Map<String, Argument> args, Map<String, Attribute> attrs)
    {
        return new PrimitiveAND(name, Kind.MODE, retExpr, args, attrs);
    }

    static Primitive createOpOR(String name, List<Primitive> ors)
    {
        return new PrimitiveOR(name, Kind.OP, ors);
    }

    static Primitive createModeOR(String name, List<Primitive> ors)
    {
        return new PrimitiveOR(name, Kind.MODE, ors);
    }

    public final String getName()
    {
        return name;
    }

    public final Kind getKind()
    {
        return kind;
    }

    public final boolean isOrRule()
    {
        return isOrRule;
    }

    public abstract TypeExpr getReturnType();
}
