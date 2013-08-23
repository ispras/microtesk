/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprOperator.java, Aug 14, 2013 12:33:41 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

public enum Operator
{
    OR ("||", Priority.current(), 2),
    AND("&&", Priority.higher(),  2),

    // Arithmetical operators
    PLUS,
    MINUS,
    UNARY_PLUS,
    UNARY_MINUS,
    MUL,
    DIV,
    MOD,
    POW,

    // Comparison operators
    GREATER,
    LESS,
    GREATER_EQ,
    LESS_EQ,
    EQ,
    NOT_EQ,

    // Bitwise operators
    L_SHIFT,
    R_SHIFT,
    BIT_AND,
    BIT_OR,
    BIT_XOR,
    BIT_NOT,
    L_ROTATE,
    R_ROTATE,

    // Logical operators
    //AND,
    // OR,
    NOT;

    private final String  text;
    private final int priority;
    private final int operands;

    private Operator()
    {
        this("", 0, 0);
    }

    private Operator(
        String text,
        int priority,
        int operands
        )
    {
        this.text     = text;
        this.priority = priority;
        this.operands = operands;
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
        return null;
    }

    public static Operator forTextUnary(String text)
    {
        return null;
    }
}

final class Priority
{
    private Priority() {}
    private static int value = 0;

    public static int current() { return value; }
    public static int  higher() { return ++value; }
}