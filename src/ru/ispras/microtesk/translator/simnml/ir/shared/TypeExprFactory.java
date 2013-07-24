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

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.SizeExpressionTypeMismatch;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class TypeExprFactory extends WalkerFactoryBase
{
    public TypeExprFactory(WalkerContext context)
    {
        super(context);
    }

    public void failIfNotInteger(Where where, Class<?> type) throws SemanticException
    {
        if (!type.equals(Integer.class) && !type.equals(int.class))
            raiseError(where, new SizeExpressionTypeMismatch(type));
    }

    public TypeExpr createAlias(String name) throws SemanticException
    {
        final TypeExpr ref = getIR().getTypes().get(name); 
        return new TypeExpr(ref.getTypeId(), ref.getBitSize(), name);
    }

    public TypeExpr createIntegerType(Where where, Expr bitSize) throws SemanticException
    {
        failIfNotInteger(where, bitSize.getJavaType());
        return new TypeExpr(ETypeID.INT, bitSize);
    }

    public TypeExpr createCardType(Where where, Expr bitSize) throws SemanticException
    {
        failIfNotInteger(where, bitSize.getJavaType());
        return new TypeExpr(ETypeID.CARD, bitSize);
    }
}
