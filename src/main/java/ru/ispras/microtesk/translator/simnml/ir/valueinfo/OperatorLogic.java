/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * OperatorLogic.java, Jan 17, 2014 11:54:10 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.valueinfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operands;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operator;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

enum OperatorLogic
{
    OR (Operator.OR, Arrays.asList(TypeId.BOOL), 

        new BinaryAction(Boolean.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Boolean) left || (Boolean) right; }
        }
    ),

    AND (Operator.AND, Arrays.asList(TypeId.BOOL),

        new BinaryAction(Boolean.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Boolean) left && (Boolean) right; }
        }
    ),

    BIT_OR (Operator.BIT_OR, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.BOOL),

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Integer) left | (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Long) left | (Long) right; }
        },

        new BinaryAction(Boolean.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Boolean) left | (Boolean) right; }
        }
    ),

    BIT_XOR (Operator.BIT_XOR, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.BOOL),

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Integer) left ^ (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Long) left ^ (Long) right; }
        },

        new BinaryAction(Boolean.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Boolean) left ^ (Boolean) right; }
        }
    ),

    BIT_AND (Operator.BIT_AND, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.BOOL),

         new BinaryAction(Integer.class)
         {
             @Override public Object calculate(Object left, Object right)
                 { return (Integer) left & (Integer) right; }
         },

         new BinaryAction(Long.class)
         {
             @Override public Object calculate(Object left, Object right)
                 { return (Long) left & (Long) right; }
         },

         new BinaryAction(Boolean.class)
         {
             @Override public Object calculate(Object left, Object right)
                 { return (Boolean) left & (Boolean) right; }
         }
    ),

    EQ (Operator.EQ, Type.BOOLEAN, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT, TypeId.BOOL), Boolean.class,

         new BinaryAction(Integer.class)
         {
             @Override public Object calculate(Object left, Object right)
                 { return (Integer) left == (Integer) right; }
         },

         new BinaryAction(Long.class)
         {
             @Override public Object calculate(Object left, Object right)
                 { return (Long) left == (Long) right; }
         },

         new BinaryAction(Boolean.class)
         {
             @Override public Object calculate(Object left, Object right)
                 { return (Boolean) left == (Boolean) right; }
         }
    ),

    NOT_EQ (Operator.NOT_EQ, Type.BOOLEAN, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.BOOL), Boolean.class,

         new BinaryAction(Integer.class)
         {
             @Override public Object calculate(Object left, Object right)
                { return (Integer) left != (Integer) right; }
         },

         new BinaryAction(Long.class)
         {
             @Override public Object calculate(Object left, Object right)
                { return (Long) left != (Long) right; }
         },

         new BinaryAction(Boolean.class)
         {
             @Override public Object calculate(Object left, Object right)
                 { return (Boolean) left != (Boolean) right; }
         }
    ),

    LEQ (Operator.LEQ, Type.BOOLEAN, Arrays.asList(TypeId.CARD, TypeId.INT), Boolean.class,

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Integer) left <= (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Long) left <= (Long) right; }
        }
    ),

    GEQ (Operator.GEQ, Type.BOOLEAN, Arrays.asList(TypeId.CARD, TypeId.INT), Boolean.class,

        new BinaryAction(Integer.class)
        {
             @Override public Object calculate(Object left, Object right)
                 { return (Integer) left >= (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
             @Override public Object calculate(Object left, Object right)
                 { return (Long) left >= (Long) right; }
        }
    ),

    LESS (Operator.LESS, Type.BOOLEAN, Arrays.asList(TypeId.CARD, TypeId.INT), Boolean.class,

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Integer) left < (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Long) left < (Long) right; }
        }
    ),

    GREATER (Operator.GREATER, Type.BOOLEAN, Arrays.asList(TypeId.CARD, TypeId.INT), Boolean.class,

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Integer) left > (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Long) left > (Long) right; }
        }
    ),

    L_SHIFT (Operator.L_SHIFT, Arrays.asList(TypeId.CARD, TypeId.INT),

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Integer) left << (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Long) left << (Long) right; }
        }
    ),

    R_SHIFT (Operator.R_SHIFT, Arrays.asList(TypeId.CARD, TypeId.INT),

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Integer) left >> (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Long) left >> (Long) right; }
        }
    ),

    L_ROTATE (Operator.L_ROTATE, Arrays.asList(TypeId.CARD, TypeId.INT),

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return Integer.rotateLeft((Integer) left, (Integer) right); }
        }
    ), 

    R_ROTATE (Operator.R_ROTATE, Arrays.asList(TypeId.CARD, TypeId.INT),

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return Integer.rotateRight((Integer) left, (Integer) right); }
        }
    ),

    PLUS (Operator.PLUS, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Integer) left + (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Long) left + (Long) right; }
        }
    ),

    MINUS (Operator.MINUS, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Integer) left - (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Long) left - (Long) right; }
        }
    ),

    MUL (Operator.MUL, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Integer) left * (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
               { return (Long) left * (Long) right; }
        }
    ),

    DIV (Operator.DIV, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Integer) left / (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Long) left / (Long) right; }
        }
     ),

    MOD (Operator.MOD, Arrays.asList(TypeId.CARD, TypeId.INT),

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Integer) left % (Integer) right; }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (Long) left % (Long) right; }
        }
    ),

    POW (Operator.POW, Arrays.asList(TypeId.CARD, TypeId.INT),

        new BinaryAction(Integer.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (int) Math.pow((Integer) left, (Integer) right); }
        },

        new BinaryAction(Long.class)
        {
            @Override public Object calculate(Object left, Object right)
                { return (int) Math.pow((Long) left, (Long) right); }
        }
    ),

    UPLUS (Operator.UPLUS, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),

        new UnaryAction(Integer.class)
        {
            @Override public Object calculate(Object value) { return (Integer)value; }
        },

        new UnaryAction(Long.class)
        {
            @Override public Object calculate(Object value) { return (Long) value; }
        }
    ),
           
    UMINUS (Operator.UMINUS, Arrays.asList(TypeId.CARD, TypeId.INT, TypeId.FLOAT),

        new UnaryAction(Integer.class)
        {
            @Override public Object calculate(Object value) { return -((Integer) value); }
        },

        new UnaryAction(Long.class)
        {
            @Override public Object calculate(Object value) { return -((Long) value); }
        }
    ),

    BIT_NOT (Operator.BIT_NOT, Arrays.asList(TypeId.CARD, TypeId.INT),

        new UnaryAction(Integer.class)
        {
            @Override public Object calculate(Object value) { return ~((Integer) value); }
        },

        new UnaryAction(Long.class)
        {
            @Override public Object calculate(Object value) { return ~((Long) value); }
        }
    ),

    NOT (Operator.NOT, Arrays.asList(TypeId.BOOL),

        new UnaryAction(Integer.class)
        {
            @Override public Object calculate(Object value) { return !((Boolean) value); }
        }
    ),
    
    ITE (Operator.ITE, Arrays.asList(TypeId.BOOL, TypeId.CARD, TypeId.INT)
    );

    private static final Map<Operator, OperatorLogic> operators;
    static
    {
        operators = new EnumMap<Operator, OperatorLogic>(Operator.class);

        for (OperatorLogic ol : values())
            operators.put(ol.operator, ol);
        
        for (Operator o : Operator.values())
            assert operators.containsKey(o) : "No implementation for Operator." + o.name();
    }

    public static OperatorLogic forOperator(Operator op)
    {
        return operators.get(op);
    }

    private final Operator         operator;

    private final Set<TypeId>   modelTypes;
    private final Set<Class<?>> nativeTypes;

    private final Type      modelResultType;
    private final Class<?> nativeResultType;

    private Map<Class<?>, Action>   actions;

    private OperatorLogic(
        Operator           operator,    
        List<TypeId>    modelTypes,
        Action ...    nativeActions
        )
    {
        this(
            operator,
            null,
            modelTypes,
            null,
            nativeActions
            );
    }

    private OperatorLogic(
        Operator           operator,
        Type        modelResultType,
        List<TypeId>    modelTypes,
        Class<?>   nativeResultType,
        Action ...    nativeActions
        )
    {
        assert null != operator;
        assert null != modelTypes;
        assert null != nativeActions;

        this.operator = operator;

        final Set<Class<?>>  nativeTypeSet =
            new HashSet<Class<?>>(nativeActions.length);

        final Map<Class<?>, Action> actionMap =
            new HashMap<Class<?>, Action>(nativeActions.length);

        for (Action action : nativeActions)
        {
            assert action.getOperands() == operator.operands();

            nativeTypeSet.add(action.getType());
            actionMap.put(action.getType(), action);
        }

        this.modelTypes       = EnumSet.copyOf(modelTypes);
        this.nativeTypes      = nativeTypeSet;

        this.modelResultType  = modelResultType;
        this.nativeResultType = nativeResultType;

        this.actions          = actionMap; 
    }

    public int operands()
    {
        return operator.operands();
    }

    public ValueInfo calculate(ValueInfo cast, List<ValueInfo> values)
    {
        assert isSupportedFor(cast);

        if (cast.isModel())
        {
            return (null != modelResultType) ?
                ValueInfo.createModel(modelResultType) : cast;
        }

        if (!allValuesConstant(values))
        {
            return (null != nativeResultType) ?
                ValueInfo.createNativeType(nativeResultType) : cast;
        }

        final List<Object> nativeValues = new ArrayList<Object>(values.size()); 

        for (ValueInfo vi : values)
            nativeValues.add(vi.getNativeValue());

        final Object result = calculateNative(cast.getNativeType(), nativeValues);
        return ValueInfo.createNative(result);
    }

    public boolean isSupportedFor(ValueInfo value)
    {
        if (value.isNative())
            return nativeTypes.contains(value.getNativeType());
        
        return modelTypes.contains(value.getModelType().getTypeId());
    }

    private static boolean allValuesConstant(List<ValueInfo> values)
    {
        for (ValueInfo vi : values)
            if (!vi.isConstant()) return false;

        return true;
    }

    private Object calculateNative(Class<?> type, List<Object> values)
    {
        final Action action = actions.get(type);

        assert null != action;
        assert action.getOperands() == values.size(); 

        if (Operands.UNARY.count() == action.getOperands())
            return ((UnaryAction) action).calculate(values.get(0));

        return ((BinaryAction) action).calculate(values.get(0), values.get(1));
    }
}
