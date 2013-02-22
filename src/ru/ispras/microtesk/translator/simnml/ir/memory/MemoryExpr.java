/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MemoryExp.java, Dec 13, 2012 1:27:55 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.memory;

import ru.ispras.microtesk.model.api.memory.EMemoryKind;
import ru.ispras.microtesk.translator.simnml.ir.expression.ConstExpr;
import ru.ispras.microtesk.translator.simnml.ir.type.TypeExpr;

public final class MemoryExpr
{
    private final EMemoryKind kind;
    private final TypeExpr    type;
    private final ConstExpr   size;

    public MemoryExpr(EMemoryKind kind, TypeExpr type, ConstExpr size)
    {
        this.size = size;
        this.type = type;
        this.kind = kind;
    }

    public EMemoryKind getKind()
    {
        return kind;
    }
    
    public TypeExpr getType()
    {
        return type;
    }
    
    public ConstExpr getSize()
    {
        return size;
    }    

    @Override
    public String toString()
    {
        return String.format(
            "MemoryExp [kind=%s, type= %s, size= %s]",
            kind,
            type,
            size
        );
    }
}
