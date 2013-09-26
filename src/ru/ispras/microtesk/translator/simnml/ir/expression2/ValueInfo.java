/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ValueInfo.java, Sep 26, 2013 5:17:00 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public interface ValueInfo
{
    public enum ValueKind
    {
        MODEL,
        NATIVE
    }

    public ValueKind getValueKind();
    public int getBitSize();

    public Type getModelType();
    public NativeValue getNativeValue();
}

abstract class ValueInfoAbstract implements ValueInfo
{
    private final ValueKind valueKind;

    protected ValueInfoAbstract(ValueKind valueKind)
    {
        this.valueKind = valueKind;
    }

    @Override
    public final ValueKind getValueKind()
    {
        return valueKind;
    }
}
