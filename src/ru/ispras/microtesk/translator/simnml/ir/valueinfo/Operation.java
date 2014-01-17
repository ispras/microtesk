/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Operation.java, Jan 17, 2014 11:54:10 AM Andrei Tatarnikov
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

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operands;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operator;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

enum Operation
{
    OR (Operator.OR, Arrays.asList(ETypeID.BOOL), 

            new BinaryAction(Boolean.class)
            {
                @Override public Object calculate(Object left, Object right)
                    { return (Boolean) left || (Boolean) right; }
            }
        ),

    AND (Operator.AND, Arrays.asList(ETypeID.BOOL),

            new BinaryAction(Boolean.class)
            {
                @Override public Object calculate(Object left, Object right)
                    { return (Boolean) left && (Boolean) right; }
            }
        ),

    BIT_OR (Operator.BIT_OR, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL),

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

    BIT_XOR (Operator.BIT_XOR, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL),

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

    BIT_AND (Operator.BIT_AND, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL),

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

    EQ (Operator.EQ, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL), Boolean.class,

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

    NOT_EQ (Operator.NOT_EQ, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL), Boolean.class,

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

    LEQ (Operator.LEQ, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT), Boolean.class,

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

    GEQ (Operator.GEQ, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT), Boolean.class,

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

    LESS (Operator.LESS, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT), Boolean.class,

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

    GREATER (Operator.GREATER, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT), Boolean.class,

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

    L_SHIFT (Operator.L_SHIFT, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    R_SHIFT (Operator.R_SHIFT, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    L_ROTATE (Operator.L_ROTATE, Arrays.asList(ETypeID.CARD, ETypeID.INT),

             new BinaryAction(Integer.class)
             {
                 @Override public Object calculate(Object left, Object right)
                     { return Integer.rotateLeft((Integer) left, (Integer) right); }
             }
         ), 

    R_ROTATE (Operator.R_ROTATE, Arrays.asList(ETypeID.CARD, ETypeID.INT),

             new BinaryAction(Integer.class)
             {
                 @Override public Object calculate(Object left, Object right)
                     { return Integer.rotateRight((Integer) left, (Integer) right); }
             }
         ),

    PLUS (Operator.PLUS, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    MINUS (Operator.MINUS, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    MUL (Operator.MUL, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    DIV (Operator.DIV, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    MOD (Operator.MOD, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    POW (Operator.POW, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    UPLUS (Operator.UPLUS, Arrays.asList(ETypeID.CARD, ETypeID.INT),

             new UnaryAction(Integer.class)
             {
                 @Override public Object calculate(Object value) { return (Integer)value; }
             },

             new UnaryAction(Long.class)
             {
                 @Override public Object calculate(Object value) { return (Long) value; }
             }
         ),
           
    UMINUS (Operator.UMINUS, Arrays.asList(ETypeID.CARD, ETypeID.INT),

             new UnaryAction(Integer.class)
             {
                 @Override public Object calculate(Object value) { return -((Integer) value); }
             },

             new UnaryAction(Long.class)
             {
                 @Override public Object calculate(Object value) { return -((Long) value); }
             }
         ),

    BIT_NOT (Operator.BIT_NOT, Arrays.asList(ETypeID.CARD, ETypeID.INT),

             new UnaryAction(Integer.class)
             {
                 @Override public Object calculate(Object value) { return ~((Integer) value); }
             },

             new UnaryAction(Long.class)
             {
                 @Override public Object calculate(Object value) { return ~((Long) value); }
             }
         ),

    NOT (Operator.NOT, Arrays.asList(ETypeID.BOOL),

             new UnaryAction(Integer.class)
             {
                 @Override public Object calculate(Object value) { return !((Boolean) value); }
             }
          )
    ;

    private static final Map<Operator, Operation> map;
    static
    {
        map = new EnumMap<Operator, Operation>(Operator.class);

        for (Operation o : values())
            map.put(o.operator, o);
        
        for (Operator o : Operator.values())
            assert map.containsKey(o) : "No implementation for Operator." + o.name();
    }

    public static Operation forOperator(Operator op)
    {
        return map.get(op);
    }

    private final Operator         operator;

    private final Set<ETypeID>   modelTypes;
    private final Set<Class<?>> nativeTypes;

    private final Type      modelResultType;
    private final Class<?> nativeResultType;

    private Map<Class<?>, Action>   actions;

    private Operation(
        Operator           operator,    
        List<ETypeID>    modelTypes,
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

    private Operation(
        Operator           operator,
        Type        modelResultType,
        List<ETypeID>    modelTypes,
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
