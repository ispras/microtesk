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

import org.antlr.runtime.RecognitionException;

import ru.ispras.microtesk.model.api.memory.EMemoryKind;
import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.errors.SizeExpressionTypeMismatch;
import ru.ispras.microtesk.translator.simnml.ir.expression.ConstExpr;

public final class MemoryExprFactory
{
    private static final ConstExpr DEFAULT_SIZE = new ConstExpr(int.class, 1, "1");

    private final IErrorReporter reporter; 

    public MemoryExprFactory(IErrorReporter reporter)
    {
        this.reporter = reporter;
    }

    private void failIfNotInteger(Where where, Class<?> type) throws RecognitionException
    {
        if (!type.equals(Integer.class) && !type.equals(int.class))
            reporter.raiseError(where, new SizeExpressionTypeMismatch(type));
    }

    public MemoryExpr createMemoryExpr(Where where, EMemoryKind kind, TypeExpr type, ConstExpr size) throws RecognitionException 
    {
        failIfNotInteger(where, size.getJavaType());
        return new MemoryExpr(kind, type, size);        
    }
    
    public MemoryExpr createMemoryExpr(EMemoryKind kind, TypeExpr type)
    {
        return new MemoryExpr(kind, type, DEFAULT_SIZE);        
    }
}
