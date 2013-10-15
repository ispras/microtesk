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

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public final class ExprFactory extends WalkerFactoryBase
{
    public ExprFactory(WalkerContext context)
    {
        super(context);
    }

    public final Expr namedConstant(Where w, String name) throws SemanticException
    {
        if (!getIR().getConstants().containsKey(name))
            getReporter().raiseError(w, new UndefinedConstant(name));

        final LetConstant constant = getIR().getConstants().get(name);
        return new ExprNodeNamedConst(constant);
    }

    public Expr constant(Where w, String text, int radix) throws SemanticException
    {
        try
        {
            final Integer value = Integer.valueOf(text, radix);
            return new ExprNodeConst(ValueInfo.createNative(value), radix);
        }
        catch (NumberFormatException e) {}

        try
        {
            final Long value = Long.valueOf(text, radix);
            return new ExprNodeConst(ValueInfo.createNative(value), radix);
        }
        catch (NumberFormatException e) {}

        getReporter().raiseError(w, new ValueParsingFailure(text, "Java integer"));
        return null;
    }

    public Expr location(Location location)
    {
        return new ExprNodeLocation(location);
    }

    public Expr coerce(Where w, Expr src, Type type) throws SemanticException
    {
        return new ExprNodeCoercion(src, type);
    }

    public Expr operator(
        Where w, ValueKind target, String id, Expr ... operands) throws SemanticException
    {
        assert Operands.UNARY.count() == operands.length ||
               Operands.BINARY.count() == operands.length;

        final Operator op = Operator.forText(id);

        if (null == op)
            getReporter().raiseError(w, new UnsupportedOperator(id));

        if (operands.length != op.operands())
            getReporter().raiseError(w, new OperandNumberMismatch(id, op.operands()));

        final List<ValueInfo> values =
            new ArrayList<ValueInfo>(operands.length);

        for(Expr operand : operands)
        {
            final ValueInfo vi = operand.getValueInfo();
            assert ValueKind.MODEL == vi.getValueKind() || ValueKind.NATIVE == vi.getValueKind();
            values.add(vi);
        }

        final ValueInfo castValueInfo =
            ValueInfoCast.getCast(target, values);

        if (null == castValueInfo)
            getReporter().raiseError(w, new IncompatibleTypes(values));

        final ValueInfo result = calculate(w, target, op, values);
        return new ExprNodeOperator(op, Arrays.asList(operands), result);
    }

    private ValueInfo calculate(
        Where w, ValueKind target, Operator op, List<ValueInfo> values) throws SemanticException
    {
        final ValueInfoCalculator calculator = new ValueInfoCalculator(this, w, target, op);
        return calculator.calculate(values);
    }
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
