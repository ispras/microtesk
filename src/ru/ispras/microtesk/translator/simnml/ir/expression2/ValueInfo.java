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

public abstract class ValueInfo
{
    public static ValueInfo createModel(Type type)
    {
        return new ValueInfoModel(type);
    }

    public static ValueInfo createNative(Object value)
    {
        return new ValueInfoNative(value);
    }

    public static ValueInfo createNativeType(Class<?> type)
    {
        return new ValueInfoNative(type);
    }

    private final ValueKind valueKind;

    protected ValueInfo(ValueKind valueKind)
    {
        assert null != valueKind;
        this.valueKind = valueKind;
    }

    public final ValueKind getValueKind()
    {
        return valueKind;
    }

    public final boolean isConstant()
    {
        return (ValueKind.NATIVE == valueKind) && (null != getNativeValue());
    }

    public boolean hasEqualType(ValueInfo value)
    {
        assert null != value;

        if (getValueKind() != value.getValueKind())
            return false;

        if (ValueKind.MODEL == getValueKind() && getModelType().equals(value.getModelType()))
            return true;

        if (ValueKind.NATIVE == getValueKind() && getNativeType() == value.getNativeType())
            return true;

        return false;
    }

    public abstract Type      getModelType();
    public abstract Class<?> getNativeType();
    public abstract Object  getNativeValue();
}

final class ValueInfoModel extends ValueInfo
{
    private final Type type;

    ValueInfoModel(Type type)
    {
        super(ValueKind.MODEL);

        assert null != type;
        this.type = type;
    }

    @Override
    public Type getModelType()
    {
        return type;
    }

    @Override
    public Class<?> getNativeType()
    {
        assert false : "Not applicable";
        return null;
    }

    @Override
    public Object getNativeValue()
    {
        assert false : "Not applicable";
        return null;
    }
}

final class ValueInfoNative extends ValueInfo 
{
    private final Class<?> type;
    private final Object  value;

    ValueInfoNative(Object value)
    {
        super(ValueKind.NATIVE);

        assert null != value;
        assert isSupportedType(value.getClass()); 

        this.type  = value.getClass();
        this.value = value;
    }

    ValueInfoNative(Class<?> type)
    {
        super(ValueKind.NATIVE);

        assert null != type;
        this.type  = type;
        this.value = null;
    }

    private static boolean isSupportedType(Class<?> type)
    {
        return (type == Integer.class) || (type == Long.class) || (type == Boolean.class);
    }

    @Override
    public Type getModelType()
    {
        assert false : "Not applicable";
        return null;
    }

    @Override
    public Class<?> getNativeType()
    {
        return type;
    }

    @Override
    public Object getNativeValue()
    {
        return value;
    }
}
