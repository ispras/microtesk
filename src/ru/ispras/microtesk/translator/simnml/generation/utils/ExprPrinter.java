/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprPrinter.java, Feb 6, 2014 11:33:40 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation.utils;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.data.EOperatorID;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operator;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

public final class ExprPrinter
{
    private ExprPrinter() {}

    public static String toString(Expr expr)
    {
        if (null == expr)
            throw new NullPointerException();

        switch(expr.getNodeInfo().getKind())
        {
            case CONST:
                return constToString(expr);

            case NAMED_CONST:
                return namedConstToString(expr);

            case LOCATION:
                return locationToString(expr);

            case OPERATOR:
                return operatorToString(expr);

            default:
                assert false : "Unknown expression node kind.";
                return "";
        }
    }

    private static String constToString(Expr expr)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private static String namedConstToString(Expr expr)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private static String locationToString(Expr expr)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private static String operatorToString(Expr expr)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private static String getCastString(ValueInfo target, Expr src)
    {
        final String CLASS_NAME = DataEngine.class.getSimpleName();

        if (target.hasEqualType(src.getValueInfo()))
            return toString(src);

        if (target.isModel())
        {
            final String methodName = 
                src.getValueInfo().isModel() ? "coerce" : "valueOf";

            return String.format("%s.%s(%s, %s)",
                CLASS_NAME, methodName, target.getModelType().getJavaText(), toString(src));
        }
        
        assert target.isNative();
        if (src.getValueInfo().isModel())
        {
            final String methodName;

            if (target.isNativeOf(Integer.class))
            {
                methodName = "intValue";
            }
            else if (target.isNativeOf(Long.class))
            {
                methodName = "longValue";
            }
            else if (target.isNativeOf(Boolean.class))
            {
                methodName = "booleanValue";
            }
            else
            {
                assert false : "Cannot coerce to " + target.getTypeName();
                return toString(src);
            }

            return String.format("%s.%s(%s)", CLASS_NAME, methodName, toString(src));
        }
        else
        {
            if (target.isNativeOf(Integer.class))
            {
                return String.format("((int) %s)", toString(src));
            }
            else if (target.isNativeOf(Long.class))
            {
                return String.format("((long) %s)", toString(src));
            }
            else if (target.isNativeOf(Boolean.class))
            {
                return String.format("0 != %s", toString(src));
            }
            else
            {
                assert false : "Cannot coerce to " + target.getTypeName();
                return toString(src);
            }
        }
    }
    
    private static final Map<Operator, EOperatorID> operators = createModelOperators();
    private static Map<Operator, EOperatorID> createModelOperators()
    {
        final Map<Operator, EOperatorID> result =
            new EnumMap<Operator, EOperatorID>(Operator.class);

        result.put(Operator.OR,       EOperatorID.OR);
        result.put(Operator.AND,      EOperatorID.AND);
        result.put(Operator.BIT_OR,   EOperatorID.BIT_OR);
        result.put(Operator.BIT_XOR,  EOperatorID.BIT_XOR);
        result.put(Operator.BIT_AND,  EOperatorID.BIT_AND);
        result.put(Operator.EQ,       EOperatorID.EQ);
        result.put(Operator.NOT_EQ,   EOperatorID.NOT_EQ);
        result.put(Operator.LEQ,      EOperatorID.LESS_EQ);
        result.put(Operator.GEQ,      EOperatorID.GREATER_EQ);
        result.put(Operator.LESS,     EOperatorID.LESS);
        result.put(Operator.GREATER,  EOperatorID.GREATER);
        result.put(Operator.L_SHIFT,  EOperatorID.L_SHIFT);
        result.put(Operator.R_SHIFT,  EOperatorID.R_SHIFT);
        result.put(Operator.L_ROTATE, EOperatorID.L_ROTATE);
        result.put(Operator.R_ROTATE, EOperatorID.R_ROTATE);
        result.put(Operator.PLUS,     EOperatorID.PLUS);
        result.put(Operator.MINUS,    EOperatorID.MINUS);
        result.put(Operator.MUL,      EOperatorID.MUL);
        result.put(Operator.DIV,      EOperatorID.DIV);
        result.put(Operator.MOD,      EOperatorID.MOD);
        result.put(Operator.POW,      EOperatorID.POW);
        result.put(Operator.UPLUS,    EOperatorID.UNARY_PLUS);
        result.put(Operator.UMINUS,   EOperatorID.UNARY_MINUS);
        result.put(Operator.BIT_NOT,  EOperatorID.BIT_NOT);
        result.put(Operator.NOT,      EOperatorID.NOT);

        return Collections.unmodifiableMap(result);
    }

    private static final Map<Operator, String> operatorsNative = createNativeOperators();
    private static Map<Operator, String> createNativeOperators()
    {
        final Map<Operator, String> result = new EnumMap<Operator, String>(Operator.class);

        result.put(Operator.OR,       "%s || %s");
        result.put(Operator.AND,      "%s && %s");
        result.put(Operator.BIT_OR,   "%s | %s");
        result.put(Operator.BIT_XOR,  "%s ^ %s");
        result.put(Operator.BIT_AND,  "%s & %s");
        result.put(Operator.EQ,       "%s == %s");
        result.put(Operator.NOT_EQ,   "%s != %s");
        result.put(Operator.LEQ,      "%s <= %s");
        result.put(Operator.GEQ,      "%s >= %s");
        result.put(Operator.LESS,     "%s < %s");
        result.put(Operator.GREATER,  "%s > %s");
        result.put(Operator.L_SHIFT,  "%s << %s");
        result.put(Operator.R_SHIFT,  "%s >> %s");
        result.put(Operator.L_ROTATE, "Integer.rotateLeft(%s, %s)");
        result.put(Operator.R_ROTATE, "Integer.rotateRight(%s, %s)");
        result.put(Operator.PLUS,     "%s + %s");
        result.put(Operator.MINUS,    "%s - %s");
        result.put(Operator.MUL,      "%s * %s");
        result.put(Operator.DIV,      "%s / %s");
        result.put(Operator.MOD,      "%s % %s");
        result.put(Operator.POW,      "(int)Math.pow(%s, %s)");
        result.put(Operator.UPLUS,    "+%s");
        result.put(Operator.UMINUS,   "-%s");
        result.put(Operator.BIT_NOT,  "~%s");
        result.put(Operator.NOT,      "!%s");

        return result;
    }

    private static String toModelString(Operator op)
    {
        return EOperatorID.class.getSimpleName() + "." + operators.get(op).name();
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
    