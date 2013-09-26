/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprNodeConst.java, Aug 14, 2013 12:30:39 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

public final class ExprNodeConst extends ExprAbstract
{
    private final int radix;

    ExprNodeConst(NativeValue value, int radix)
    {
        super(NodeKind.CONST, new ValueInfoNative(value));
        this.radix = radix;
    }

    public NativeValue getValue()
    {
        return getValueInfo().getNativeValue();
    }

    public int getRadix()
    {
        return radix;
    }
}
