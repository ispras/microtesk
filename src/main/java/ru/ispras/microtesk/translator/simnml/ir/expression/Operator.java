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

/**
 * Contains descriptions of Sim-nML operators.
 * 
 * @author Andrei Tatarnikov
 */

public enum Operator
{
    OR       ("||",     Operands.BINARY,  Priority.CURRENT),

    AND      ("&&",     Operands.BINARY,  Priority.HIGHER),

    BIT_OR   ("|",      Operands.BINARY,  Priority.HIGHER),
    BIT_XOR  ("^",      Operands.BINARY,  Priority.HIGHER),

    BIT_AND  ("&",      Operands.BINARY,  Priority.HIGHER),

    EQ       ("==",     Operands.BINARY,  Priority.HIGHER),
    NOT_EQ   ("!=",     Operands.BINARY,  Priority.CURRENT),

    LEQ      ("<=",     Operands.BINARY,  Priority.HIGHER),
    GEQ      (">=",     Operands.BINARY,  Priority.CURRENT),
    LESS     ("<",      Operands.BINARY,  Priority.CURRENT),
    GREATER  (">",      Operands.BINARY,  Priority.CURRENT),

    L_SHIFT  ("<<",     Operands.BINARY,  Priority.HIGHER),
    R_SHIFT  (">>",     Operands.BINARY,  Priority.CURRENT),
    L_ROTATE ("<<<",    Operands.BINARY,  Priority.CURRENT), 
    R_ROTATE (">>>",    Operands.BINARY,  Priority.CURRENT),

    PLUS     ("+",      Operands.BINARY,  Priority.HIGHER),
    MINUS    ("-",      Operands.BINARY,  Priority.CURRENT),

    MUL      ("*",      Operands.BINARY,  Priority.HIGHER),
    DIV      ("/",      Operands.BINARY,  Priority.CURRENT),
    MOD      ("%",      Operands.BINARY,  Priority.CURRENT),

    POW      ("**",     Operands.BINARY,  Priority.HIGHER),

    UPLUS    ("UPLUS",  Operands.UNARY,   Priority.HIGHER),
    UMINUS   ("UMINUS", Operands.UNARY,   Priority.CURRENT),
    BIT_NOT  ("~",      Operands.UNARY,   Priority.CURRENT),
    NOT      ("!",      Operands.UNARY,   Priority.CURRENT),

    // Synthetic operator
    ITE      ("",       Operands.TERNARY, Priority.HIGHER)
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

    /**
     * Returns an operator that corresponds to the specified text.  
     * 
     * @param text Textual representation of the operator.
     * @return Operator.
     */

    public static Operator forText(String text)
    {
        return operators.get(text);
    }

    private final String  text;
    private final int operands;
    private final int priority;

    private Operator(String text, Operands operands, Priority priority)
    {
        if (null == text)
            throw new NullPointerException();

        if (null == operands)
            throw new NullPointerException();

        if (null == priority)
            throw new NullPointerException();

        this.text      = text;
        this.operands  = operands.count();
        this.priority  = priority.value();
    }

    /**
     * Returns textual representation of the operator. 
     * 
     * @return Operator text.
     */

    public String text()
    {
        return text;
    }

    /**
     * Returns relative priority of the operator. 
     *  
     * @return Operator priority.
     */

    public int priority()
    {
        return priority;
    }

    /**
     * Returns operand number.
     * 
     * @return Operand number.
     */

    public int operands()
    {
        return operands;
    }
}
