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

/**
 * Provides constants that specify the number of operator's operands.
 * 
 * @author Andrei Tatarnikov
 */

public enum Operands
{
    UNARY(1),
    BINARY(2),
    TERNARY(3);

    private final int count;

    private Operands(int count)
    {
        this.count = count;
    }

    /**
     * Returns operand count.
     * 
     * @return Operand count.
     */

    public int count()
    {
        return count;
    }
}
