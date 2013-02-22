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

import ru.ispras.microtesk.translator.simnml.ir.type.TypeExpr;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

public final class Argument
{
    private final String name;
    private final EArgumentKind kind;

    private final TypeExpr valueType;
    private final Mode mode;
    private final Op op;

    public Argument(String name, TypeExpr valueType)
    {
        this(name, EArgumentKind.TYPE, valueType, null, null);
        assert(null != valueType);
    }

    public Argument(String name, Mode mode)
    {
        this(name, EArgumentKind.MODE, null, mode, null);
        assert(null != mode);
    }

    public Argument(String name, Op op)
    {
        this(name, EArgumentKind.OP, null, null, op);
        assert(null != op);
    }

    private Argument(
        String name,
        EArgumentKind kind,
        TypeExpr type,
        Mode mode,
        Op op
        )
    {
        this.name = name;
        this.kind = kind;
        this.valueType = type;
        this.mode = mode;
        this.op = op;
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

    public Mode getMode()
    {
        assert EArgumentKind.MODE == kind;
        assert null != mode;

        return mode;
    }

    public Op getOp()
    {
        assert EArgumentKind.OP == kind;
        assert null != op;

        return op;
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
