package ru.ispras.microtesk.translator.simnml.ir.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public enum Operation
{
    OR (Operands.BINARY, Arrays.asList(ETypeID.BOOL), 

            new BinaryAction(Boolean.class)
            {
                @Override public Object calculate(Object left, Object right)
                    { return (Boolean) left || (Boolean) right; }
            }
        ),

    AND (Operands.BINARY, Arrays.asList(ETypeID.BOOL),

            new BinaryAction(Boolean.class)
            {
                @Override public Object calculate(Object left, Object right)
                    { return (Boolean) left && (Boolean) right; }
            }
        ),

    BIT_OR (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL),

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

    BIT_XOR  (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL),

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

    BIT_AND (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL),

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

    EQ (Operands.BINARY, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL), Boolean.class,

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

    NOT_EQ (Operands.BINARY, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL), Boolean.class,

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

    LEQ (Operands.BINARY, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT), Boolean.class,

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

    GEQ (Operands.BINARY, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT), Boolean.class,

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

    LESS (Operands.BINARY, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT), Boolean.class,

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

    GREATER (Operands.BINARY, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT), Boolean.class,

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

    L_SHIFT (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    R_SHIFT (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    L_ROTATE (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

             new BinaryAction(Integer.class)
             {
                 @Override public Object calculate(Object left, Object right)
                     { return Integer.rotateLeft((Integer) left, (Integer) right); }
             }
         ), 

    R_ROTATE (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

             new BinaryAction(Integer.class)
             {
                 @Override public Object calculate(Object left, Object right)
                     { return Integer.rotateRight((Integer) left, (Integer) right); }
             }
         ),

    PLUS (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    MINUS (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    MUL (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    DIV (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    MOD (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    POW (Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    UPLUS (Operands.UNARY,  Arrays.asList(ETypeID.CARD, ETypeID.INT),

             new UnaryAction(Integer.class)
             {
                 @Override public Object calculate(Object value) { return (Integer)value; }
             },

             new UnaryAction(Long.class)
             {
                 @Override public Object calculate(Object value) { return (Long) value; }
             }
         ),
           
    UMINUS (Operands.UNARY,  Arrays.asList(ETypeID.CARD, ETypeID.INT),

             new UnaryAction(Integer.class)
             {
                 @Override public Object calculate(Object value) { return -((Integer) value); }
             },

             new UnaryAction(Long.class)
             {
                 @Override public Object calculate(Object value) { return -((Long) value); }
             }
         ),

    BIT_NOT (Operands.UNARY,  Arrays.asList(ETypeID.CARD, ETypeID.INT),

             new UnaryAction(Integer.class)
             {
                 @Override public Object calculate(Object value) { return ~((Integer) value); }
             },

             new UnaryAction(Long.class)
             {
                 @Override public Object calculate(Object value) { return ~((Long) value); }
             }
         ),

    NOT (Operands.UNARY,  Arrays.asList(ETypeID.BOOL),

             new UnaryAction(Integer.class)
             {
                 @Override public Object calculate(Object value) { return !((Boolean) value); }
             }
          )
    ;

    private static abstract class Action
    {
        private final Class<?>     type;
        private final Operands operands;

        public Action(Class<?> type, Operands operands)
        {
            assert null != type; 
            assert null != operands;

            this.type = type;
            this.operands = operands;
        }

        public final Class<?> getType()     { return type; }
        public final Operands getOperands() { return operands; }
    }

    private static abstract class UnaryAction extends Action
    {
        public UnaryAction(Class<?> type) { super(type, Operands.UNARY); }
        public abstract Object calculate(Object value);
    }

    private static abstract class BinaryAction extends Action
    {
        public BinaryAction(Class<?> type) { super(type, Operands.BINARY); }
        public abstract Object calculate(Object left, Object right);
    }

    private final Operands         operands;

    private final Set<ETypeID>   modelTypes;
    private final Set<Class<?>> nativeTypes;

    private final Type      modelResultType;
    private final Class<?> nativeResultType;

    private Map<Class<?>, Action>   actions;

    private Operation(
        Operands           operands,
        List<ETypeID>    modelTypes,
        Action ...    nativeActions
        )
    {
        this(
            operands,
            null,
            modelTypes,
            null,
            nativeActions
            );
    }

    private Operation(
        Operands           operands,
        Type        modelResultType,
        List<ETypeID>    modelTypes,
        Class<?>   nativeResultType,
        Action ...    nativeActions
        )
    {
        assert null != operands;
        assert null != modelTypes;
        assert null != nativeActions;

        this.operands = operands;

        final Set<Class<?>>  nativeTypeSet =
            new HashSet<Class<?>>(nativeActions.length);

        final Map<Class<?>, Action> actionMap =
            new HashMap<Class<?>, Action>(nativeActions.length);

        for (Action action : nativeActions)
        {
            assert action.getOperands() == operands;

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
        return operands.count();
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
        assert action.getOperands().count() == values.size(); 

        if (Operands.UNARY == action.getOperands())
            return ((UnaryAction) action).calculate(values.get(0));

        return ((BinaryAction) action).calculate(values.get(0), values.get(1));
    }
}
