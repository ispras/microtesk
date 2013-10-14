/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ValueInfoCast.java, Oct 11, 2013 1:59:03 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class ValueInfoCast
{
    public static List<ValueInfo> cast(ValueKind target, List<ValueInfo> values)
    {
        assert values.size() == 1 || values.size() == 2;

        if (values.size() < 2)
            return values;

        final ValueInfo left  = values.get(0); 
        final ValueInfo right = values.get(1);

        if (left.hasEqualType(right))
            return values;

        if (left.getValueKind() != right.getValueKind())
            return castMixed(target, values);

        if (ValueKind.MODEL == left.getValueKind())
            return castModel(values);
        else
            return castNative(values);
    }

    private static List<ValueInfo> castMixed(ValueKind target, List<ValueInfo> values)
    {
        ValueInfo castValueInfo = null;
        for (ValueInfo vi : values)
        {
            if (vi.getValueKind() == target)
            {
                if (vi.isConstant())
                {    
                    castValueInfo = ValueKind.MODEL == vi.getValueKind() ?
                        ValueInfo.createModel(vi.getModelType()) :
                        ValueInfo.createNativeType(vi.getNativeType());
                }
                else
                {
                    castValueInfo = vi;
                }

                break;
            }
        }

        if (null == castValueInfo)
        {
            assert false;
            return null;
        }

        final List<ValueInfo> result = new ArrayList<ValueInfo>(values.size());

        for (ValueInfo vi : values)
        {
            if (vi.hasEqualType(castValueInfo))
                result.add(vi);
            else
                result.add(castValueInfo);
        }

        return result;
    }

    private static List<ValueInfo> castModel(List<ValueInfo> values)
    {
        final Type castType =
            ModelTypeCastRules.getCastType(values.get(0).getModelType(), values.get(1).getModelType());

        if (null == castType)
            return null;

        final List<ValueInfo> result = new ArrayList<ValueInfo>(values.size());

        for (ValueInfo vi : values)
        {
            if (vi.getModelType().equals(castType))
                result.add(vi);
            else
                result.add(ValueInfo.createModel(castType));
        }

        return result;
    }

    private static List<ValueInfo> castNative(List<ValueInfo> values)
    {
        final Class<?> castType = 
            NativeTypeCastRules.getCastType(values.get(0).getNativeType(), values.get(1).getNativeType());

        if (null == castType)
            return null;

        final List<ValueInfo> result = new ArrayList<ValueInfo>(values.size());

        for (ValueInfo vi : values)
        {
            if (vi.getNativeType() == castType)
            {
                result.add(vi);
            }
            else if (vi.isConstant())
            {
                final Object value = NativeTypeCastRules.castTo(castType, vi.getNativeValue());
                result.add(ValueInfo.createNative(value));
            }
            else
            {
                result.add(ValueInfo.createNativeType(castType));
            }
        }

        return result;
    }
}

class ModelTypeCastRules
{
    private ModelTypeCastRules() {}

    private static final ETypeID CAST_TYPE_MAP[][]=
    {
        { null,          ETypeID.CARD,  ETypeID.INT,  ETypeID.BOOL },
        { ETypeID.CARD,  ETypeID.CARD,  ETypeID.INT,  null         },
        { ETypeID.INT,   ETypeID.INT,   ETypeID.INT,  null         },
        { ETypeID.BOOL,  null,          null,         ETypeID.BOOL },
    };

    public static ETypeID getCastTypeId(ETypeID left, ETypeID right)
    {
        int col = 0; // left -> col
        for (int columnIndex = 1; columnIndex < CAST_TYPE_MAP[0].length; ++columnIndex)
        {
            if (CAST_TYPE_MAP[0][columnIndex] == left)
            {
                col = columnIndex;
                break;
            }
        }

        if (0 == col) // left is not found
            return null;

        int row = 0; // right -> row
        for (int rowIndex = 1; rowIndex < CAST_TYPE_MAP.length; ++rowIndex)
        {
            if (CAST_TYPE_MAP[rowIndex][0] == right)
            {
                row = rowIndex;
                break;
            }
        }

        if (0 == row) // right is not found
            return null;

        return CAST_TYPE_MAP[col][row];
    }
    
    public static Type getCastType(Type left, Type right)
    {
        final ETypeID typeId = 
            getCastTypeId(left.getTypeId(), right.getTypeId());

        if (null == typeId)
            return null;

        final int bitSize;
        final Expr bitSizeExpr;

        if (left.getBitSize2() >= right.getBitSize2())
        {
            bitSize = left.getBitSize2();
            bitSizeExpr = left.getBitSizeExpr();
        }
        else
        {
            bitSize = right.getBitSize2();
            bitSizeExpr = right.getBitSizeExpr();
        }

        if (typeId == left.getTypeId() && bitSize == left.getBitSize2())
            return left;

        if (typeId == right.getTypeId() && bitSize == right.getBitSize2())
            return right;

        return new Type(typeId, bitSizeExpr);
    }
}

class NativeTypeCastRules
{
    private NativeTypeCastRules() {}

    private static abstract class Cast
    {
        final Class<?> from;
        final Class<?> to;

        Cast(Class<?> from, Class<?> to)
        {
            assert null != from;
            assert null != to;

            this.from = from;
            this.to = to;
        }

        final Object cast(Object value)
        {
            assert (value.getClass() == from) && (value.getClass() != to);
            return doCast(value);
        }

        abstract Object doCast(Object value);
    }

    private static final Cast INT_TO_LONG = new Cast(Integer.class, Long.class)
    {
        @Override
        public Object doCast(Object value) 
            { return ((Number) value).longValue(); }
    }; 

    private static final Class<?> CAST_TYPE_MAP[][]=
    {
        { null,          int.class,    long.class,    boolean.class },
        { int.class,     int.class,    long.class,    null          },
        { long.class,    long.class,   long.class,    null          },
        { boolean.class, null,         null,          boolean.class }
    };

    public static Class<?> getCastType(Class<?> left, Class<?> right)
    {
        int col = 0; // left -> col
        for (int columnIndex = 1; columnIndex < CAST_TYPE_MAP[0].length; ++columnIndex)
        {
            if (CAST_TYPE_MAP[0][columnIndex] == left)
            {
                col = columnIndex;
                break;
            }
        }

        if (0 == col) // left is not found
            return null;

        int row = 0; // right -> row
        for (int rowIndex = 1; rowIndex < CAST_TYPE_MAP.length; ++rowIndex)
        {
            if (CAST_TYPE_MAP[rowIndex][0] == right)
            {
                row = rowIndex;
                break;
            }
        }

        if (0 == row) // right is not found
            return null;

        return CAST_TYPE_MAP[col][row];
    }
    
    public static Object castTo(Class<?> target, Object value)
    {
        if (value.getClass() == target)
            return value;

        if (INT_TO_LONG.to == target && INT_TO_LONG.from == value.getClass())
            return INT_TO_LONG.cast(value);

        assert false : String.format("Unsupported cast: %s to %s", value.getClass(), target);
        return null;
    }
}
