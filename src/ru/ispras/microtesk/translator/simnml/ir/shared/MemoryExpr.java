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

package ru.ispras.microtesk.translator.simnml.ir.shared;

import ru.ispras.microtesk.model.api.memory.EMemoryKind;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression.ExprUtils;

public final class MemoryExpr
{
    private final EMemoryKind kind;
    private final Type    type;
    private final Expr        size;

    public MemoryExpr(EMemoryKind kind, Type type, Expr size)
    {
        assert null != type;
        assert null != size;
        
        this.size = size;
        this.type = type;
        this.kind = kind;
    }

    public EMemoryKind getKind()
    {
        return kind;
    }
    
    public Type getType()
    {
        return type;
    }
    
    public Expr getSizeExpr()
    {
        return size;
    }
    
    public int getSize()
    {
        return ExprUtils.integerValue(size);
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
