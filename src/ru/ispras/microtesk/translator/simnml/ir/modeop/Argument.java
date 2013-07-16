/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Argument.java, Jan 11, 2013 12:34:41 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.modeop;

import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

public final class Argument
{
    private final String name;
    private final EArgumentKind kind;

    private final TypeExpr  valueType;
    private final Primitive primitive;

    public Argument(String name, TypeExpr valueType)
    {
        this(
            name,
            EArgumentKind.TYPE,
            valueType,
            null
            );

        assert(null != valueType);
    }

    public Argument(String name, Primitive primitive)
    {
        this(
            name,
            primitive.getKind() == Primitive.Kind.MODE ? EArgumentKind.MODE : EArgumentKind.OP,
            null,
            primitive
            );

        assert(null != primitive);
    }

    private Argument(
        String name,
        EArgumentKind kind,
        TypeExpr type,
        Primitive primitive
        )
    {
        this.name      = name;
        this.kind      = kind;
        this.valueType = type;
        this.primitive = primitive;
    }

    public String getName()
    {
        return name;
    }

    public EArgumentKind getKind()
    {
        return kind;
    }

    public TypeExpr getValueType()
    {
        assert EArgumentKind.TYPE == kind;
        assert null != valueType;

        return valueType;
    }

    public Primitive getMode()
    {
        assert EArgumentKind.MODE == kind;
        assert null != primitive;
        
        return primitive;
    }

    public Primitive getOp()
    {
        assert EArgumentKind.OP == kind;
        assert null != primitive;
        
        return primitive;
    }

    public String getTypeText()
    {
        if (EArgumentKind.MODE == kind)
            return getMode().getName();

        if (EArgumentKind.OP == kind)
            return getOp().getName();

        if (null != getValueType().getRefName())
            return getValueType().getRefName();

        return String.format(
            "new %s(%s.%s, %s)",
            Type.class.getSimpleName(),
            ETypeID.class.getSimpleName(),
            getValueType().getTypeId().name(),
            getValueType().getBitSize().getText()
            );
    }
}
