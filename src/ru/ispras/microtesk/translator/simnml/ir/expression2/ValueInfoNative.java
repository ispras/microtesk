/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ValueInfoNative.java, Sep 26, 2013 5:56:34 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

final class ValueInfoNative extends ValueInfoAbstract 
{
    private final NativeValue value;

    ValueInfoNative(NativeValue value)
    {
        super(ValueKind.NATIVE);

        assert null != value;
        this.value = value;
    }

    @Override
    public int getBitSize()
    {
        return value.getBitSize();
    }

    @Override
    public Type getModelType()
    {
        assert false : "Not applicable";
        return null;
    }

    @Override
    public NativeValue getNativeValue()
    {
        return value;
    }
}
