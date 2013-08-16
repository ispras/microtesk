/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExpressionConstrant.java, Aug 14, 2013 12:30:39 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

public final class ExprConstrant extends Expr implements ValueConstant
{
    private final long  value;
    private final int   radix;
    private final int bitSize;

    public ExprConstrant(long value, int radix, int bitSize)
    {
        super(Kind.CONSTANT);

        this.value   = value;
        this.radix   = radix;
        this.bitSize = bitSize;
    }

    @Override
    public long getValue()
    {
        return value;
    }

    public int getRadix()
    {
        return radix;
    }

    @Override
    public int getBitSize()
    {
        return bitSize;
    }

    @Override
    public ValueInfo getValueInfo()
    {
        return this;
    }
}
