/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Type.java, Nov 9, 2012 5:47:50 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.type;

/**
 * The Type class stores information on a type defined in the design
 * specification. This includes type identifier and size of the data in bits.
 *
 * <pre>
 * For example, the following definition in Sim-nML:
 * 
 * type index = card(6)
 * 
 * corresponds to:
 * 
 * Type index = Type.CARD(6);</pre>
 * 
 * @author Andrei Tatarnikov
 */

public class Type
{
    public static Type INT(int bitSize)
    {
        return new Type(TypeId.INT, bitSize);
    }

    public static Type CARD(int bitSize)
    {
        return new Type(TypeId.CARD, bitSize);
    }

    public static Type BOOL(int bitSize)
    {
        return new Type(TypeId.BOOL, bitSize);
    }

    private final TypeId typeId;
    private final int bitSize;

    public Type(TypeId typeId, int bitSize)
    {
        if (null == typeId)
            throw new NullPointerException();

        this.typeId  = typeId;
        this.bitSize = bitSize;
    }

    public final TypeId getTypeId()
    {
        return typeId;
    }

    public final int getBitSize()
    {
        return bitSize;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;

        result = prime * result + bitSize;
        result = prime * result + typeId.hashCode();

        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;

        if (getClass() != obj.getClass())
            return false;

        final Type other = (Type) obj;
        return (typeId == other.typeId) &&
               (bitSize == other.bitSize);
    }

    @Override
    public String toString()
    {
        return String.format(
            "Type.%s(%d)", typeId, bitSize);
    }
}
