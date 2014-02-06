/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprPrinter.java, Oct 18, 2013 2:17:22 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation.utils;

import java.util.EnumMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.data.EOperatorID;
import ru.ispras.microtesk.translator.simnml.ir.expression_.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression_.ExprNodeCoercion;
import ru.ispras.microtesk.translator.simnml.ir.expression_.ExprNodeCondition;
import ru.ispras.microtesk.translator.simnml.ir.expression_.ExprNodeConst;
import ru.ispras.microtesk.translator.simnml.ir.expression_.ExprNodeLocation;
import ru.ispras.microtesk.translator.simnml.ir.expression_.ExprNodeNamedConst;
import ru.ispras.microtesk.translator.simnml.ir.expression_.ExprNodeOperator;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operator;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operands;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

public final class ExprPrinter_
{
    private ExprPrinter_() {}

    public static String toString(Expr expr)
    {
        assert null != expr;

        switch (expr.getNodeKind())
        {
        case CONST:
            return toString((ExprNodeConst) expr);

        case NAMED_CONST:
            return toString((ExprNodeNamedConst) expr);

        case LOCATION:
            return toString((ExprNodeLocation) expr);

        case COERCION:
            return toString((ExprNodeCoercion) expr);

        case OPERATOR:
            return toString((ExprNodeOperator) expr);
            
        case CONDITION:
            return toString((ExprNodeCondition) expr);

        default:
            assert false : "Unknown expression node kind.";
            break;
        }

        return "";
    }

    private static String toString(ExprNodeConst expr)
    {
        final Object value = expr.getValue();
        final String result;

        if (Integer.class == value.getClass())
        {
            result = (expr.getRadix() == 10) ?
                Integer.toString(((Number) value).intValue()) :
                "0x" + Integer.toHexString((Integer) value);
        }
        else if (Long.class == value.getClass())
        {
            result = (expr.getRadix() == 10) ?
                 Long.toString(((Number) value).longValue()) + "L" :
                 "0x" + Long.toHexString(((Number) value).longValue()) + "L";
        }
        else
        {
            assert false;
            result = value.toString();
        }

        return result;
    }

    private static String toString(ExprNodeNamedConst expr)
    {
        return expr.getConstant().getName();
    }

    private static String toString(ExprNodeLocation expr)
    {
        return LocationPrinter.toString(expr.getLocation()) + ".load()";
    }

    private static String toString(ExprNodeCoercion expr)
    {
        return getCastString(expr.getValueInfo(), expr.getSource());
    }

    private static String getCastString(ValueInfo target, Expr src)
    {
        if (target.hasEqualType(src.getValueInfo()))
            return toString(src);

        if (target.isModel())
        {
            if (src.getValueInfo().isModel())
            {
                return String.format("%s.coerce(%s, %s)",
                    DataEngine.class.getSimpleName(),
                    target.getModelType().getJavaText(),
                    toString(src)
                    );
            }
            else
            {
                return String.format("%s.valueOf(%s, %s)",
                    DataEngine.class.getSimpleName(),
                    target.getModelType().getJavaText(),
                    toString(src)
                    );
            }
        }

        if (src.getValueInfo().isModel())
        {
            if (Integer.class == target.getNativeType())
            {
                return String.format("%s.intValue(%s)",
                    DataEngine.class.getSimpleName(),
                    toString(src)
                    );
            }
            else if (Long.class == target.getNativeType())
            {
                return String.format("%s.longValue(%s)",
                    DataEngine.class.getSimpleName(),
                    toString(src)
                    );
            }
            else if (Boolean.class == target.getNativeType())
            {
                return String.format("%s.booleanValue(%s)",
                    DataEngine.class.getSimpleName(),
                    toString(src)
                    );
            }
            else
            {
                assert false;
                return toString(src);
            }
        }
        else
        {
            if (Integer.class == target.getNativeType())
            {
                return String.format("((int) %s)", toString(src));
            } 
            else if (Long.class == target.getNativeType())
            {
                return String.format("((long) %s)", toString(src));
            }
            else
            {
                assert false;
                return "";
            }
        }
    }

    private static String toString(ExprNodeOperator expr)
    {
        if (expr.getCast().isModel())
        {
            final StringBuilder sb = new StringBuilder();
            for (Expr operand : expr.getOperands())
            {
                sb.append(", ");
                sb.append(getCastString(expr.getCast(), operand));
            }

            return String.format("%s.execute(%s%s)",
                DataEngine.class.getSimpleName(),
                toModelString(expr.getOperator()),
                sb.toString()
                );
        }

        final Operator op = expr.getOperator();

        if (Operands.UNARY.count() == expr.getOperands().size())
        {
            final Expr arg = expr.getOperands().get(0);

            boolean enclose = false;
            if (arg.getNodeKind() == Expr.NodeKind.OPERATOR)
                enclose = ((ExprNodeOperator) arg).getOperator().priority() < op.priority();

            final String argText = getCastString(expr.getCast(), arg);
            return toOperatorString(op, enclose ? "(" + argText + ")" : argText);
        }

        final Expr arg1 = expr.getOperands().get(0);
        final Expr arg2 = expr.getOperands().get(1);

        boolean enclose1 = false;
        if (arg1.getNodeKind() == Expr.NodeKind.OPERATOR)
            enclose1 = ((ExprNodeOperator) arg1).getOperator().priority() < op.priority();

        boolean enclose2 = false;
        if (arg2.getNodeKind() == Expr.NodeKind.OPERATOR)
            enclose2 = ((ExprNodeOperator) arg2).getOperator().priority() < op.priority();

        final String argText1 = getCastString(expr.getCast(), arg1);
        final String argText2 = getCastString(expr.getCast(), arg2);

        return toOperatorString(
            op,
            enclose1 ? "(" + argText1 + ")" : argText1,
            enclose2 ? "(" + argText2 + ")" : argText2
            );
    }
    
