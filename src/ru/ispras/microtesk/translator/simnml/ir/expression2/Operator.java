/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Operator.java, Aug 14, 2013 12:33:41 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public enum Operator
{
    OR       ("||",  Priority.CURRENT, Operands.BINARY, null),
    AND      ("&&",  Priority.HIGHER,  Operands.BINARY, null),

    BIT_OR   ("|",   Priority.HIGHER,  Operands.BINARY, null),
    BIT_XOR  ("^",   Priority.HIGHER,  Operands.BINARY, null),
    BIT_AND  ("&",   Priority.HIGHER,  Operands.BINARY, null),

    EQ       ("==",  Priority.HIGHER,  Operands.BINARY, null),
    NOT_EQ   ("!=",  Priority.CURRENT, Operands.BINARY, null),

    LEQ      ("<=",  Priority.HIGHER,  Operands.BINARY, null),
    GEQ      (">=",  Priority.CURRENT, Operands.BINARY, null),
    LESS     ("<",   Priority.CURRENT, Operands.BINARY, null),
    GREATER  (">",   Priority.CURRENT, Operands.BINARY, null),

    L_SHIFT  ("<<",  Priority.HIGHER,  Operands.BINARY, null),
    R_SHIFT  (">>",  Priority.CURRENT, Operands.BINARY, null),
    L_ROTATE ("<<<", Priority.CURRENT, Operands.BINARY, null),
    R_ROTATE (">>>", Priority.CURRENT, Operands.BINARY, null),

    PLUS     ("+",   Priority.HIGHER,  Operands.BINARY, null),
    MINUS    ("-",   Priority.CURRENT, Operands.BINARY, null),

    MUL      ("*",   Priority.HIGHER,  Operands.BINARY, null),
    DIV      ("/",   Priority.CURRENT, Operands.BINARY, null), 
    MOD      ("%",   Priority.CURRENT, Operands.BINARY, null),

    POW      ("**",  Priority.HIGHER,  Operands.BINARY, null),

    UPLUS  ("UPLUS", Priority.HIGHER,  Operands.UNARY,  null),
    UMINUS ("UMINUS",Priority.CURRENT, Operands.UNARY,  null),
    BIT_NOT  ("~",   Priority.CURRENT, Operands.UNARY,  null),
    NOT      ("!",   Priority.CURRENT, Operands.UNARY,  null)
    ;

    private static enum Priority
    {
        CURRENT { @Override public int value() { return   priorityCounter; }},
        HIGHER  { @Override public int value() { return ++priorityCounter; }};

        public abstract int value();
        private static int priorityCounter = 0;
    }

    private static final Map<String, Operator> operators;
    static
    {
        final Operator[] ops = Operator.values();
        operators =  new HashMap<String, Operator>(ops.length);

        for (Operator o : ops)
            operators.put(o.text(), o);
    }

    private final String  text;
    private final int priority;
    private final int operands;
    
    private final OperatorLogic logic;

    private Operator(
        String text,
        Priority priority,
        Operands operands,
        OperatorLogic logic
        )
    {
        this.text     = text;
        this.priority = priority.value();
        this.operands = operands.count();
        this.logic    = logic;
    }

    public String text()
    {
        return text;
    }

    public int priority()
    {
        return priority;
    }

    public int operands()
    {
        return operands;
    }
    
    OperatorLogic getLogic()
    {
        return logic; 
    }

    public static Operator forText(String text)
    {
        return operators.get(text);
    }
}

/**
 * Provides constants to specify number of operands used by operators. 
 * 
 * @author Andrei Tatarnikov
 */

enum Operands
{
    UNARY(1),
    BINARY(2);

    Operands(int count) { this.count = count; }
    int count()         { return count; } 

    private final int count;
}

abstract class OperatorLogic
{
    private final Set<ETypeID>   modelTypes;
    private final Set<Class<?>> nativeTypes;

    private final Type      modelResultType;
    private final Class<?> nativeResultType;

    public OperatorLogic(
        List<ETypeID> modelTypes, List<Class<?>> nativeTypes, Type modelResultType, Class<?> nativeResultType)
    {
        this.modelTypes = EnumSet.copyOf(modelTypes);
        this.nativeTypes = new HashSet<Class<?>>(nativeTypes);

        this.modelResultType = modelResultType;
        this.nativeResultType = nativeResultType;
    }

    public OperatorLogic(
        List<ETypeID> modelTypes, List<Class<?>> nativeTypes)
    {
        this(modelTypes, nativeTypes, null, null);
    }

    public final boolean isSupportedFor(ValueInfo value)
    {
        if (!isSupportedFor(value.getValueKind()))
            return false;

        if (ValueKind.MODEL == value.getValueKind())
            return modelTypes.contains(value.getModelType().getTypeId());

        return nativeTypes.contains(value.getNativeType());
    }

    public final boolean isSupportedFor(ValueKind kind)
    {
        if (ValueKind.MODEL == kind)
            return (null != modelTypes) && !modelTypes.isEmpty();

        return (null != nativeTypes) && !nativeTypes.isEmpty();
    }
}

