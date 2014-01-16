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
    OR       (Priority.CURRENT, Operands.BINARY, Arrays.asList(ETypeID.BOOL), 

                 new BinaryAction(Boolean.class)
                 {
                     @Override public Object calculate(Object left, Object right)
                         { return (Boolean) left || (Boolean) right; }
                 }
             ),
    
    AND      (Priority.HIGHER,  Operands.BINARY, Arrays.asList(ETypeID.BOOL),

                 new BinaryAction(Boolean.class)
                 {
                     @Override public Object calculate(Object left, Object right)
                          { return (Boolean) left && (Boolean) right; }
                 }
             ),
            

    BIT_OR   (Priority.HIGHER,  Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL),

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
            
    BIT_XOR  (Priority.HIGHER,  Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL),

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

    BIT_AND  (Priority.HIGHER,  Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL),

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

    EQ       (Priority.HIGHER,  Operands.BINARY, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL), Boolean.class,

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

    NOT_EQ   (Priority.CURRENT, Operands.BINARY, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT, ETypeID.BOOL), Boolean.class,

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

    LEQ      (Priority.HIGHER, Operands.BINARY, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT), Boolean.class,

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

    GEQ      (Priority.CURRENT, Operands.BINARY, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT), Boolean.class,

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

    LESS     (Priority.CURRENT, Operands.BINARY, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT), Boolean.class,

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

    GREATER  (Priority.CURRENT, Operands.BINARY, Type.BOOLEAN, Arrays.asList(ETypeID.CARD, ETypeID.INT), Boolean.class,

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

    L_SHIFT  (Priority.HIGHER,  Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    R_SHIFT  (Priority.CURRENT, Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    L_ROTATE (Priority.CURRENT, Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

                 new BinaryAction(Integer.class)
                 {
                     @Override public Object calculate(Object left, Object right)
                         { return Integer.rotateLeft((Integer) left, (Integer) right); }
                 }
             ), 

    R_ROTATE (Priority.CURRENT, Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

                 new BinaryAction(Integer.class)
                 {
                     @Override public Object calculate(Object left, Object right)
                         { return Integer.rotateRight((Integer) left, (Integer) right); }
                 }
             ),

    PLUS     (Priority.HIGHER,  Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    MINUS    (Priority.CURRENT, Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    MUL      (Priority.HIGHER,  Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    DIV      (Priority.CURRENT, Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    MOD      (Priority.CURRENT, Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    POW      (Priority.HIGHER,  Operands.BINARY, Arrays.asList(ETypeID.CARD, ETypeID.INT),

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

    UPLUS    (Priority.HIGHER,  Operands.UNARY,  Arrays.asList(ETypeID.CARD, ETypeID.INT),

                 new UnaryAction(Integer.class)
                 {
                     @Override public Object calculate(Object value) { return (Integer)value; }
                 },

                 new UnaryAction(Long.class)
                 {
                     @Override public Object calculate(Object value) { return (Long) value; }
                 }
             ),
           
           
    UMINUS   (Priority.CURRENT, Operands.UNARY,  Arrays.asList(ETypeID.CARD, ETypeID.INT),

                 new UnaryAction(Integer.class)
                 {
                     @Override public Object calculate(Object value) { return -((Integer) value); }
                 },

                 new UnaryAction(Long.class)
                 {
                     @Override public Object calculate(Object value) { return -((Long) value); }
                 }
             ),

    BIT_NOT  (Priority.CURRENT, Operands.UNARY,  Arrays.asList(ETypeID.CARD, ETypeID.INT),

                 new UnaryAction(Integer.class)
                 {
                     @Override public Object calculate(Object value) { return ~((Integer) value); }
                 },

                 new UnaryAction(Long.class)
                 {
                     @Override public Object calculate(Object value) { return ~((Long) value); }
                 }
             ),

    NOT      (Priority.CURRENT, Operands.UNARY,  Arrays.asList(ETypeID.BOOL),

                 new UnaryAction(Integer.class)
                 {
                     @Override public Object calculate(Object value) { return !((Boolean) value); }
                 }
              )
    ;

    private static enum Priority
    {
        CURRENT { @Override public int value() { return   priorityCounter; }},
        HIGHER  { @Override public int value() { return ++priorityCounter; }};

        public abstract int value();
        private static int priorityCounter = 0;
    }

    private final int priority;
    private final int operands;
    private final Logic  logic;

    private Operation(
        Priority           priority,
        Operands           operands,
        List<ETypeID>    modelTypes,
        Action ...    nativeActions
        )
    {
        this(
            priority,
            operands,
            null,
            modelTypes,
            null,
            nativeActions
            );
    }

    private Operation(
        Priority           priority,
        Operands           operands,
        Type        modelResultType,
        List<ETypeID>    modelTypes,
        Class<?>   nativeResultType,
        Action ...    nativeActions
        )
    {
        assert null != priority;
        assert null != operands;

        assert null != modelTypes;
        assert null != nativeActions;

        this.priority = priority.value();
        this.operands = operands.count();

        final Set<Class<?>> nativeTypeSet =
            new HashSet<Class<?>>(nativeActions.length);

        final Map<Class<?>, Action> actionMap =
            new HashMap<Class<?>, Action>(nativeActions.length);

        for (Action action : nativeActions)
        {
            assert action.getOperands() == operands;

            nativeTypeSet.add(action.getType());
            actionMap.put(action.getType(), action);
        }

        this.logic = new Logic(
            EnumSet.copyOf(modelTypes), nativeTypeSet, modelResultType, nativeResultType, actionMap);
    }

    public int priority()
    {
        return priority;
    }

    public int operands()
    {
        return operands;
    }

    public ValueInfo calculate(ValueInfo cast, List<ValueInfo> values)
    { 
        return logic.calculate(cast, values);
    }

    public boolean isSupportedFor(ValueInfo value)
    {
        return logic.isSupportedFor(value);
    }

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
    
    private static final class Logic
    {
        private final Set<ETypeID>   modelTypes;
        private final Set<Class<?>> nativeTypes;

        private final Type      modelResultType;
        private final Class<?> nativeResultType;

        private Map<Class<?>, Action>   actions;

        public Logic(
            Set<ETypeID>       modelTypes,
            Set<Class<?>>     nativeTypes,
            Type          modelResultType,
            Class<?>     nativeResultType,
            Map<Class<?>, Action> actions
            )
        {
            this.modelTypes = modelTypes;
            this.nativeTypes = nativeTypes;

            this.modelResultType = modelResultType;
            this.nativeResultType = nativeResultType;

            this.actions = actions; 
        }

        public boolean isSupportedFor(ValueInfo value)
        {
            if (value.isNative())
                return nativeTypes.contains(value.getNativeType());
            
            return modelTypes.contains(value.getModelType().getTypeId());
        }

        public ValueInfo calculate(ValueInfo castValueInfo, List<ValueInfo> values)
        {
            assert isSupportedFor(castValueInfo);

            if (castValueInfo.isModel())
            {
                return (null != modelResultType) ?
                    ValueInfo.createModel(modelResultType) : castValueInfo;
            }

            if (!allValuesConstant(values))
            {
                return (null != nativeResultType) ?
                    ValueInfo.createNativeType(nativeResultType) : castValueInfo;
            }

            final List<Object> nativeValues = new ArrayList<Object>(values.size()); 
            for (ValueInfo vi : values)
                nativeValues.add(vi.getNativeValue());

            final Object result = calculateNative(castValueInfo.getNativeType(), nativeValues);
            return ValueInfo.createNative(result);
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
}
