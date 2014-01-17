/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprUtils.java, Oct 17, 2013 4:28:29 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import java.util.Arrays;

import ru.ispras.microtesk.translator.simnml.ir.valueinfo.Operands;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

public final class ExprUtils
{
    private ExprUtils() {} 

    public static int integerValue(Expr expr)
    {
        assert null != expr;
        final ValueInfo vi = expr.getValueInfo();

        if (vi.isConstant() && Integer.class == vi.getNativeType())
            return ((Number) vi.getNativeValue()).intValue();

        assert false : "Not a constant integer value.";
        return 0;
    }

    public static Expr createConstant(int value)
    {
        return new ExprNodeConst(value, 10);
    }

    public static class ReducedExpr
    {
        public final int   constant;
        public final Expr polynomial;

        private ReducedExpr(int c, Expr p)
        {
            this.constant   = c;
            this.polynomial = p;
        }
    }

    public static ReducedExpr reduce(Expr expr)
    {
        assert null != expr;

        if (expr.getValueInfo().isConstant())
            return new ReducedExpr(integerValue(expr), null);

        switch(expr.getNodeKind())
        {
            case LOCATION:
                return new ReducedExpr(0, expr); // Return without changes.

            case COERCION:
                return reduceCoercion((ExprNodeCoercion) expr);

            case OPERATOR:
                return reduceOp((ExprNodeOperator) expr);

            default: // CONST, NAMED_CONST (covered by the isConstant branch in the beginning of the method) and unknown kinds.
                assert false; // Should never happen!
                return new ReducedExpr(0, expr); // Return without changes.
        }
    }

    private static ReducedExpr reduceCoercion(ExprNodeCoercion coercion)
    {
        // If it is just a cast from model to native, we skip it.
        if (coercion.getValueInfo().isNative() && 
            coercion.getSource().getValueInfo().isModel())
        {
            return reduce(coercion.getSource());
        }

        // Otherwise, we return the expression without changes.
        return new ReducedExpr(0, coercion);
    }

    private static ReducedExpr reduceOp(ExprNodeOperator operator)
    {
        if (Operator.PLUS != operator.getOperator() &&
            Operator.MINUS != operator.getOperator())
        {
            return new ReducedExpr(0, operator); // Return without changes.
        }

        final boolean isPlus = Operator.PLUS == operator.getOperator();
        assert operator.getOperator().operands() == Operands.BINARY.count();

        final ReducedExpr left  = reduce(operator.getOperands().get(0));
        final ReducedExpr right = reduce(operator.getOperands().get(1));

        final int constant = isPlus ? left.constant + right.constant :
                                      left.constant - right.constant;

        if (null != left.polynomial && null != right.polynomial)
        {
            final Expr newPolynomial = new ExprNodeOperator(
                operator.getOperator(),
                Arrays.asList(left.polynomial, right.polynomial),
                operator.getValueInfo(),
                operator.getCast()
                );

            return new ReducedExpr(constant, newPolynomial);
        }

        if (null == left.polynomial)
        {
            if (isPlus)
                return new ReducedExpr(constant, right.polynomial);

            final Expr newPolynomial = new ExprNodeOperator(
                Operator.UMINUS,
                Arrays.asList(right.polynomial),
                right.polynomial.getValueInfo(),
                right.polynomial.getValueInfo());

            return new ReducedExpr(constant, newPolynomial);
        }

        return new ReducedExpr(constant, left.polynomial);
    }
}
