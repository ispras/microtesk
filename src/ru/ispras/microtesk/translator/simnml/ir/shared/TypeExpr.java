/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TypeExpr.java, Oct 22, 2012 1:53:02 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.shared;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class TypeExpr
{
    private final ETypeID  typeId;
    private final Expr    bitSize;
    private final String  refName;

    public TypeExpr(ETypeID typeId, Expr bitSize, String refName)
    {
        assert null != bitSize;
        
        this.typeId  = typeId;
        this.bitSize = bitSize;
        this.refName = refName;
    }
    
    public TypeExpr(ETypeID typeId, Expr bitSize)
    {
        this(typeId, bitSize, null);
    }

    public ETypeID getTypeId()
    {
        return typeId;
    }

    public Expr getBitSize()
    {
        return bitSize;
    }

    public String getRefName()
    {
        return refName;
    }

    @Override
    public String toString()
    {
        return String.format(
            "TypeExpr [typeId='%s', bitSize='%s', refName='%s']",
            typeId.name(),
            bitSize.getText(),
            refName != null ? refName : "<undefined>"
            );
    }
}
