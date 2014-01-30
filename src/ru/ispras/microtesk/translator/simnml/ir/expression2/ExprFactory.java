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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeExpr;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;

import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedConstant;
import ru.ispras.microtesk.translator.simnml.errors.ValueParsingFailure;

import ru.ispras.microtesk.translator.simnml.ir.expression.Operator;
import ru.ispras.microtesk.translator.simnml.ir.location.Location;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfoCalculator;

public final class ExprFactory extends WalkerFactoryBase
{
    private final ValueInfoCalculator calculator;

    public ExprFactory(WalkerContext context)
    {
        super(context);
        this.calculator = new ValueInfoCalculator(context);
    }

    public Node namedConstant(Where w, String name) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(name);

        if (!getIR().getConstants().containsKey(name))
            raiseError(w, new UndefinedConstant(name));

        final LetConstant source = getIR().getConstants().get(name);
        final NodeInfo  nodeInfo = NodeInfo.newNamedConst(source);

        final Data   data = toFortressData(nodeInfo.getValueInfo());
        final Node result = new NodeValue(data);

        result.setUserData(nodeInfo);
        return result;
    }

    public Node constant(Where w, String text, int radix) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(text);

        final BigInteger bi = new BigInteger(text, radix);

        final SourceConstant source;
        if (bi.bitLength() <= Integer.SIZE)
        {
            source = new SourceConstant(bi.intValue(), radix);
        }
        else if (bi.bitLength() <= Long.SIZE)
        {
            source = new SourceConstant(bi.longValue(), radix);
        }
        else
        {
            raiseError(w, new ValueParsingFailure(text, "Java integer"));
            source = null; // Will never be reached
        }

        final NodeInfo nodeInfo = NodeInfo.newConst(source);

        final Data data = toFortressData(nodeInfo.getValueInfo());
        final Node result = new NodeValue(data);

        result.setUserData(nodeInfo);
        return result;
    }

    public Node location(Location source)
    {
        checkNotNull(source);

        final NodeInfo nodeInfo = NodeInfo.newLocation(source);

        final String name = null; // TODO
        final Data data = toFortressData(nodeInfo.getValueInfo());

        final Variable variable = new Variable(name, data);
        final Node result = new NodeVariable(variable);

        result.setUserData(nodeInfo);
        return result;
    }

    public Node operator(
        Where w, ValueInfo.Kind target, String id, Node ... operands) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(target);
        checkNotNull(id);
        checkNotNull(operands);

        final Operator op = Operator.forText(id);

        if (null == op)
            raiseError(w, String.format(ERR_UNSUPPORTED_OPERATOR, id));

        if (operands.length != op.operands())
            raiseError(w, String.format(ERR_OPERAND_NUMBER_MISMATCH, id, op.operands()));

        final List<ValueInfo> values = new ArrayList<ValueInfo>(operands.length);
        for(Node operand : operands)
        {
            final NodeInfo nodeInfo = (NodeInfo) operand.getUserData();
            final ValueInfo vi = nodeInfo.getValueInfo();
            values.add(vi);
        }

        final ValueInfo   castValueInfo = calculator.cast(w, target, values);
        final ValueInfo resultValueInfo = calculator.calculate(w, op, castValueInfo, values);

        final SourceOperator source = new SourceOperator(op, castValueInfo, resultValueInfo);
        final NodeInfo nodeInfo = NodeInfo.newOperator(source);

        final Enum<?> operator = toFortressOperator(op, castValueInfo);
        final Node result = new NodeExpr(operator, operands);

        result.setUserData(nodeInfo);
        return result;
    }

    public Node coerce(Where w, Node src, Type type)
    {
        checkNotNull(w);
        checkNotNull(src);
        checkNotNull(type);

        final NodeInfo   srcNodeInfo = (NodeInfo) src.getUserData();
        final ValueInfo srcValueInfo = srcNodeInfo.getValueInfo();

        if (srcValueInfo.isModel() && type.equals(srcValueInfo.getModelType()))
            return src;

        final ValueInfo newValueInfo = ValueInfo.createModel(type);
        final NodeInfo   newNodeInfo = srcNodeInfo.coerceTo(newValueInfo);

        src.setUserData(newNodeInfo);
        return src;
     }

    public Node coerce(Where w, Node src, Class<?> type)
    {
        checkNotNull(w);
        checkNotNull(src);
        checkNotNull(type);

        final NodeInfo   srcNodeInfo = (NodeInfo) src.getUserData();
        final ValueInfo srcValueInfo = srcNodeInfo.getValueInfo();

        if (srcValueInfo.isNativeOf(type))
            return src;

        final ValueInfo newValueInfo = srcValueInfo.toNativeType(type);
        final NodeInfo   newNodeInfo = srcNodeInfo.coerceTo(newValueInfo);

        src.setUserData(newNodeInfo);
        return src;
    }

    private static void checkNotNull(Object o)
    {
        if (null == o)
            throw new NullPointerException();
    }

    private static Data toFortressData(ValueInfo valueInfo)
    {
        return null;
    }

    private static Enum<?> toFortressOperator(Operator operator, ValueInfo valueInfo)
    {
        return null;
    }

    private static final String    ERR_UNSUPPORTED_OPERATOR = "The %s operator is not supported.";
    private static final String ERR_OPERAND_NUMBER_MISMATCH = "The %s operator requires %d operands.";
}
