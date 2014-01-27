/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactory.java, Jan 27, 2014 3:04:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;

import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedConstant;

import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.location.Location;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

public final class ExprFactory extends WalkerFactoryBase
{
    public ExprFactory(WalkerContext context)
    {
        super(context);
    }

    public Node namedConstant(Where w, String name) throws SemanticException
    {
        if (!getIR().getConstants().containsKey(name))
            raiseError(w, new UndefinedConstant(name));

        final LetConstant source = 
            getIR().getConstants().get(name);

        final Data data = null;
        final Node result = new NodeValue(data);

        final NodeInfo nodeInfo = NodeInfo.newNamedConst(source);
        result.setUserData(nodeInfo);

        return result;
    }

    public Node constant(Where w, String text, int radix) throws SemanticException
    {
        return null;
    }

    public Node location(Location location)
    {
        if (null == location)
            throw new NullPointerException();

        final Variable variable = null;
        final Node result = new NodeVariable(variable);

        final NodeInfo nodeInfo = NodeInfo.newLocation(location);
        result.setUserData(nodeInfo);

        return result;
    }

    public Node coerce(Where w, Node src, Type type)
    {
        return null;
    }

    public Node coerce(Where w, Node src, Class<?> type)
    {
        return null;
    }

    public Expr operator(
        Where w, ValueInfo.Kind target, String id, Node ... operands) throws SemanticException
    {
        return null;
    }
}
