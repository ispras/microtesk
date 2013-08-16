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

public enum OperatorID
{
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
    AND,
    OR,
    NOT
}
