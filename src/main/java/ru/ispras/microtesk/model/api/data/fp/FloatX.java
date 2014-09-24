/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * FloatX.java, Sep 23, 2014 5:35:58 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.data.fp;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import sun.misc.FloatConsts;
import sun.misc.DoubleConsts;

public final class FloatX 
    extends Number implements Comparable<FloatX>
{
    private static final long serialVersionUID = 2006185347947148830L;

    private final BitVector data;
    private final int exponentSize;

    public FloatX(BitVector data, int exponentSize, int fractionSize)
    {
        if (null == data)
            throw new NullPointerException();

        if (exponentSize <= 0)
            throw new IllegalArgumentException();

        if (fractionSize <= 0)
            throw new IllegalArgumentException();

        if (data.getBitSize() != exponentSize + fractionSize)
            throw new IllegalArgumentException();

        this.data = BitVector.unmodifiable(data);
        this.exponentSize = exponentSize;
    }

    public FloatX(int exponentSize, int fractionSize)
    {
        this(
           BitVector.newEmpty(exponentSize + fractionSize),
           exponentSize,
           fractionSize
           );
    }

    public FloatX(float floatData)
    {
        this(
           BitVector.valueOf(Float.floatToIntBits(floatData), Float.SIZE),
           Float.SIZE - FloatConsts.SIGNIFICAND_WIDTH + 1,
           FloatConsts.SIGNIFICAND_WIDTH - 1
        );
    }

    public FloatX(double doubleData)
    {
        this(
           BitVector.valueOf(Double.doubleToLongBits(doubleData), Double.SIZE),
           Double.SIZE - DoubleConsts.SIGNIFICAND_WIDTH + 1,
           DoubleConsts.SIGNIFICAND_WIDTH - 1
        );
    }

    public BitVector getData()
    {
        return data;
    }

    public int getSize()
    {
        return data.getBitSize();
    }

    public int getExponentSize()
    { 
        return exponentSize;
    }

    public int getFractionSize()
    {
        return getSize() - exponentSize;
    }

    public boolean isSingle()
    {
        return (getSize() == Float.SIZE) && 
               (getFractionSize() == FloatConsts.SIGNIFICAND_WIDTH - 1);
    }

    public boolean isDouble()
    {
        return (getSize() == Double.SIZE) && 
               (getFractionSize() == DoubleConsts.SIGNIFICAND_WIDTH - 1);
    }

    @Override
    public int compareTo(FloatX o)
    {
        if (null == o)
            throw new NullPointerException();

        if (equals(o))
            return 0;

        if (isSingle() && o.isSingle())
            return Float.compare(floatValue(), o.floatValue());

        if (isDouble() && o.isDouble())
            return Double.compare(doubleValue(), o.doubleValue());

        return data.compareTo(o.data);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;

        result = prime * result + data.hashCode();
        result = prime * result + exponentSize;

        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;

        if (getClass() != obj.getClass())
            return false;

        final FloatX other = (FloatX) obj;

        if (!data.equals(other.data))
            return false;

        if (exponentSize != other.exponentSize)
            return false;

        return true;
    }

    @Override
    public float floatValue()
    {
        if (!isSingle())
            return Float.intBitsToFloat(data.intValue());

        throw new IllegalStateException(String.format(
            "The %s type is not a IEEE 754 single.", getTypeName()));
    }

    @Override
    public double doubleValue()
    {
        if (isDouble())
            return Double.longBitsToDouble(data.longValue());

        throw new IllegalStateException(String.format(
            "The %s type is not a IEEE 754 double.", getTypeName()));
    }

    @Override
    public int intValue()
    {
        return data.intValue();
    }

    @Override
    public long longValue()
    {
        return data.longValue();
    }

    public String getTypeName()
    {
        return String.format(
            "float(%d, %d)", getFractionSize(), getExponentSize());
    }

    @Override
    public String toString()
    {
        if (isSingle())
            return Float.toString(floatValue());

        if (isDouble())
            return Double.toString(doubleValue());

        return data.toString();
    }

    public String toHexString()
    {
        if (isSingle())
            return Float.toHexString(floatValue());

        if (isDouble())
            return Double.toHexString(doubleValue());

        return data.toHexString();
    }

    public FloatX neg()
    {
        if (isSingle())
            return new FloatX(-floatValue());

        if (isDouble())
            return new FloatX(-doubleValue());

        throw new UnsupportedOperationException(String.format(
            "Not supported for argument types: %s.", getTypeName()));
    }

    public FloatX add(FloatX arg)
    {
        if (null == arg)
            throw new NullPointerException();

        if (isSingle() && arg.isSingle())
            return new FloatX(floatValue() + arg.floatValue());

        if (isDouble() && arg.isDouble())
            return new FloatX(doubleValue() + arg.doubleValue());

        return raiseNotSupported(getTypeName(), arg.getTypeName());
    }

    public FloatX sub(FloatX arg)
    {
        if (null == arg)
            throw new NullPointerException();

        if (isSingle() && arg.isSingle())
            return new FloatX(floatValue() - arg.floatValue());

        if (isDouble() && arg.isDouble())
            return new FloatX(doubleValue() - arg.doubleValue());

        return raiseNotSupported(getTypeName(), arg.getTypeName());
    }

    public FloatX mul(FloatX arg)
    {
        if (null == arg)
            throw new NullPointerException();

        if (isSingle() && arg.isSingle())
            return new FloatX(floatValue() * arg.floatValue());

        if (isDouble() && arg.isDouble())
            return new FloatX(doubleValue() * arg.doubleValue());

        return raiseNotSupported(getTypeName(), arg.getTypeName());
    }

    private static FloatX raiseNotSupported(String argType1, String argType2) 
    {
        throw new UnsupportedOperationException(String.format(
            "Not supported for argument types: %s and %s.", argType1, argType2));
    }
}
