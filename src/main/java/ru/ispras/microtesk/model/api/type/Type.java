/*
 * Copyright (c) 2012 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Type.java, Nov 9, 2012 5:47:50 PM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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

    public static Type FLOAT(int fracBitSize, int expBitSize)
    {
        // TODO: Need additional fields to store the fraction and the exponent.
        final int bitSize = fracBitSize + expBitSize;
        return new Type(TypeId.FLOAT, bitSize);
    }

    private final TypeId typeId;
    private final int bitSize;

    protected Type(TypeId typeId, int bitSize)
    {
        if (null == typeId)
            throw new NullPointerException();

        this.typeId  = typeId;
        this.bitSize = bitSize;
    }
    
    public Type resize(int newBitSize)
    {
        if (bitSize == newBitSize)
            return this;

        return new Type(typeId, newBitSize);
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
        return String.format("%s.%s(%d)",
            getClass().getSimpleName(), typeId, bitSize);
    }
}
