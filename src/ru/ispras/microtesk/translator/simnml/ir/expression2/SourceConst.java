/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SourceConst.java, Jan 27, 2014 5:42:33 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

public final class SourceConst
{
    private final Object value;
    private final int    radix;

    SourceConst(Object value, int radix)
    {
        if (null == value)
            throw new NullPointerException();

        this.value = value;
        this.radix = radix;
    }

    public Object getValue()
    {
        return value;
    }

    public int getRadix()
    {
        return radix;
    }
}
