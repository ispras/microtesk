/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprNodeCoercion.java, Sep 25, 2013 12:07:03 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class ExprNodeCoercion extends ExprAbstract 
{
    private final Expr child;

    ExprNodeCoercion(Expr child, Type type)
    {
        super(NodeKind.COERCION, ValueInfo.createModel(type));

        assert null != child;
        this.child = child;
    }

    ExprNodeCoercion(Expr child, Class<?> type)
    {
        super(NodeKind.COERCION, ValueInfo.createNativeType(type));

        assert null != child;
        this.child = child;
    }

    public Expr getChild()
    {
        return child;
    }
}
