/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * NativeValue.java, Sep 26, 2013 2:39:19 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

public final class NativeValue
{
    public static enum TypeId
    {
        BOOLEAN,
        INTEGER,
        LONG        
    }

    private final TypeId      typeId;
    private final int        bitSize;
    private final boolean isConstant;
    private final long         value;

    private NativeValue(TypeId typeId, int bitSize, boolean isConstant, long value)
    {
        this.typeId     = typeId;
        this.bitSize    = bitSize;
        this.isConstant = isConstant;
        this.value      = value;
    }

    private NativeValue(TypeId typeId, int bitSize)
    {
        this(typeId, bitSize, false, 0);
    }

    public static NativeValue makeBoolean(boolean value)
    {
        return new NativeValue(TypeId.BOOLEAN, 0, true, value ? 1 : 0);
    }

    public static NativeValue makeBoolean()
    {
        return new NativeValue(TypeId.BOOLEAN, 0);
    }
    
    public static NativeValue makeNumber(int bitSize)
    {
        return new NativeValue(numberTypeForBitSize(bitSize), bitSize);
    }

    public static NativeValue makeNumber(int bitSize, long value)
    {
        return new NativeValue(numberTypeForBitSize(bitSize), bitSize, true, value);
    }
    
    public final TypeId getTypeId()
    {
        return typeId;
    }
    
    public final boolean isConstant()
    {
        return isConstant;
    }

    public final int getBitSize()
    {
        return bitSize;
    }

    public boolean booleanValue()
    {
        checkType(TypeId.BOOLEAN); 
        return 0 != value; 
    }

    public int integerValue()
    {
        checkType(TypeId.INTEGER);
        return (int) value;
    }

    public long longValue()
    {
        checkType(TypeId.LONG); 
        return value;
    }

    private static TypeId numberTypeForBitSize(int bitSize)
    {
        assert 0 < bitSize && bitSize <= Long.SIZE;
        return bitSize <= Integer.SIZE ? TypeId.INTEGER : TypeId.LONG;
    }

    private final void checkType(TypeId expectedtypeId)
    {
        assert isConstant();
        assert this.typeId == expectedtypeId;
    }
}
