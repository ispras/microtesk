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

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

/**
 * The Expr interface is implemented by all nodes of a syntax tree describing a Sim-nML expression.
 * It provides information on node kind and data values produced by the expression that includes
 * the current node and all its children.
 * 
 * @author Andrei Tatarnikov
 */

public interface Expr
{
    /** 
     * The NodeKind enumeration is used to specify a kind of a node in a expression syntax tree.  
     */

    public enum NodeKind
    {
        /** Constant value. A numeric literal. */
        CONST,

        /** Named constant described by a Let construction. */
        NAMED_CONST,

        /** Location (register, memory, variable, immediate, etc.) referred directly or via an addressing mode. */
        LOCATION,

        /** Operator expression. */
        OPERATOR,

        /** Explicit type cast (applied to locations). */
        COERCION,

        /** Conditional expression (based on the ternary conditional operator). */
        CONDITION
    }

    /**
     * Returns the kind of node describing the given expression.
     * 
     * @return Node kind.
     */

    public NodeKind getNodeKind();

    /**
     * Returns information on the value produced by the expression 
     * (type information and value for statically calculated expressions).
     * 
     * @return Value information object.
     */

    public ValueInfo getValueInfo();
    
    /**
     * Checks whether two expression can be considered equivalent.
     * Such a feature is needed to reduce expressions.
     * 
     * @param expr Expression to be compared with the current.
     * @return <code>true</code> if the expression is equivalent to the current or <code>false</code> otherwise.
     */

    public boolean isEquivalent(Expr expr); 
}

/**
 * The ExprAbstract class provides basic implementation for all expression node kinds.
 * 
 * @author Andrei Tatarnikov
 */

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
