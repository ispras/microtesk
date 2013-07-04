/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprOperatorBinary.java, Jan 31, 2013 5:34:45 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.data.EOperatorID;
import ru.ispras.microtesk.model.api.type.ETypeID;

final class ExprOperatorBinary extends ExprOperator
{
    public static interface IBinaryOperator extends IOperator
    {
        public Object execute(Object left, Object right);
    }

    private final String javaOpFormat;
    private final Map<Class<?>, IBinaryOperator> javaTypeOps;

    public ExprOperatorBinary(
        String id,
        int priority,
        EOperatorID modelOpID,
        ETypeID[] modelTypes,
        String javaOpFormat,
        IBinaryOperator[] supportedJavaTypeOps,
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

    public Object calculate(Class<?> javaType, Object arg1, Object arg2)
    {
        assert isSupported(javaType);
        assert javaType.equals(getPrimitive(arg1.getClass()));
        assert javaType.equals(getPrimitive(arg2.getClass()));

        final IBinaryOperator operator = javaTypeOps.get(javaType);
        return operator.execute(arg1, arg2);
    }

    public String translate(Class<?> javaType, String arg1, String arg2)
    {
        assert isSupported(javaType);
        return String.format(javaOpFormat, arg1, arg2, javaType);
    }

    public String translate(ETypeID modelType, String arg1, String arg2)
    {
        assert isSupported(modelType);

        return String.format(
            "DataEngine.execute(%s.%s, %s, %s)",
            EOperatorID.class.getSimpleName(),
            getModelOpID().name(),
            arg1,
            arg2
            );
    }
}

/**
* Supported binary operations:
* 
* <pre>
*  OR              '||'
*  
*  AND             '&&'
*  
*  VERT_BAR        '|'
*  
*  UP_ARROW        '^' 
*  
*  AMPER           '&'
*  
*  EQ              '=='
*  NEQ             '!='
*  
*  LEQ             '<='
*  GEQ             '>='
*  LEFT_BROCKET    '<' 
*  RIGHT_BROCKET   '>'
*  
*  LEFT_SHIFT      '<<' 
*  RIGHT_SHIFT     '>>' 
*  ROTATE_LEFT     '<<<'
*  ROTATE_RIGHT    '>>>'
*  
*  PLUS            '+'
*  MINUS           '-'
*  
*  MUL             '*'
*  DIV             '/'
*  REM             '%'
*  
*  DOUBLE_STAR     '**'
*  </pre>
* 
* @author Andrei Tatarnikov
*/

final class ExprBinaryOperators
{
    public static final int BASE_BINARY_PRIORITY = 0;

    private static ExprOperators<ExprOperatorBinary> operators = null;

    public static ExprOperators<ExprOperatorBinary> get()
    {
        if (null == operators)
            operators = createOperators();

        return operators;
    }

    private static ExprOperators<ExprOperatorBinary> createOperators()
    {
        int priority = BASE_BINARY_PRIORITY;

        final ExprOperators<ExprOperatorBinary> result = 
            new ExprOperators<ExprOperatorBinary>();

        result.addOperator(createLogicOR(priority++));

        result.addOperator(createLogicAND(priority++));

        result.addOperator(createBitwiseOR(priority++));

        result.addOperator(createBitwiseXOR(priority++));

        result.addOperator(createBitwiseAND(priority++));

        result.addOperator(createEQ(priority));
        result.addOperator(createNEQ(priority++));

        result.addOperator(createLEQ(priority));
        result.addOperator(createGEQ(priority));
        
        result.addOperator(createLess(priority));
        result.addOperator(createGreater(priority++));

        result.addOperator(createShiftLeft(priority));
        result.addOperator(createShiftRight(priority));
        result.addOperator(createRotateLeft(priority));
        result.addOperator(createRotateRight(priority++));

        result.addOperator(createPlus(priority));
        result.addOperator(createMinus(priority++));

        result.addOperator(createMul(priority));
/*      result.addOperator(createDiv(priority));
        result.addOperator(createMod(priority++));

        result.addOperator(createPow(priority++));
*/
        return result;
    }

