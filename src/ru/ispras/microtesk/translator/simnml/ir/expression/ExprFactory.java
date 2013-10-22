/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactory.java, Aug 14, 2013 12:00:36 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.SemanticError;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedConstant;
import ru.ispras.microtesk.translator.simnml.errors.ValueParsingFailure;
import ru.ispras.microtesk.translator.simnml.ir.expression.Location;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

import static ru.ispras.microtesk.translator.simnml.ir.expression.Operator.Operands.*;

/**
 * The ExprFactory class is a factory responsible for constructing expressions.
 * 
 * @author Andrei Tatarnikov
 */

public final class ExprFactory extends WalkerFactoryBase
{
    /**
     * Constructor for an expression factory. 
     *  
     * @param context Provides facilities for interacting with the tree walker. 
     */

    public ExprFactory(WalkerContext context)
    {
        super(context);
    }
    
    /**
     * Creates an expression based on a named constant. 
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param name Name of the constant.
     * @return Expression. 
     * @throws SemanticException Raised if a constant with such name is not defined.  
     */

    public Expr namedConstant(Where w, String name) throws SemanticException
    {
        if (!getIR().getConstants().containsKey(name))
            raiseError(w, new UndefinedConstant(name));

        final LetConstant constant = getIR().getConstants().get(name);
        return new ExprNodeNamedConst(constant);
    }

    /**
     * Creates an expression based on a numeric literal.
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param text Textual representation of a constant.
     * @param radix Radix used to convert text to a number.
     * @return Expression.
     * @throws SemanticException Raised if the specified text cannot be converted to a number (due to incorrect format). 
     */

    public Expr constant(Where w, String text, int radix) throws SemanticException
    {
        try
        {
            final Integer value = Integer.valueOf(text, radix);
            return new ExprNodeConst(value, radix);
        }
        catch (NumberFormatException e) {}

        try
        {
            final Long value = Long.valueOf(text, radix);
            return new ExprNodeConst(value, radix);
        }
        catch (NumberFormatException e) {}

        raiseError(w, new ValueParsingFailure(text, "Java integer"));
        return null; // Cannot be reached.
    }

    /**
     * Creates an expression based on a location.
     * 
     * @param location Location object.
     * @return Expression.
     */

    public Expr location(Location location)
    {
        return new ExprNodeLocation(location);
    }

    /**
     * Creates a type coercion expression. Source expression is coerced to a Model API type.
     * 
     * @param src Source expression.
     * @param type Target type.
     * @return Expression.
     */

    public Expr coerce(Where w, Expr src, Type type)
    {
        if (src.getValueInfo().isModel() && type.equals(src.getValueInfo().getModelType()))
            return src;

        return new ExprNodeCoercion(src, type);
    }

    /**
     * Creates a type coercion expression. Source expression is coerced to a Native Java type.
     * 
     * @param src Source expression.
     * @param type Target type.
     * @return Expression.
     */

    public Expr coerce(Where w, Expr src, Class<?> type)
    {
        if (src.getValueInfo().isNative() && type == src.getValueInfo().getNativeType())
            return src;

        return new ExprNodeCoercion(src, type);
    }

    /**
     * Creates an expression based on an unary or binary operation. 
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param target Preferable value kind (needed to calculate value and type of the result, when mixed operand types are used). 
     * @param id Identifier for the operator. 
     * @param operands Operand expressions (one or two).
     * @return Expression.
     * @throws SemanticException Raised if factory fails to create a valid expression (unsupported operator, incompatible types, etc.).
     */

    public Expr operator(
        Where w, ValueKind target, String id, Expr ... operands) throws SemanticException
    {
        assert UNARY.count() == operands.length || BINARY.count() == operands.length;

        final Operator op = Operator.forText(id);

        if (null == op)
            raiseError(w, new UnsupportedOperator(id));

        if (operands.length != op.operands())
            raiseError(w, new OperandNumberMismatch(id, op.operands()));

        final List<ValueInfo> values = new ArrayList<ValueInfo>(operands.length);

        for(Expr operand : operands)
        {
            final ValueInfo vi = operand.getValueInfo();
            values.add(vi);
        }

        final ValueInfo castValueInfo = ValueInfoCast.getCast(target, values);

        if (null == castValueInfo)
            raiseError(w, new IncompatibleTypes(values));

        if (!op.isSupportedFor(castValueInfo))
            raiseError(w, new UnsupportedOperandType(op, castValueInfo));

        final ValueInfo resultValueInfo = op.calculate(castValueInfo, values);

        return new ExprNodeOperator(
            op, Arrays.asList(operands), resultValueInfo, castValueInfo);
    }
    
