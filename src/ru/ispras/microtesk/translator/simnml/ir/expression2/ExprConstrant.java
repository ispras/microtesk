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

import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class ExprConstrant extends Expr
{
    private final long  value;
    private final int   radix;
    private final int bitSize;

    private final class Info implements ValueInfo
    {
        @Override public ValueKind    getKind() { return ValueKind.INTEGER; }
        @Override public int       getBitSize() { return bitSize; }
        @Override public boolean   isConstant() { return true; }
        @Override public long    integerValue() { return value; }
        @Override public boolean booleanValue() { return 0 != value; }
        @Override public Type    locationType() { assert false; return null; }
    }

    private final Info info;

    ExprConstrant(long value, int radix, int bitSize)
    {
        super(Kind.CONSTANT);

        this.value   = value;
        this.radix   = radix;
        this.bitSize = bitSize;
        this.info    = new Info();
    }

    public long getValue()
    {
        return value;
    }

    public int getRadix()
    {
        return radix;
    }

    @Override
    public ValueInfo getValueInfo()
    {
        return info;
    }
}
