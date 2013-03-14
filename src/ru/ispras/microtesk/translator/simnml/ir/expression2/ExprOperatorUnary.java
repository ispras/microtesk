/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprOperatorUnary.java, Jan 31, 2013 6:44:54 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.data.EOperatorID;
import ru.ispras.microtesk.model.api.type.ETypeID;

final class ExprOperatorUnary extends ExprOperator
{
    public static interface IUnaryOperator extends IOperator
    {
        public Object execute(Object arg);
    }

    private final String javaOpFormat;
    private final Map<Class<?>, IUnaryOperator> javaTypeOps;

    public ExprOperatorUnary(
        String id,
        int priority,
        EOperatorID modelOpID,
        ETypeID[] modelTypes,
        String javaOpFormat,
        IUnaryOperator[] supportedJavaTypeOps,
        ExprOperatorRetType retType
        )
    {
        super(id, priority, modelOpID, modelTypes, retType);

        this.javaOpFormat = javaOpFormat;
        this.javaTypeOps = createJavaTypeOps(supportedJavaTypeOps);
    }

    @Override
    protected Set<Class<?>> getJavaTypes()
    {
        return javaTypeOps.keySet();
    }

    public Object calculate(Class<?> javaType, Object arg)
    {
        assert isSupported(javaType);
        assert javaType.equals(getPrimitive(arg.getClass()));

        final IUnaryOperator operator = javaTypeOps.get(javaType);
        return operator.execute(arg);
    }

    public String translate(Class<?> javaType, String arg)
    {
        assert isSupported(javaType);
        return String.format(javaOpFormat, arg, javaType);
    }

    public String translate(ETypeID modelType, String arg)
    {
        assert isSupported(modelType);

        return String.format(
            "DataEngine.execute(%s.%s, %s)",
            EOperatorID.class.getSimpleName(),
            getModelOpID().name(),
            arg
            );
    }
}

/**
* Supported unary operations:
* 
* <pre>
*  UNARY_PLUS      "UNARY_PLUS"
*  UNARY_MINUS     "UNARY_MINUS"
*  TILDE           "~"
*  NOT             "!"
* </pre>
* 
* @author Andrei Tatarnikov
*/

final class ExprUnaryOperators
{
    public static final int BASE_UNARY_PRIORITY = 100;

    private static ExprOperators<ExprOperatorUnary> operators = null;

    public static ExprOperators<ExprOperatorUnary> get()
    {
        if (null == operators)
            operators = createOperators();

        return operators;
    }

    private static ExprOperators<ExprOperatorUnary> createOperators()
    {
        final int priority = BASE_UNARY_PRIORITY;

        final ExprOperators<ExprOperatorUnary> result =
            new ExprOperators<ExprOperatorUnary>();

        result.addOperator(createUnaryPlus(priority));
        result.addOperator(createUnaryMinus(priority));
        result.addOperator(createBitwiseNot(priority)); 
        result.addOperator(createLogicNot(priority));

        return result;
    }

    private static ExprOperatorUnary createUnaryPlus(int priority)
    {
        final ExprOperatorUnary.IUnaryOperator[] ops = new ExprOperatorUnary.IUnaryOperator[] 
        {
            new ExprOperatorUnary.IUnaryOperator()
            {
                @Override
                public Class<?> getJavaType() { return int.class; }

                @Override
                public Object execute(Object arg) { return (Integer) arg; }                
            }
        };

        return new ExprOperatorUnary(
            "UNARY_PLUS",
            priority,
            EOperatorID.UNARY_PLUS,
            new ETypeID[] { ETypeID.INT, ETypeID.CARD },
            "+ %s",
            ops,
            null // No special return type.
            );
    }

    private static ExprOperatorUnary createUnaryMinus(int priority)
    {
        final ExprOperatorUnary.IUnaryOperator[] ops = new ExprOperatorUnary.IUnaryOperator[] 
        {
            new ExprOperatorUnary.IUnaryOperator()
            {
                @Override
                public Class<?> getJavaType() { return int.class; }

                @Override
                public Object execute(Object arg) { return - (Integer) arg; }                
            }
        };

        return new ExprOperatorUnary(
            "UNARY_MINUS",
            priority,
            EOperatorID.UNARY_MINUS,
            new ETypeID[] { ETypeID.INT, ETypeID.CARD },
            "- %s",
            ops,
            null // No special return type.
            );
    }

    private static ExprOperatorUnary createBitwiseNot(int priority)
    {
        final ExprOperatorUnary.IUnaryOperator[] ops = new ExprOperatorUnary.IUnaryOperator[] 
        {
            new ExprOperatorUnary.IUnaryOperator()
            {
                @Override
                public Class<?> getJavaType() { return int.class; }

                @Override
                public Object execute(Object arg) { return ~ (Integer) arg; }                
            }
        };

        return new ExprOperatorUnary(
            "~",
            priority,
            EOperatorID.BIT_NOT,
            new ETypeID[] { ETypeID.INT, ETypeID.CARD },
            "~ %s",
            ops,
            null // No special return type.
            );
    }

    private static ExprOperatorUnary createLogicNot(int priority)
    {
        final ExprOperatorUnary.IUnaryOperator[] ops = new ExprOperatorUnary.IUnaryOperator[] 
        {
            new ExprOperatorUnary.IUnaryOperator()
            {
                @Override
                public Class<?> getJavaType() { return int.class; }

                @Override
                public Object execute(Object arg) { return ((Integer) arg == 0) ? -1 : 0; }                
            }
        };

        return new ExprOperatorUnary(
            "!",
            priority,
            EOperatorID.NOT,
            new ETypeID[] { ETypeID.INT, ETypeID.CARD },
            "! %s",
            ops,
            null // No special return type.
            );
    }
}
