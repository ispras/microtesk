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

package ru.ispras.microtesk.translator.simnml.ir.expression_;

import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

/**
 * The ExprNodeCoercion class describes cast of a source expression to the specified type.
 * 
 * @author Andrei Tatarnikov
 */

public final class ExprNodeCoercion extends ExprAbstract 
{
    private final Expr source;

    /**
     * Constructs a cast of a source expression to the specified target type (or value for constants).  
     * 
     * @param source Source expression.
     * @param target Target value information.
     */

    ExprNodeCoercion(Expr source, ValueInfo target)
    {
        super(NodeKind.COERCION, target);

        if (null == source)
            throw new NullPointerException();

        if (null == target)
            throw new NullPointerException();

        this.source = source;
    }

    /**
     * Returns source expression.
     * 
     * @return Source expression.
     */

    public Expr getSource()
    {
        return source;
    }

    @Override
    public boolean isEquivalent(Expr expr)
    {
        if (this == expr) return true;
        if (expr == null) return false;
        
        if (getValueInfo().isConstant() && getValueInfo().equals(expr.getValueInfo()))
            return true;

        if (!getValueInfo().hasEqualType(expr.getValueInfo()))
            return false;

        if (NodeKind.COERCION == expr.getNodeKind())
        {
            final ExprNodeCoercion exprCoercion = (ExprNodeCoercion) expr;
            return getSource().isEquivalent(exprCoercion.getSource());
        }

        return getSource().isEquivalent(expr);
    }
}
