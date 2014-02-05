/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Expr.java, Feb 4, 2014 5:40:25 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

/**
 * The role of the Expr class is to describe Sim-nML expressions. The class aggregates
 * a Fortress expression and provides methods to obtain additional information.
 * 
 * @author Andrei Tatarnikov
 */

public final class Expr
{
    private final Node nodeTree;

    /**
     * Constructs an expression basing on a Fortress expression tree.
     * 
     * @param nodeTree A Fortress expression.
     * 
     * @throws NullPointerException if the parameter is null.
     * @throws IllegalArgumentException is the user attribute of the node
     * does not refer to a {@link NodeInfo} object. 
     */

    public Expr(Node nodeTree)
    {
        if (null == nodeTree)
            throw new NullPointerException();

        if (!(nodeTree.getUserData() instanceof NodeInfo))
            throw new IllegalArgumentException();

        this.nodeTree = nodeTree;
    }
    
    /**
     * Returns additional information on the root node of the Fortress 
     * expression tree. 
     * 
     * @return a {@link NodeInfo} object. 
     */
    
    private NodeInfo getNodeTreeInfo()
    {
        return (NodeInfo) nodeTree.getUserData();
    }

    /**
     * Returns a Fortress expression tree describing the expression.
     * 
     * @return Fortress expression tree.
     */

    public Node getNodeTree()
    {
        return nodeTree;
    }

    /**
     * Returns information on the value produced by the expression 
     * (type information and value for statically calculated expressions).
     * 
     * @return Value information object.
     */

    public ValueInfo getValueInfo()
    {
        return getNodeTreeInfo().getValueInfo();
    }

    /**
     * Checks whether two expression can be considered equivalent.
     * Such a feature is needed to reduce expressions.
     * 
     * @param expr Expression to be compared with the current.
     * @return <code>true</code> if the expression is equivalent to
     * the current or <code>false</code> otherwise.
     */
/*
    public boolean isEquivalent(Expr expr)
    {
        if (null == expr) return false;
        if (this == expr) return true;

        if (this.nodeTree == expr.nodeTree)
            return true;

        if (getValueInfo().isConstant() && getValueInfo().equals(expr.getValueInfo()))
            return true;
        
        if (!getValueInfo().hasEqualType(expr.getValueInfo()))
            return false;

        // TODO
        return false;
    }
    */
}
