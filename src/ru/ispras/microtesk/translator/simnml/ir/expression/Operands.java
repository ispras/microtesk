/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Operands.java, Jan 16, 2014 3:32:16 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

public enum Operands
{
    UNARY(1),
    BINARY(2),
    TERNARY(3);

    private Operands(int count) { this.count = count; }
    public int count()          { return count; }

    private final int count;
}
