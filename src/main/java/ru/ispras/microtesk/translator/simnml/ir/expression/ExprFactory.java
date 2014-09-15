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

package ru.ispras.microtesk.translator.simnml.ir.expression;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;

import ru.ispras.microtesk.model.api.type.TypeId;
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

/**
 * The ExprFactory class is a factory responsible for constructing expressions.
 * 
 * @author Andrei Tatarnikov
 */

public final class ExprFactory extends WalkerFactoryBase
{
    private final ValueInfoCalculator calculator;

    /**
     * Constructor for an expression factory. 
     *  
     * @param context Provides facilities for interacting with the tree walker. 
     */

    public ExprFactory(WalkerContext context)
    {
        super(context);
        this.calculator = new ValueInfoCalculator(context);
    }
    
    /**
     * Creates an expression based on a named constant. 
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param name Name of the constant.
     * @return New expression.
     *  
     * @throws NullPointerException if any of the parameters is null.
     * @throws SemanticException if a constant with such name is not defined.  
     */

    public Expr namedConstant(Where w, String name) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(name);

        if (!getIR().getConstants().containsKey(name))
            raiseError(w, new UndefinedConstant(name));

        final LetConstant source = getIR().getConstants().get(name);
        final NodeInfo  nodeInfo = NodeInfo.newNamedConst(source);

        final Data data = Converter.toFortressData(nodeInfo.getValueInfo());
        final Node node = new NodeValue(data);

