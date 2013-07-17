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

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public class Primitive
{
    public static enum Kind
    {
        /** Addressing mode. */
        MODE,

        /** Operation. */
        OP,

        /** Immediate value. */
        IMM
    }

    private final String         name;
    private final Kind           kind;
    private final boolean    isOrRule;
    private final TypeExpr returnType;

    Primitive(String name, Kind kind, boolean isOrRule, TypeExpr returnType)
    {
        this.name       = name;
        this.kind       = kind;
        this.isOrRule   = isOrRule;
        this.returnType = returnType;
    }

    static Primitive createMode(
        String name, Expr retExpr, Map<String, Primitive> args, Map<String, Attribute> attrs)
    {
        return new PrimitiveAND(name, Kind.MODE, retExpr, args, attrs);
    }

    static Primitive createModeOR(String name, List<Primitive> ors)
    {
        return new PrimitiveOR(name, Kind.MODE, ors);
    }

    static final Primitive createOp(
        String name, Map<String, Primitive> args, Map<String, Attribute> attrs)
    {
        return new PrimitiveAND(name, Kind.OP, null, args, attrs);
    }
    
    static Primitive createOpOR(String name, List<Primitive> ors)
    {
        return new PrimitiveOR(name, Kind.OP, ors);
    }
    
    static Primitive createImm(TypeExpr type)
    {
        return new Primitive(type.getRefName(), Kind.IMM, false, type);
    }

    public final String getName()
    {
        if (null != name)
            return name;

        if (Kind.IMM == kind)
        {
            return String.format(
                "new %s(%s.%s, %s)",
                Type.class.getSimpleName(),
                ETypeID.class.getSimpleName(),
                returnType.getTypeId().name(),
                returnType.getBitSize().getText()
            );
        }

        assert false : "Primitive name is not defined.";
        return null;
    }

    public final Kind getKind()
    {
        return kind;
    }

    public final boolean isOrRule()
    {
        return isOrRule;
    }

    public final TypeExpr getReturnType()
    {
        return returnType;
    }
}
