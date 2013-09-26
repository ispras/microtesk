/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Expr.java, Aug 14, 2013 12:30:28 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

public interface Expr
{
    public enum NodeKind
    {
        CONST,
        NAMED_CONST,
        LOCATION,
        OPERATOR,
        COERCION
    }

    public NodeKind   getNodeKind();
    public ValueInfo getValueInfo();
}

abstract class ExprAbstract implements Expr
{
    private final NodeKind   nodeKind;
    private final ValueInfo valueInfo;

    protected ExprAbstract(NodeKind nodeKind, ValueInfo valueInfo)
    {
        assert null != nodeKind;
        assert null != valueInfo;

        this.nodeKind = nodeKind;
        this.valueInfo = valueInfo;
    }

    @Override
    public final NodeKind getNodeKind()
    {
        return nodeKind;
    }

    @Override
    public final ValueInfo getValueInfo()
    {
        return valueInfo;
    }
}