        node.setUserData(nodeInfo);
        return new Expr(node);
    }
    

    /**
     * Creates an expression based on a numeric literal.
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param text Textual representation of a constant.
     * @param radix Radix used to convert text to a number.
     * @return New expression.
     * 
     * @throws NullPointerException if any of the parameters is null.
     * @throws SemanticException if the specified text cannot be converted to a number (due to incorrect format). 
     */

    public Expr constant(Where w, String text, int radix) throws SemanticException
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
        final Node node = new NodeValue(data);

        node.setUserData(nodeInfo);
        return new Expr(node);
    }

    /**
     * Creates an expression based on a location.
     * 
     * @param location Location object.
     * @return New expression.
     * 
     * @throws NullPointerException if the parameter is null.
     */

    public Expr location(Location source)
    {
        checkNotNull(source);

        final NodeInfo nodeInfo = NodeInfo.newLocation(source);

        final String name = "unnamed"; // TODO
        final Data data = Converter.toFortressData(nodeInfo.getValueInfo());

        final Variable variable = new Variable(name, data);
        final Node node = new NodeVariable(variable);

        node.setUserData(nodeInfo);
        return new Expr(node);
    }
    
    /**
     * Creates an operator-based expression. 
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param target Preferable value kind (needed to calculate value and type of the result, when mixed operand types are used). 
     * @param id Operator identifier. 
     * @param operands Operand expressions.
     * @return New expression.
     * 
     * @throws NullPointerException if any of the parameters is null.
     * @throws SemanticException if factory fails to create a valid expression (unsupported operator, incompatible types, etc.).
     */

    public Expr operator(Where w, ValueInfo.Kind target, String id, Expr ... operands) throws SemanticException
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
        final Node[]    operandNodes = new Node[operands.length];

        for (int index = 0; index < operands.length; ++index)
        {
            final Expr operand = operands[index];

            // All Model API values that have the BOOL type are cast to 
            // Java boolean values for the sake of simplicity.

            if (operand.getValueInfo().isModelOf(TypeId.BOOL))
            {
                final ValueInfo newValueInfo = operand.getValueInfo().toNativeType(Boolean.class);
                final NodeInfo   newNodeInfo = operand.getNodeInfo().coerceTo(newValueInfo);

                operand.setNodeInfo(newNodeInfo);
            }

            values.add(operand.getValueInfo());
            operandNodes[index] = operand.getNode();
        }

        final ValueInfo   castValueInfo = calculator.cast(w, target, values);
        final ValueInfo resultValueInfo = calculator.calculate(w, op, castValueInfo, values);

        final SourceOperator source = new SourceOperator(op, castValueInfo, resultValueInfo);
        final NodeInfo nodeInfo = NodeInfo.newOperator(source);

        final Enum<?> operator = Converter.toFortressOperator(op, castValueInfo);
        final Node node = new NodeOperation(operator, operandNodes);

        node.setUserData(nodeInfo);
        return new Expr(node);
    }

    /**
     * Performs type coercion. Source expression is coerced to a Model API type.
     * 
     * @param src Source expression (its attributes will be modified).
     * @param type Target type.
     * @return Coerced expression.
     * 
     * @throws NullPointerException if any of the parameters is null.
     */

    public Expr coerce(Where w, Expr src, Type type)
    {
        checkNotNull(w);
        checkNotNull(src);
        checkNotNull(type);

        final ValueInfo srcValueInfo = src.getValueInfo();

        if (srcValueInfo.isModel() && type.equals(srcValueInfo.getModelType()))
            return src;

        final ValueInfo newValueInfo = ValueInfo.createModel(type);
        final NodeInfo   newNodeInfo = src.getNodeInfo().coerceTo(newValueInfo);

        src.setNodeInfo(newNodeInfo);
        return src;
    }

    /**
     * Performs type coercion. Source expression is coerced to a Native Java type.
     * 
     * @param src Source expression (its attributes will be modified).
     * @param type Target type.
     * @return Coerced expression.
     * 
     * @throws NullPointerException if any of the parameters is null.
     */

    public Expr coerce(Where w, Expr src, Class<?> type)
    {
        checkNotNull(w);
        checkNotNull(src);
        checkNotNull(type);

        final ValueInfo srcValueInfo = src.getValueInfo();

        if (srcValueInfo.isNativeOf(type))
            return src;

        final ValueInfo newValueInfo = srcValueInfo.toNativeType(type);
        final NodeInfo   newNodeInfo = src.getNodeInfo().coerceTo(newValueInfo);

        src.setNodeInfo(newNodeInfo);
        return src;
    }

    /**
     * Creates a conditional expression based on the if-elif-else construction. The returned
     * expression is represented by a hierarchy of expressions based on the ITE ternary operator. 
     * 
     * @param Position in a source file (needed for error reporting).
     * @param conds List of conditions and expressions associated with them. The 'else' condition is the last item if presents.
     * @return New expression.
     * 
     * @throws NullPointerException if any of the parameters is null.
     * @throws IllegalArgumentException if the list of conditions is empty.
     * @throws SemanticException if the types of expressions to be selected do not match.
     */

    public Expr condition(Where w, List<Condition> conds) throws SemanticException
    {
        checkNotNull(w);
        checkConditions(w, conds);

        final Deque<Condition> stack = new ArrayDeque<Condition>(conds);

        Expr tail = stack.peekLast().isElse() ? stack.removeLast().getExpression() : null;
        final ValueInfo tailVI = tail.getValueInfo();

        while(!stack.isEmpty())
        {
            final Condition current = stack.removeLast();

            final Expr cond = current.getCondition();
            final Expr expr = current.getExpression();

            final ValueInfo condVI = cond.getValueInfo();
            final ValueInfo exprVI = expr.getValueInfo();

            ValueInfo resultVI = exprVI.typeInfoOnly(); // By default
            if (condVI.isConstant())
            {
                final boolean isCondTrue =
                    ((Boolean) condVI.getNativeValue());

                if (isCondTrue)
                {
                    resultVI = exprVI;
                }
                else if (tail != null)
                {
                    resultVI = tailVI;
                }
            }

            final SourceOperator source = new SourceOperator(Operator.ITE, resultVI, resultVI);
            final NodeInfo nodeInfo = NodeInfo.newOperator(source);

            final Node node = new NodeOperation(StandardOperation.ITE, cond.getNode(), expr.getNode(), tail.getNode());
            node.setUserData(nodeInfo);

            tail = new Expr(node);
        }

        return tail;
    }

    private void checkConditions(Where w, List<Condition> conds) throws SemanticException
    {
        checkNotNull(conds);

        if (conds.isEmpty())
            throw new IllegalArgumentException("Empty conditions.");

        final Iterator<Condition> it = conds.iterator();

        final Expr     firstExpression = it.next().getExpression();
        final ValueInfo firstValueInfo = firstExpression.getValueInfo();

        while(it.hasNext())
        {
            final Expr     currentExpression = it.next().getExpression();
            final ValueInfo currentValueInfo = currentExpression.getValueInfo();

            if (!currentValueInfo.hasEqualType(firstValueInfo))
                raiseError(w, String.format(ERR_TYPE_MISMATCH, currentValueInfo.getTypeName(), firstValueInfo.getTypeName()));
        }
    }

    /**
     * Checks whether the specified expression is a constant expression. Constant expressions
     * are statically calculated at translation time and are represented by constant Java
     * values (currently, "int" or "long"). If the source expression is not constant,
     * an exception is raised.
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param src Source expression (returned if meets the conditions).
     * @return Constant expression.
     * 
     * @throws NullPointerException if any of the parameters is null.
     * @throws SemanticException if the expression is not constant.
     */

    public Expr evaluateConst(Where w, Expr src) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(src);

        final ValueInfo srcValueInfo = src.getValueInfo();

        if (!srcValueInfo.isConstant())
            raiseError(w, ERR_NOT_STATIC);

        return src;
    }

    /**
     * Checks whether the specified expression is a size expression. Size expressions
     * are constant expressions represented by Java integer values (int). If the source
     * expression is not a size expression, an exception is raised.
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param src Source expression (returned if meets the conditions).
     * @return Size expression.
     * 
     * @throws NullPointerException if any of the parameters is null.
     * @throws SemanticException if the expression does not meet the requirements for a size expression.
     */

    public Expr evaluateSize(Where w, Expr src) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(src);

        final ValueInfo srcValueInfo = src.getValueInfo();

        if (!srcValueInfo.isConstant())
            raiseError(w, ERR_NOT_STATIC);

        if (!srcValueInfo.isNativeOf(Integer.class))
            raiseError(w, ERR_NOT_CONST_INTEGER);

        return src;
    }

    /**
     * Evaluates the specified expression to an index expression. The result of such an expression
     * is a Java integer value (int). If the source expression cannot be evaluated to an index
     * expression, an exception is raised. It it can, a required cast is performed. 
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param src Source expression (its attributes are modified if required).
     * @return Index expression.
     * 
     * @throws NullPointerException if any of the parameters is null.
     * @throws SemanticException if the expression does not meet the requirements for an index expression.
     */

    public Expr evaluateIndex(Where w, Expr src) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(src);

        final ValueInfo srcValueInfo = src.getValueInfo();

        if (srcValueInfo.isNativeOf(Integer.class))
            return src;

        if (srcValueInfo.isModel())
        {
            final ValueInfo newValueInfo = srcValueInfo.toNativeType(Integer.class);
            final NodeInfo   newNodeInfo = src.getNodeInfo().coerceTo(newValueInfo);

            src.setNodeInfo(newNodeInfo);
            return src;
        }

        raiseError(w, ERR_NOT_INDEX);
        return null; // Never executed.
    }

    /**
     * Evaluates the specified expression to an logic expression. The result of such an expression
     * is a Java boolean value (boolean). If the source expression cannot be evaluated
     * to a logic expression, an exception is raised. It it can, a required cast is performed. 
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param src Source expression (its attributes are modified if required).
     * @return Logic expression.
     * 
     * @throws NullPointerException if any of the parameters is null.
     * @throws SemanticException if the expression does not meet the requirements for a logic expression.
     */

    public Expr evaluateLogic(Where w, Expr src) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(src);

        final ValueInfo srcValueInfo = src.getValueInfo();

        if (srcValueInfo.isNativeOf(Boolean.class))
            return src;

        if (srcValueInfo.isModel())
        {
            final ValueInfo newValueInfo = srcValueInfo.toNativeType(Boolean.class);
            final NodeInfo   newNodeInfo = src.getNodeInfo().coerceTo(newValueInfo);

            src.setNodeInfo(newNodeInfo);
            return src;
        }

        raiseError(w, ERR_NOT_BOOLEAN);
        return null; // Never executed.
    }

    /**
     * Evaluates the specified expression to a Model API data expression. If the source
     * expression is represented by a native Java expression, a required cast is performed.
     * If the cast is not supported (e.g due to incompatible data types), an exception is raised.
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param src Source expression (its attributes are modified if required).
     * @return Model API data expression.
     * 
     * @throws NullPointerException if any of the parameters is null.
     * @throws SemanticException if the expression cannot be converted to a Model API
     * data expression (e.g. incompatible data types).
     */

    public Expr evaluateData(Where w, Expr src) throws SemanticException
    {
        checkNotNull(w);
        checkNotNull(src);

        final ValueInfo srcValueInfo = src.getValueInfo();

        if (srcValueInfo.isModel())
            return src;

        if (!srcValueInfo.isNativeOf(Integer.class) && !srcValueInfo.isNativeOf(Long.class))
            raiseError(w, String.format(ERR_NOT_LOCATION_COMPATIBLE, srcValueInfo.getTypeName()));

        final int size;
        if (srcValueInfo.isConstant())
        {
            final Number value = (Number) srcValueInfo.getNativeValue();

            final int usedSize = (Integer.class == value.getClass()) ?
                Integer.SIZE - Integer.numberOfLeadingZeros(value.intValue()) :
                Long.SIZE - Long.numberOfLeadingZeros(value.longValue());

            int adjustedSize = 1;

            while (adjustedSize < usedSize)
                adjustedSize *= 2;

            size = adjustedSize;
        }
        else
        {
            size = (Integer.class == srcValueInfo.getNativeType()) ?
                Integer.SIZE : Long.SIZE;
        }

        final Type type = new Type(TypeId.INT, size);

        final ValueInfo newValueInfo = ValueInfo.createModel(type);
        final NodeInfo   newNodeInfo = src.getNodeInfo().coerceTo(newValueInfo);

        src.setNodeInfo(newNodeInfo);
        return src;
    }

    private static void checkNotNull(Object o)
    {
        if (null == o)
            throw new NullPointerException();
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

    private static final String ERR_NOT_LOCATION_COMPATIBLE =
        "The %s type cannot be stored in a location.";
}
