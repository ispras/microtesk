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

package ru.ispras.microtesk.translator.simnml.ir.expression;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.valueinfo.Operation;

public enum Operator
{
    OR       ("||",     Operation.OR,       Priority.CURRENT),

    AND      ("&&",     Operation.AND,      Priority.HIGHER),

    BIT_OR   ("|",      Operation.BIT_OR,   Priority.HIGHER),
    BIT_XOR  ("^",      Operation.BIT_XOR,  Priority.HIGHER),

    BIT_AND  ("&",      Operation.BIT_AND,  Priority.HIGHER),

    EQ       ("==",     Operation.EQ,       Priority.HIGHER),
    NOT_EQ   ("!=",     Operation.NOT_EQ,   Priority.CURRENT),

    LEQ      ("<=",     Operation.LEQ,      Priority.HIGHER),
    GEQ      (">=",     Operation.GEQ,      Priority.CURRENT),
    LESS     ("<",      Operation.LESS,     Priority.CURRENT),
    GREATER  (">",      Operation.GREATER,  Priority.CURRENT),

    L_SHIFT  ("<<",     Operation.L_SHIFT,  Priority.HIGHER),
    R_SHIFT  (">>",     Operation.R_SHIFT,  Priority.CURRENT),
    L_ROTATE ("<<<",    Operation.L_ROTATE, Priority.CURRENT), 
    R_ROTATE (">>>",    Operation.R_ROTATE, Priority.CURRENT),

    PLUS     ("+",      Operation.PLUS,     Priority.HIGHER),
    MINUS    ("-",      Operation.MINUS,    Priority.CURRENT),

    MUL      ("*",      Operation.MUL,      Priority.HIGHER),
    DIV      ("/",      Operation.DIV,      Priority.CURRENT),
    MOD      ("%",      Operation.MOD,      Priority.CURRENT),

    POW      ("**",     Operation.POW,      Priority.HIGHER),

    UPLUS    ("UPLUS",  Operation.UPLUS,    Priority.HIGHER),
    UMINUS   ("UMINUS", Operation.UMINUS,   Priority.CURRENT),
    BIT_NOT  ("~",      Operation.BIT_NOT,  Priority.CURRENT),
    NOT      ("!",      Operation.NOT,      Priority.CURRENT )
    ;

    private static enum Priority
    {
        CURRENT { @Override public int value() { return   priorityCounter; }},
        HIGHER  { @Override public int value() { return ++priorityCounter; }};

        abstract int value();
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

    public static Operator forText(String text)
    {
        return operators.get(text);
    }

    private final String         text;
    private final Operation operation;
    private final int        priority;

    private Operator(String text, Operation operation, Priority priority)
    {
        assert null != text;
        assert null != operation;
        assert null != priority;

        this.text      = text;
        this.operation = operation;
        this.priority  = priority.value();
    }

    public String text()
    {
        return text;
    }

    public Operation operation()
    {
        return operation;
    }

    public int priority()
    {
        return priority;
    }

    public int operands()
    {
        return operation.operands();
    }
}