    /**
     * The evaluateConst method evaluates the given expression to a constant expression.
     * Constant expressions are statically calculated at translation time and are 
     * represented by constant Java values (currently, "int" or "long").
     * If the source expression is not constant, an exception is raised.
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param src Source expression.
     * @return Expression.
     * @throws SemanticException Raised if the expression is not constant.
     */

    public Expr evaluateConst(Where w, Expr src) throws SemanticException
    {
        if (!src.getValueInfo().isConstant())
            raiseError(w, ERR_NOT_STATIC);

        return src; 
    }

    /**
     * The evaluateSize method evaluates the given expression to a size expression.
     * Size expressions must be represented by constant integer values and have the Java "int" type.
     * If the source expressions does not meet the necessary conditions, an exception is raised. 
     * 
     * @param w Position in a source file (needed for error reporting).
     * @param src Source expression.
     * @return Expression.
     * @throws SemanticException Raised if the expression cannot be evaluated to an "int" constant.
     */
    
    public Expr evaluateSize(Where w, Expr src) throws SemanticException
    {
        if (!src.getValueInfo().isConstant())
            raiseError(w, ERR_NOT_STATIC);

        if (Integer.class != src.getValueInfo().getNativeType())
            raiseError(w, ERR_NOT_CONST_INTEGER);

        return src; 
    }
    
    /**
     * 
     * @param w
     * @param src
     * @return
     * @throws SemanticException
     */
    
    public Expr evaluateIndex(Where w, Expr src) throws SemanticException
    {
        final ValueInfo vi = src.getValueInfo();

        if (vi.isNative() && Integer.class == vi.getNativeType())
            return src;

        if (vi.isModel())
            return new ExprNodeCoercion(src, Integer.class);

        raiseError(w, ERR_NOT_INDEX);
        return null; // Never executed.
    }

    /**
     * 
     * @param w
     * @param src
     * @return
     * @throws SemanticException
     */
    
    public Expr evaluateLogic(Where w, Expr src) throws SemanticException
    {
        return src;
    }
    
    /**
     * 
     * @param w
     * @param src
     * @return
     * @throws SemanticException
     */
    
    public Expr evaluateData(Where w, Expr src) throws SemanticException
    {
        if (src.getValueInfo().isModel())
            return src;

        assert Integer.class == src.getValueInfo().getNativeType() ||
               Long.class == src.getValueInfo().getNativeType();
        
        final int size;

        if (src.getValueInfo().isConstant())
        {
            final Object value = src.getValueInfo().getNativeValue();

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
            size = (Integer.class == src.getValueInfo().getNativeType()) ? Integer.SIZE : Long.SIZE;
        }

        final Type type = new Type(ETypeID.INT, ExprUtils.createConstant(size));
        return new ExprNodeCoercion(src, type);
    }
    
    private static final String ERR_NOT_STATIC =
        "The expression cannot be statically calculated.";

    private static final String ERR_NOT_CONST_INTEGER =
        "The expression cannot be used to specify size since it cannot be evaluated to an integer constant (int).";
    
    private static final String ERR_NOT_INDEX =
        "The expression cannot be used as an index since it cannot be evaluated to a Java integer (int) value.";
}

final class UnsupportedOperator extends SemanticError
{
    private static final String FORMAT = "The %s operator is not supported.";

    public UnsupportedOperator(String op)
    {
        super(String.format(FORMAT, op));
    }
}

final class OperandNumberMismatch extends SemanticError
{
    private static final String FORMAT = "The %s operator requires %d operands.";

    public OperandNumberMismatch(String op, int operands)
    {
        super(String.format(FORMAT, op, operands));
    }
}

final class IncompatibleTypes implements ISemanticError
{
    private static final String FORMAT = "Incompatible types: %s.";

    private final List<ValueInfo> values;

    public IncompatibleTypes(List<ValueInfo> values)
    {
        this.values = values;
    }

    @Override
    public String getMessage()
    {
        final StringBuilder sb = new StringBuilder();

        for (ValueInfo vi : values)
        {
            if (sb.length() != 0) sb.append(", ");
            sb.append(vi.getTypeName());
        }

        return String.format(FORMAT, sb.toString());
    }
}

final class UnsupportedOperandType extends SemanticError
{
    private static final String FORMAT = "The %s type is not supported by the %s operator.";

    public UnsupportedOperandType(Operator op, ValueInfo vi)
    {
        super(String.format(FORMAT, vi.getTypeName(), op.text()));
    }
}
