/*
 * Copyright (c) 2012 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Type.java, Oct 22, 2012 1:53:02 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.translator.simnml.ir.shared;

import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class Type
{
    public final static Type BOOLEAN = 
        new Type(TypeId.BOOL, Expr.CONST_ONE);

    public static Type INT(int bitSize)
        { return new Type(TypeId.INT, bitSize); }

    public static Type INT(Expr bitSize)
        { return new Type(TypeId.INT, bitSize); }

    public static Type CARD(int bitSize)
        { return new Type(TypeId.CARD, bitSize); }

    public static Type CARD(Expr bitSize)
        { return new Type(TypeId.CARD, bitSize); }

    public static Type FLOAT(int fracBitSize, int expBitSize)
        { return new Type(TypeId.FLOAT, fracBitSize + expBitSize); }

    public static Type FLOAT(Expr fracBitSize, Expr expBitSize)
        { return FLOAT(fracBitSize.integerValue(), expBitSize.integerValue()); }

    private static final Class<?> MODEL_API_CLASS =
        ru.ispras.microtesk.model.api.type.Type.class;

    private final TypeId typeId;
    private final Expr bitSize;
    private final String refName;

    private Type(TypeId typeId, Expr bitSize)
    {
        this(typeId, bitSize, null);
    }

    private Type(TypeId typeId, int bitSize)
    {
        this(typeId, Expr.newConstant(bitSize));
    }

    private Type(TypeId typeId, Expr bitSize, String refName)
    {
        if (null == typeId)
            throw new NullPointerException();

        if (null == bitSize)
            throw new NullPointerException();

        this.typeId = typeId;
        this.bitSize = bitSize;
        this.refName = refName;
    }

    public Type alias(String name)
    {
        if (null == name)
            throw new NullPointerException();

        if (name.equals(refName))
            return this;

        return new Type(getTypeId(), getBitSizeExpr(), name);
    }

    public Type resize(int newBitSize)
    {
        if (newBitSize == getBitSize())
            return this;

        return new Type(typeId, newBitSize);
    }
    
    public Type resize(Expr newBitSize)
    {
        if (newBitSize.integerValue() == getBitSize())
            return this;

        return new Type(typeId, newBitSize);
    }

    public TypeId getTypeId()
    {
        return typeId;
    }

    public Expr getBitSizeExpr()
    {
        return bitSize;
    }

    public int getBitSize()
    {
        return bitSize.integerValue();
    }

    public String getRefName()
    {
        return refName;
    }

    public String getJavaText()
    {
        if (null != refName)
            return refName;

        return String.format(
            "%s.%s(%d)",
            MODEL_API_CLASS.getSimpleName(),
            getTypeId(),
            getBitSize()
        );
    }

    public String getTypeName()
    {
        return String.format(
            "%s(%s.%s, %d)",
            MODEL_API_CLASS.getSimpleName(),
            TypeId.class.getSimpleName(),
            getTypeId(),
            getBitSize()
            );
    }

    @Override
    public String toString()
    {
        return String.format(
            "Type [typeId='%s', bitSize='%d', refName='%s']",
            typeId,
            getBitSize(),
            refName != null ? refName : "<undefined>"
            );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;

        result = prime * result + typeId.hashCode();
        result = prime * result + getBitSize();

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

        if (typeId != other.typeId)
            return false;

        if (getBitSize() != other.getBitSize())
            return false;

        return true;
    }
}
