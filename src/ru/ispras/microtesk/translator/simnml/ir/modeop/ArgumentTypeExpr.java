/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArgumentInfo.java, Dec 25, 2012 11:09:22 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.modeop;

import ru.ispras.microtesk.translator.simnml.ir.type.TypeExpr;

public final class ArgumentTypeExpr
{
    private final EArgumentKind     kind;
    private final String        typeName;
    private final TypeExpr      typeExpr;

    public ArgumentTypeExpr(EArgumentKind kind, String typeName)
    {
        assert EArgumentKind.TYPE != kind;
        
        this.kind     = kind;
        this.typeName = typeName;
        this.typeExpr = null;
    }

    public ArgumentTypeExpr(EArgumentKind kind, TypeExpr typeExpr)
    {
        assert EArgumentKind.TYPE == kind;

        this.kind     = kind;
        this.typeName = null;
        this.typeExpr = typeExpr;
    }

    public EArgumentKind getKind()
    {
        return kind;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public TypeExpr getTypeExpr()
    {
        return typeExpr;
    }
}
