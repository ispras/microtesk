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

public final class TypeFactory extends WalkerFactoryBase
{
    public TypeFactory(WalkerContext context)
    {
        super(context);
    }

    public void failIfNotInteger(Where where, Class<?> type) throws SemanticException
    {
        if (!type.equals(Integer.class) && !type.equals(int.class))
            raiseError(where, new SizeExpressionTypeMismatch(type));
    }

    public Type createAlias(String name) throws SemanticException
    {
        final Type ref = getIR().getTypes().get(name); 
        return new Type(ref.getTypeId(), ref.getBitSize(), name);
    }

    public Type createIntegerType(Where where, Expr bitSize) throws SemanticException
    {
        failIfNotInteger(where, bitSize.getJavaType());
        return new Type(ETypeID.INT, bitSize);
    }

    public Type createCardType(Where where, Expr bitSize) throws SemanticException
    {
        failIfNotInteger(where, bitSize.getJavaType());
        return new Type(ETypeID.CARD, bitSize);
    }
}
