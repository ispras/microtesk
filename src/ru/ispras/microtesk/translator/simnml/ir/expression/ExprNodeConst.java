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

package ru.ispras.microtesk.translator.simnml.ir.expression;

public final class ExprNodeConst extends ExprAbstract
{
    private final int radix;

    ExprNodeConst(Object value, int radix)
    {
        super(NodeKind.CONST, ValueInfo.createNative(value));
        this.radix = radix;
    }

    public Object getValue()
    {
        return getValueInfo().getNativeValue();
    }

    public int getRadix()
    {
        return radix;
    }
}
