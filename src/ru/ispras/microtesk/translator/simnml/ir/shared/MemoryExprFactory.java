/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MemoryExprFactory.java, Dec 13, 2012 1:32:04 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.shared;

import ru.ispras.microtesk.model.api.memory.EMemoryKind;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression.ExprUtils;

public final class MemoryExprFactory extends WalkerFactoryBase
{
    private static final Expr DEFAULT_SIZE = ExprUtils.createConstant(1);

    public MemoryExprFactory(WalkerContext context)
    {
        super(context);
    }

    public MemoryExpr createMemory(EMemoryKind kind, Type type, Expr size) 
    {
        return new MemoryExpr(kind, type, size);        
    }
    
    public MemoryExpr createMemory(EMemoryKind kind, Type type)
    {
        return new MemoryExpr(kind, type, DEFAULT_SIZE);        
    }
}