    private static String toString(ExprNodeCondition expr)
    {
        return String.format("%s ? %s : %s",
            toString(expr.getCondition()),
            toString(expr.getLeft()),
            toString(expr.getRight())
            ); 
    }

    private static String toModelString(Operator op)
    {
        return EOperatorID.class.getSimpleName() + "." + operators.get(op).name();
    }

    private static final Map<Operator, EOperatorID> operators;
    static 
    {
        operators = new EnumMap<Operator, EOperatorID>(Operator.class);

        operators.put(Operator.OR,       EOperatorID.OR);
        operators.put(Operator.AND,      EOperatorID.AND);
        operators.put(Operator.BIT_OR,   EOperatorID.BIT_OR);
        operators.put(Operator.BIT_XOR,  EOperatorID.BIT_XOR);
        operators.put(Operator.BIT_AND,  EOperatorID.BIT_AND);
        operators.put(Operator.EQ,       EOperatorID.EQ);
        operators.put(Operator.NOT_EQ,   EOperatorID.NOT_EQ);
        operators.put(Operator.LEQ,      EOperatorID.LESS_EQ);
        operators.put(Operator.GEQ,      EOperatorID.GREATER_EQ);
        operators.put(Operator.LESS,     EOperatorID.LESS);
        operators.put(Operator.GREATER,  EOperatorID.GREATER);
        operators.put(Operator.L_SHIFT,  EOperatorID.L_SHIFT);
        operators.put(Operator.R_SHIFT,  EOperatorID.R_SHIFT);
        operators.put(Operator.L_ROTATE, EOperatorID.L_ROTATE);
        operators.put(Operator.R_ROTATE, EOperatorID.R_ROTATE);
        operators.put(Operator.PLUS,     EOperatorID.PLUS);
        operators.put(Operator.MINUS,    EOperatorID.MINUS);
        operators.put(Operator.MUL,      EOperatorID.MUL);
        operators.put(Operator.DIV,      EOperatorID.DIV);
        operators.put(Operator.MOD,      EOperatorID.MOD);
        operators.put(Operator.POW,      EOperatorID.POW);
        operators.put(Operator.UPLUS,    EOperatorID.UNARY_PLUS);
        operators.put(Operator.UMINUS,   EOperatorID.UNARY_MINUS);
        operators.put(Operator.BIT_NOT,  EOperatorID.BIT_NOT);
        operators.put(Operator.NOT,      EOperatorID.NOT);
    }

    private static final Map<Operator, String> operatorsNative;
    static 
    {
        operatorsNative = new EnumMap<Operator, String>(Operator.class);

        operatorsNative.put(Operator.OR,       "%s || %s");
        operatorsNative.put(Operator.AND,      "%s && %s");
        operatorsNative.put(Operator.BIT_OR,   "%s | %s");
        operatorsNative.put(Operator.BIT_XOR,  "%s ^ %s");
        operatorsNative.put(Operator.BIT_AND,  "%s & %s");
        operatorsNative.put(Operator.EQ,       "%s == %s");
        operatorsNative.put(Operator.NOT_EQ,   "%s != %s");
        operatorsNative.put(Operator.LEQ,      "%s <= %s");
        operatorsNative.put(Operator.GEQ,      "%s >= %s");
        operatorsNative.put(Operator.LESS,     "%s < %s");
        operatorsNative.put(Operator.GREATER,  "%s > %s");
        operatorsNative.put(Operator.L_SHIFT,  "%s << %s");
        operatorsNative.put(Operator.R_SHIFT,  "%s >> %s");
        operatorsNative.put(Operator.L_ROTATE, "Integer.rotateLeft(%s, %s)");
        operatorsNative.put(Operator.R_ROTATE, "Integer.rotateRight(%s, %s)");
        operatorsNative.put(Operator.PLUS,     "%s + %s");
        operatorsNative.put(Operator.MINUS,    "%s - %s");
        operatorsNative.put(Operator.MUL,      "%s * %s");
        operatorsNative.put(Operator.DIV,      "%s / %s");
        operatorsNative.put(Operator.MOD,      "%s % %s");
        operatorsNative.put(Operator.POW,      "(int)Math.pow(%s, %s)");
        operatorsNative.put(Operator.UPLUS,    "+%s");
        operatorsNative.put(Operator.UMINUS,   "-%s");
        operatorsNative.put(Operator.BIT_NOT,  "~%s");
        operatorsNative.put(Operator.NOT,      "!%s");
    }

    private static final String toOperatorString(Operator op, String arg)
    {
        return String.format(operatorsNative.get(op), arg);
    }

    private static final String toOperatorString(Operator op, String arg1, String arg2)
    {
        return String.format(operatorsNative.get(op), arg1, arg2);
    }
}