    private static ExprOperatorBinary createLogicOR(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[] 
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                    { return int.class; }

                @Override
                public Object execute(Object left, Object right)
                    { return (Integer) 0 != left || (Integer) 0 != right; }
            },

            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                    { return boolean.class; }

                @Override
                public Object execute(Object left, Object right)
                    { return (Boolean) left || (Boolean) right; }
            }
        };

        return new ExprOperatorBinary(
            "||",
            priority,
            EOperatorID.OR,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s || %s",
            ops,
            new ExprOperatorRetType(boolean.class, ETypeID.BOOL)
            );
    }

    private static ExprOperatorBinary createLogicAND(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[] 
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                    { return int.class; }

                @Override
                public Object execute(Object left, Object right)
                    { return (Integer) 0 != left && (Integer) 0 != right; }
            },

            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                    { return boolean.class; }

                @Override
                public Object execute(Object left, Object right)
                    { return (Boolean) left && (Boolean) right; }
            }
        };

        return new ExprOperatorBinary(
            "&&",
            priority,
            EOperatorID.AND,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s && %s",
            ops,
            new ExprOperatorRetType(boolean.class, ETypeID.BOOL)
            );
    }

    private static ExprOperatorBinary createBitwiseOR(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[]
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                {
                    return int.class;
                }

                @Override
                public Object execute(Object left, Object right)
                {
                    return (Integer) left | (Integer) right;
                }
            }
        };

        return new ExprOperatorBinary(
            "|",
            priority,
            EOperatorID.BIT_OR,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s | %s",
            ops,
            null
            );
    }

    private static ExprOperatorBinary createBitwiseXOR(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[]
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                {
                    return int.class;
                }

                @Override
                public Object execute(Object left, Object right)
                {
                    return (Integer) left ^ (Integer) right;
                }
            }
        };

        return new ExprOperatorBinary(
            "^",
            priority,
            EOperatorID.BIT_XOR,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s ^ %s",
            ops,
            null
            );
    }

    private static ExprOperatorBinary createBitwiseAND(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[]
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                {
                    return int.class;
                }

                @Override
                public Object execute(Object left, Object right)
                {
                    return (Integer) left & (Integer) right;
                }
            }
        };

        return new ExprOperatorBinary(
            "&",
            priority,
            EOperatorID.BIT_AND,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s & %s",
            ops,
            null
            );
    }

    private static ExprOperatorBinary createEQ(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[]
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                {
                    return int.class;
                }

                @Override
                public Object execute(Object left, Object right)
                {
                    return (Integer) left == (Integer) right;
                }
            }
        };

        return new ExprOperatorBinary(
            "==",
            priority,
            EOperatorID.EQ,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s == %s",
            ops,
            null
            );
    }

    private static ExprOperatorBinary createNEQ(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[]
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                {
                    return int.class;
                }

                @Override
                public Object execute(Object left, Object right)
                {
                    return (Integer) left != (Integer) right;
                }
            }
        };

        return new ExprOperatorBinary(
            "!=",
            priority,
            EOperatorID.NOT_EQ,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s != %s",
            ops,
            null
            );
    }
    
    private static ExprOperatorBinary createLEQ(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[]
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                {
                    return int.class;
                }

                @Override
                public Object execute(Object left, Object right)
                {
                    return (Integer) left <= (Integer) right;
                }
            }
        };

        return new ExprOperatorBinary(
            "<=",
            priority,
            EOperatorID.LESS_EQ,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s <= %s",
            ops,
            null
            );
    }

    private static ExprOperatorBinary createGEQ(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[]
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                {
                    return int.class;
                }

                @Override
                public Object execute(Object left, Object right)
                {
                    return (Integer) left >= (Integer) right;
                }
            }
        };

        return new ExprOperatorBinary(
            ">=",
            priority,
            EOperatorID.GREATER_EQ,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s >= %s",
            ops,
            null
            );
    }
    
    private static ExprOperatorBinary createLess(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[]
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                {
                    return int.class;
                }

                @Override
                public Object execute(Object left, Object right)
                {
                    return (Integer) left < (Integer) right;
                }
            }
        };

        return new ExprOperatorBinary(
            "<",
            priority,
            EOperatorID.LESS,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s < %s",
            ops,
            null
            );
    }

    private static ExprOperatorBinary createGreater(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[]
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                {
                    return int.class;
                }

                @Override
                public Object execute(Object left, Object right)
                {
                    return (Integer) left > (Integer) right;
                }
            }
        };

        return new ExprOperatorBinary(
            ">",
            priority,
            EOperatorID.GREATER,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s > %s",
            ops,
            null
            );
    }

    private static ExprOperatorBinary createShiftLeft(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[] 
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                    { return int.class; }

                @Override
                public Object execute(Object left, Object right)
                    { return (Integer) left << (Integer) right; }
            }
        };

        return new ExprOperatorBinary(
            "<<",
            priority,
            EOperatorID.L_SHIFT,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s << %s",
            ops,
            null // No special return type (matches the argument type).
            );
    }

    private static ExprOperatorBinary createShiftRight(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[] 
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                    { return int.class; }

                @Override
                public Object execute(Object left, Object right)
                    { return (Integer) left >> (Integer) right; }
            }
        };

        return new ExprOperatorBinary(
            ">>",
            priority,
            EOperatorID.R_SHIFT,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s >> %s",
            ops,
            null // No special return type (matches the argument type).
            );
    }

    private static ExprOperatorBinary createRotateLeft(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[] 
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                    { return int.class; }

                @Override
                public Object execute(Object left, Object right)
                   { return Integer.rotateLeft((Integer) left, (Integer) right); }
            }
        };

        return new ExprOperatorBinary(
            "<<<",
            priority,
            EOperatorID.L_ROTATE,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "Integer.rotateLeft(%s, %s)",
            ops,
            null // No special return type (matches the argument type).
            );
    }

    private static ExprOperatorBinary createRotateRight(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[] 
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                    { return int.class; }

                @Override
                public Object execute(Object left, Object right)
                    { return Integer.rotateRight((Integer) left, (Integer) right); }
            }
        };

        return new ExprOperatorBinary(
            ">>>",
            priority,
            EOperatorID.R_ROTATE,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "Integer.rotateRight(%s, %s)",
            ops,
            null // No special return type (matches the argument type).
            );
    }

    private static ExprOperatorBinary createPlus(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[] 
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                    { return int.class; }

                @Override
                public Object execute(Object left, Object right)
                    { return (Integer) left + (Integer) right; }
            },
        };

        return new ExprOperatorBinary(
            "+",
            priority,
            EOperatorID.PLUS,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s + %s",
            ops,
            null // No special return type (matches the argument type).
            );
    }
    
    private static ExprOperatorBinary createMinus(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[]
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                {
                    return int.class;
                }

                @Override
                public Object execute(Object left, Object right)
                {
                    return (Integer) left - (Integer) right;
                }
            }
        };

        return new ExprOperatorBinary(
            "-",
            priority,
            EOperatorID.MINUS,
            new ETypeID[] { ETypeID.CARD, ETypeID.INT },
            "%s - %s",
            ops,
            null // No special return type (matches the argument type).
            );
    }

    private static ExprOperatorBinary createMul(int priority)
    {
        final ExprOperatorBinary.IBinaryOperator[] ops = new ExprOperatorBinary.IBinaryOperator[]
        {
            new ExprOperatorBinary.IBinaryOperator()
            {
                @Override
                public Class<?> getJavaType()
                {
                    return int.class;
                }

                @Override
                public Object execute(Object left, Object right)
                {
                    return (Integer) left * (Integer) right;
                }
            }
        };

        return new ExprOperatorBinary(
            "*",
            priority,
            EOperatorID.MUL,
            new ETypeID[] {ETypeID.CARD, ETypeID.INT},
            "%s * %s",
            ops,
            null // No special return type (matches the argument type).
            );
    }

/*    
    private static ExprOperatorBinary createDiv(int priority)
    {
        return null;
    }

    private static ExprOperatorBinary createMod(int priority)
    {
        return null;
    }

    private static ExprOperatorBinary createPow(int priority)
    {
        return null;
    }
*/
}
