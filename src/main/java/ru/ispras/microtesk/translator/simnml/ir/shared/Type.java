/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Type.java, Oct 22, 2012 1:53:02 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.shared;

import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class Type
{
    public final static Type BOOLEAN = new Type(TypeId.BOOL, Expr.newConstant(1));

    private final TypeId  typeId;
    private final Expr    bitSize;
    private final String  refName;

    public Type(TypeId typeId, Expr bitSize, String refName)
    {
        if (null == typeId)
            throw new NullPointerException();

        if (null == bitSize)
            throw new NullPointerException();

        this.typeId  = typeId;
        this.bitSize = bitSize;
        this.refName = refName;
    }

    public Type(TypeId typeId, Expr bitSize)
    {
        this(typeId, bitSize, null);
    }

    public Type(TypeId typeId, int bitSize)
    {
        this(typeId, Expr.newConstant(bitSize), null);
    }

    public TypeId getTypeId()
    {
        return typeId;
    }

    public Expr getBitSizeExpr()
    {
        return bitSize;
    }

    public int getBitSize()
    {
        return bitSize.integerValue();
    }

    public String getRefName()
    {
        return refName;
    }

    public String getJavaText()
    {
        if (null != refName)
            return refName;

        return String.format("new %s", getTypeName());
    }

    public String getTypeName()
    {
        return String.format(
            "%s(%s.%s, %d)",
            ru.ispras.microtesk.model.api.type.Type.class.getSimpleName(),
            TypeId.class.getSimpleName(),
            getTypeId(),
            getBitSize()
            );
    }

    @Override
    public String toString()
    {
        return String.format(
            "Type [typeId='%s', bitSize='%d', refName='%s']",
            typeId,
            getBitSize(),
            refName != null ? refName : "<undefined>"
            );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;

        result = prime * result + typeId.hashCode();
        result = prime * result + getBitSize();

        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;

        if (getClass() != obj.getClass())
            return false;

        final Type other = (Type) obj;

        if (typeId != other.typeId)
            return false;

        if (getBitSize() != other.getBitSize())
            return false;

        return true;
    }
}
