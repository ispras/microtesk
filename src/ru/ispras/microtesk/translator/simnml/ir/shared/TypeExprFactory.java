/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TypeExprFactory.java, Oct 22, 2012 1:53:18 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.shared;

import java.util.Map;

import org.antlr.runtime.RecognitionException;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.errors.SizeExpressionTypeMismatch;
import ru.ispras.microtesk.translator.simnml.ir.expression2.Expr;

public class TypeExprFactory
{
    private final Map<String, TypeExpr> types;
    private final IErrorReporter     reporter; 

    public TypeExprFactory(Map<String, TypeExpr> types, IErrorReporter reporter)
    {
        this.types    = types;        
        this.reporter = reporter;
    }

    public void failIfNotInteger(Where where, Class<?> type) throws RecognitionException
    {
        if (!type.equals(Integer.class) && !type.equals(int.class))
            reporter.raiseError(where, new SizeExpressionTypeMismatch(type));
    }

    public TypeExpr createAlias(String name) throws RecognitionException
    {
        final TypeExpr ref = types.get(name); 
        return new TypeExpr(ref.getTypeId(), ref.getBitSize(), name);
    }

    public TypeExpr createIntegerType(Where where, Expr bitSize) throws RecognitionException
    {
        failIfNotInteger(where, bitSize.getJavaType());
        return new TypeExpr(ETypeID.INT, bitSize);
    }

    public TypeExpr createCardType(Where where, Expr bitSize) throws RecognitionException
    {
        failIfNotInteger(where, bitSize.getJavaType());
        return new TypeExpr(ETypeID.CARD, bitSize);
    }
}
