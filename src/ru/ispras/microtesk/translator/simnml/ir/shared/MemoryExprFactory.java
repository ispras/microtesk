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
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.SizeExpressionTypeMismatch;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression.ExprClass;

public final class MemoryExprFactory extends WalkerFactoryBase
{
    private static final Expr DEFAULT_SIZE = ExprClass.createConstant(1, "1");

    public MemoryExprFactory(WalkerContext context)
    {
        super(context);
    }

    private void failIfNotInteger(Where where, Class<?> type) throws SemanticException
    {
        if (!type.equals(Integer.class) && !type.equals(int.class))
            raiseError(where, new SizeExpressionTypeMismatch(type));
    }

    public MemoryExpr createMemoryExpr(Where where, EMemoryKind kind, TypeExpr type, Expr size) throws SemanticException 
    {
        failIfNotInteger(where, size.getJavaType());
        return new MemoryExpr(kind, type, size);        
    }
    
    public MemoryExpr createMemoryExpr(EMemoryKind kind, TypeExpr type)
    {
        return new MemoryExpr(kind, type, DEFAULT_SIZE);        
    }
}
