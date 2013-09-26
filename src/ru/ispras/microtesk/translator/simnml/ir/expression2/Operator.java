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

import java.util.HashMap;
import java.util.Map;

public enum Operator
{
    OR       ("||",  Priority.CURRENT, Operands.BINARY),
    AND      ("&&",  Priority.HIGHER,  Operands.BINARY),

    BIT_OR   ("|",   Priority.HIGHER,  Operands.BINARY),
    BIT_XOR  ("^",   Priority.HIGHER,  Operands.BINARY),
    BIT_AND  ("&",   Priority.HIGHER,  Operands.BINARY),

    EQ       ("==",  Priority.HIGHER,  Operands.BINARY),
    NOT_EQ   ("!=",  Priority.CURRENT, Operands.BINARY),

    LEQ      ("<=",  Priority.HIGHER,  Operands.BINARY),
    GEQ      (">=",  Priority.CURRENT, Operands.BINARY),
    LESS     ("<",   Priority.CURRENT, Operands.BINARY),
    GREATER  (">",   Priority.CURRENT, Operands.BINARY),

    L_SHIFT  ("<<",  Priority.HIGHER,  Operands.BINARY),
    R_SHIFT  (">>",  Priority.CURRENT, Operands.BINARY),
    L_ROTATE ("<<<", Priority.CURRENT, Operands.BINARY),
    R_ROTATE (">>>", Priority.CURRENT, Operands.BINARY),

    PLUS     ("+",   Priority.HIGHER,  Operands.BINARY),
    MINUS    ("-",   Priority.CURRENT, Operands.BINARY),

    MUL      ("*",   Priority.HIGHER,  Operands.BINARY),
    DIV      ("/",   Priority.CURRENT, Operands.BINARY), 
    MOD      ("%",   Priority.CURRENT, Operands.BINARY),

    POW      ("**",  Priority.HIGHER,  Operands.BINARY),


    UPLUS  ("UPLUS", Priority.HIGHER,  Operands.UNARY),
    UMINUS ("UMINUS",Priority.CURRENT, Operands.UNARY),
    BIT_NOT  ("~",   Priority.CURRENT, Operands.UNARY),
    NOT      ("!",   Priority.CURRENT, Operands.UNARY)
    ;

    private static enum Operands
    {
        UNARY(1),
        BINARY(2);

        Operands(int value) { this.value = value; }
        int value()         { return value; } 

        private final int value;
    }

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

    private Operator(
        String text,
        Priority priority,
        Operands operands
        )
    {
        this.text = text;
        this.priority = priority.value();
        this.operands = operands.value();
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

    public static Operator forText(String text)
    {
        return operators.get(text);
    }
}
