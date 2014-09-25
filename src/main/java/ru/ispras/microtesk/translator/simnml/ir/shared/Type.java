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
import ru.ispras.microtesk.translator.simnml.generation.PrinterExpr;
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
        { return new Type(TypeId.FLOAT, fracBitSize, expBitSize); }

    public static Type FLOAT(Expr fracBitSize, Expr expBitSize)
        { return new Type(TypeId.FLOAT, fracBitSize, expBitSize); }
    
    public static Type FIX(int beforeBinPtSize, int afterBinPtSize)
        { return new Type(TypeId.FIX, beforeBinPtSize, afterBinPtSize); }

    public static Type FIX(Expr beforeBinPtSize, Expr afterBinPtSize)
        { return new Type(TypeId.FIX, beforeBinPtSize, afterBinPtSize); }

    private static final Class<?> MODEL_API_CLASS =
        ru.ispras.microtesk.model.api.type.Type.class;

    private final TypeId typeId;
    private final String aliasName;
    private final Expr bitSize;
    private final Expr[] fieldSizes;

    private Type(TypeId typeId, String aliasName, Expr bitSize, Expr[] fieldSizes)
    {
        if (null == typeId)
            throw new NullPointerException();

        if (null == bitSize)
            throw new NullPointerException();

        if (null == fieldSizes)
            throw new NullPointerException();

        this.typeId = typeId;
        this.aliasName = aliasName;
        this.bitSize = bitSize;
        this.fieldSizes = fieldSizes;
    }

    private Type(Type type, String aliasName)
    {
        this.typeId = type.typeId;
        this.aliasName = aliasName;
        this.bitSize = type.bitSize;
        this.fieldSizes = type.fieldSizes;
    }

    private Type(TypeId typeId, Expr bitSize)
    {
        this(typeId, null, bitSize, new Expr[] { bitSize } );
    }

    private Type(TypeId typeId, int bitSize)
    {
        this(typeId, Expr.newConstant(bitSize));
    }

    private Type(TypeId typeId, Expr ... fieldSizes)
    {
        this(typeId, null, getTotalSize(fieldSizes), fieldSizes);
    }
    
    private static Expr getTotalSize(Expr ... fieldSizes)
    {
        if (fieldSizes.length == 1)
            return fieldSizes[0];

        int total = 0;
        for (Expr size : fieldSizes)
            total += size.integerValue();

        return Expr.newConstant(total);
    }
    
    private Type(TypeId typeId, int ... fieldSizes)
    {
        this(typeId, null, getTotalSize(fieldSizes), toExprArray(fieldSizes));
    }

    private static Expr getTotalSize(int ... fieldSizes)
    {
        int total = 0;
        for (int size : fieldSizes)
            total += size;

        return Expr.newConstant(total);
    }

    private static Expr[] toExprArray(int ... values)
    {
        final Expr[] exprs = new Expr[values.length];

        for (int index = 0; index < exprs.length; index++)
            exprs[index] = Expr.newConstant(values[index]);

        return exprs;
    }

    public Type alias(String name)
    {
        if (null == name)
            throw new NullPointerException();

        if (name.equals(aliasName))
            return this;

        return new Type(this, name);
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

    public String getAlias()
    {
        return aliasName;
    }

    /**
     * Returns printable representation of the type
     * for the generated Java code. Returns an alias name
     * if it is defined. 
     * 
     * @return Textual representation of the type in Java format.
     */

    public String getJavaText()
    {
        return (null != aliasName) ? aliasName : getTypeName();
    }

    /**
     * Return a printable name of the type to be used in various
     * diagnostic messages.
     *    
     * @return Printable name of the type.
     */

    public String getTypeName()
    {
        final StringBuilder sbFieldSizes = new StringBuilder();
        
        for(Expr fieldSize : fieldSizes)
        {
            if (sbFieldSizes.length() > 0) sbFieldSizes.append(", ");
            sbFieldSizes.append(new PrinterExpr(fieldSize).toString());
        }

        return String.format(
            "%s.%s(%s)",
            MODEL_API_CLASS.getSimpleName(),
            getTypeId(),
            sbFieldSizes
            );
    }

    @Override
    public String toString()
    {
        return String.format(
            "%s, bitSize=%d, alias=%s",
            getTypeName(),
            getBitSize(),
            aliasName != null ? aliasName : "<undefined>"
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
