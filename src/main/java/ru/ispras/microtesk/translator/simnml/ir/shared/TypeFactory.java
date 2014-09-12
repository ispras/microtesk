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

import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class TypeFactory extends WalkerFactoryBase
{
    public TypeFactory(WalkerContext context)
    {
        super(context);
    }

    public Type newAlias(Where where, String name) throws SemanticException
    {
        final Type ref = getIR().getTypes().get(name); 

        if (null == ref)
            raiseError(where, String.format("Undefined type: %s.", name));

        return ref.newAlias(name);
    }

    public Type newInt(Where where, Expr bitSize) throws SemanticException
    {
        return new Type(TypeId.INT, bitSize);
    }

    public Type newCard(Where where, Expr bitSize) throws SemanticException
    {
        return new Type(TypeId.CARD, bitSize);
    }
    
    public Type newFloat(
        Where where, Expr fractionBitSize, Expr exponentBitSize)
        throws SemanticException
    {
        // TODO
        return null;
    }
}
