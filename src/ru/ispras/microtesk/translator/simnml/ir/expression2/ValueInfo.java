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

/**
 * The ValueInfo class holds information about a value associated with an expression node
 * (it may be a value hold by a terminal or a value calculated as a result of some expression). 
 * 
 * @author Andrei Tatarnikov
 */

public abstract class ValueInfo
{
    /**
     * Creates a value information object basing on a MicroTESK Model API type description.
     * 
     * @param type MicroTESK Model API type description.
     * @return Value information.
     */

    public static ValueInfo createModel(Type type)
    {
        return new ValueInfoModel(type);
    }

    /**
     * Creates a value information object basing on a native Java value (Integer, Long or Boolean).
     * 
     * @param value Integer, Long or Boolean value.
     * @return Value information.
     */

    public static ValueInfo createNative(Object value)
    {
        return new ValueInfoNative(value);
    }

    /**
     * Creates a value information object basing on a native Java type (Integer, Long or Boolean).
     * 
     * @param type Native Java type (Integer.class, Long.class or Boolean.class).
     * @return Value information.
     */

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
    
    /**
     * Returns the kind of the referenced value (model API or native Java).
     * 
     * @return  MODEL (MicroTESK Model API) or NATIVE (Java)
     */

    public final ValueKind getValueKind()
    {
        return valueKind;
    }

    /**
     * Returns <code>true</code> if the object holds a statically calculated Java constant value.
     * 
     * @return <code>true</code> for a statically calculated constant value or <code>false</code> is all other cases. 
     */

    public final boolean isConstant()
    {
        return (ValueKind.NATIVE == valueKind) && (null != getNativeValue());
    }

    /**
     * Returns a value information object that holds only type information (does not include a constant value).
     * Returns <code>this</code> for non-constant values.
     *  
     * @return Value information object that holds type information only.
     */

    public final ValueInfo typeInfoOnly()
    {
        if (isConstant())
            return createNativeType(getNativeType());

        return this;
    }

    /**
     * Checks whether both objects refer to values that have the same type.
     * 
     * @param value Value information object to be compared.
     * @return <code>true</code> for equal types, <code>false</code> otherwise.
     */

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

    /**
     * Returns Model API type description if the object refers to a Model API value. 
     * 
     * @return Model API type description for MODEL values or <code>null</code> for NATIVE values.
     */

    public abstract Type getModelType();

    /**
     * Returns Java type information (Class<?>) if the object refers to a native Java value.
     * 
     * @return Java type information (Class<?>) for NATIVE values or <code>null</code> for MODEL values.
     */

    public abstract Class<?> getNativeType();

    /**
     * Returns a value (Integer, Long or Boolean) if the object refers to a statically calculated Java constant value.  
     * 
     * @return Java value (Integer, Long or Boolean) for constant value or <code>null</code> for non-constant values.
     */

    public abstract Object getNativeValue();
}

/**
 * Value information implementation for values based on MicroTESK Model API values.
 * 
 * @author Andrei Tatarnikov
 */

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

/**
 * Value information implementation for values based on native Java values (Integer, Long, Boolean).
 * 
 * @author Andrei Tatarnikov
 */

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
