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

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression.ExprUtils;

public final class Type
{
    public final static Type BOOLEAN = new Type(ETypeID.BOOL, ExprUtils.createConstant(1));
    
    private final ETypeID  typeId;
    private final Expr    bitSize;
    private final String  refName;

    public Type(ETypeID typeId, Expr bitSize, String refName)
    {
        assert null != bitSize;
        
        this.typeId  = typeId;
        this.bitSize = bitSize;
        this.refName = refName;
    }
    
    public Type(ETypeID typeId, Expr bitSize)
    {
        this(typeId, bitSize, null);
    }
    
    public ETypeID getTypeId()
    {
        return typeId;
    }

    public Expr getBitSizeExpr()
    {
        return bitSize;
    }

    public int getBitSize()
    {
        return ExprUtils.integerValue(bitSize);
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
            ETypeID.class.getSimpleName(),
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
