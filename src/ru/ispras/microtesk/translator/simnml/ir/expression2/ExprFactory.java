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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeExpr;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;

import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedConstant;
import ru.ispras.microtesk.translator.simnml.errors.ValueParsingFailure;

import ru.ispras.microtesk.translator.simnml.ir.expression.ExprNodeCoercion;
import ru.ispras.microtesk.translator.simnml.ir.expression.ExprUtils;
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

        final Data   data = Converter.toFortressData(nodeInfo.getValueInfo());
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

        final Data data = Converter.toFortressData(nodeInfo.getValueInfo());
        final Node result = new NodeValue(data);

        result.setUserData(nodeInfo);
        return result;
    }

    public Node location(Location source)
    {
        checkNotNull(source);

        final NodeInfo nodeInfo = NodeInfo.newLocation(source);

        final String name = "unnamed"; // TODO
        final Data data = Converter.toFortressData(nodeInfo.getValueInfo());

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

        final Enum<?> operator = Converter.toFortressOperator(op, castValueInfo);
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

    public Node condition(Where w, List<Condition> conds) throws SemanticException
    {
        checkNotNull(w);
        checkConditions(w, conds);

        final Deque<Condition> stack = new ArrayDeque<Condition>(conds);

        Node tail = stack.peekLast().isElse() ? stack.removeLast().getExpression() : null;
        final ValueInfo tailVI = ((NodeInfo) tail.getUserData()).getValueInfo();

        while(!stack.isEmpty())
        {
            final Condition current = stack.removeLast();

            final Node cond = current.getCondition();
            final ValueInfo condVI = ((NodeInfo) cond.getUserData()).getValueInfo();

            final Node expr = current.getExpression();
            final ValueInfo exprVI = ((NodeInfo) expr.getUserData()).getValueInfo();

            ValueInfo resultVI = exprVI.typeInfoOnly(); // By default
            if (condVI.isConstant())
            {
                final boolean isCondTrue = ((Boolean) condVI.getNativeValue());
                if (isCondTrue)
                {
                    resultVI = exprVI;
                }
                else if (tail != null)
                {
                    resultVI = tailVI;
                }
            }

            tail = new NodeExpr(StandardOperation.ITE, cond, expr, tail);

            final SourceOperator source = new SourceOperator(Operator.ITE, resultVI, resultVI);
            final NodeInfo nodeInfo = NodeInfo.newOperator(source);

            tail.setUserData(nodeInfo);
        }

        return tail;
    }

    public Node evaluateConst(Where w, Node src) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(src);

        final ValueInfo srcValueInfo = ((NodeInfo) src.getUserData()).getValueInfo();

        if (!srcValueInfo.isConstant())
            raiseError(w, ERR_NOT_STATIC);

        return src;
    }

    public Node evaluateSize(Where w, Node src) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(src);

        final ValueInfo srcValueInfo = ((NodeInfo) src.getUserData()).getValueInfo();

        if (!srcValueInfo.isConstant())
            raiseError(w, ERR_NOT_STATIC);

        if (!srcValueInfo.isNativeOf(Integer.class))
            raiseError(w, ERR_NOT_CONST_INTEGER);

        return src;
    }

    public Node evaluateIndex(Where w, Node src) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(src);

        final NodeInfo   srcNodeInfo = (NodeInfo) src.getUserData();
        final ValueInfo srcValueInfo = srcNodeInfo.getValueInfo();

        if (srcValueInfo.isNativeOf(Integer.class))
            return src;

        if (srcValueInfo.isModel())
        {
            final ValueInfo newValueInfo = srcValueInfo.toNativeType(Integer.class);
            final NodeInfo   newNodeInfo = srcNodeInfo.coerceTo(newValueInfo);

            src.setUserData(newNodeInfo);
            return src;
        }

        raiseError(w, ERR_NOT_INDEX);
        return null; // Never executed.
    }

    public Node evaluateLogic(Where w, Node src) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(src);

        final NodeInfo   srcNodeInfo = (NodeInfo) src.getUserData();
        final ValueInfo srcValueInfo = srcNodeInfo.getValueInfo();

        if (srcValueInfo.isNativeOf(Boolean.class))
            return src;

        if (srcValueInfo.isModel())
        {
            final ValueInfo newValueInfo = srcValueInfo.toNativeType(Boolean.class);
            final NodeInfo   newNodeInfo = srcNodeInfo.coerceTo(newValueInfo);

            src.setUserData(newNodeInfo);
            return src;
        }

        raiseError(w, ERR_NOT_BOOLEAN);
        return null; // Never executed.
    }

    public Node evaluateData(Where w, Node src) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(src);

        final NodeInfo   srcNodeInfo = (NodeInfo) src.getUserData();
        final ValueInfo srcValueInfo = srcNodeInfo.getValueInfo();

        if (srcValueInfo.isModel())
            return src;

        assert srcValueInfo.isNativeOf(Integer.class) ||
               srcValueInfo.isNativeOf(Long.class);

        final int size;

        if (srcValueInfo.isConstant())
        {
            final Object value = srcValueInfo.getNativeValue();

            final int usedSize = (Integer.class == value.getClass()) ?
                Integer.SIZE - Integer.numberOfLeadingZeros(((Number) value).intValue()) :
                Long.SIZE - Long.numberOfLeadingZeros(((Number) value).longValue());
                
            int adjustedSize = 1;

            while (adjustedSize < usedSize)
                adjustedSize *= 2;
            
            size = adjustedSize;
        }
        else
        {
            size = (Integer.class == srcValueInfo.getNativeType()) ? Integer.SIZE : Long.SIZE;
        }

        // final Type type = new Type(ETypeID.INT, ExprUtils.createConstant(size));
        // return new ExprNodeCoercion(src, ValueInfo.createModel(type));
        
        // TODO
        return null;
    }

    private static void checkNotNull(Object o)
    {
        if (null == o)
            throw new NullPointerException();
    }

    private void checkConditions(Where w, List<Condition> conds) throws SemanticException
    {
        checkNotNull(conds);

        if (conds.isEmpty())
            throw new IllegalArgumentException("Empty conditions.");

        final Iterator<Condition> it = conds.iterator();

        final Node firstExpression = it.next().getExpression();
        final ValueInfo firstValueInfo = ((NodeInfo) firstExpression.getUserData()).getValueInfo();

        while(it.hasNext())
        {
            final Node currentExpression = it.next().getExpression();
            final ValueInfo currentValueInfo = ((NodeInfo) currentExpression.getUserData()).getValueInfo();

            if (!currentValueInfo.hasEqualType(firstValueInfo))
                raiseError(w, String.format(ERR_TYPE_MISMATCH, currentValueInfo.getTypeName(), firstValueInfo.getTypeName()));
        }
    }

    private static final String ERR_UNSUPPORTED_OPERATOR =
        "The %s operator is not supported.";

    private static final String ERR_OPERAND_NUMBER_MISMATCH =
        "The %s operator requires %d operands.";

    private static final String ERR_TYPE_MISMATCH =
        "%s is unexpected. All parts of the current conditional expression must have the %s type.";

    private static final String ERR_NOT_STATIC =
        "The expression cannot be statically calculated.";

    private static final String ERR_NOT_CONST_INTEGER =
        "The expression cannot be used to specify size since it cannot be evaluated to an integer constant (int).";

    private static final String ERR_NOT_INDEX =
        "The expression cannot be used as an index since it cannot be evaluated to a Java integer (int) value.";

    private static final String ERR_NOT_BOOLEAN =
        "The expression cannot be evaluated to a boolean value (Java boolean).";
}
