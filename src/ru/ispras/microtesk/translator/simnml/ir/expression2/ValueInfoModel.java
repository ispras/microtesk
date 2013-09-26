/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ValueInfoModel.java, Sep 26, 2013 5:25:34 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.simnml.ir.expression2.NativeValue.TypeId;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

final class ValueInfoModel extends ValueInfoAbstract
{
    private final Type type;

    ValueInfoModel(Type type)
    {
        super(ValueKind.MODEL);

        assert null != type;
        this.type = type;
    }

    @Override
    public int getBitSize()
    {
        final ValueInfo bitSizeVI = type.getBitSize2().getValueInfo();

        assert ValueKind.NATIVE == bitSizeVI.getValueKind();
        assert TypeId.INTEGER == bitSizeVI.getNativeValue().getTypeId();

        return bitSizeVI.getNativeValue().integerValue();
    }

    @Override
    public Type getModelType()
    {
        return type;
    }

    @Override
    public NativeValue getNativeValue()
    {
        assert false : "Not applicable";
        return null;
    }
}
