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

import java.util.List;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class ValueInfoCast
{
    public static List<ValueInfo> cast(ValueKind target, List<ValueInfo> values)
    {
        assert null != values && !values.isEmpty();
        assert values.size() == 1 || values.size() == 2;

        if (values.size() < 2)
            return values;

        final ValueInfo left  = values.get(0); 
        final ValueInfo right = values.get(1);

        if (left.hasEqualType(right))
            return values;

        if (left.getValueKind() != right.getValueKind())
            return castMixed(target, left, right);

        if (ValueKind.MODEL == left.getValueKind())
            return castModel(left, right);

        assert ValueKind.NATIVE == left.getValueKind();
        return castNative(left, right);
    }

    private static List<ValueInfo> castMixed(ValueKind target, ValueInfo left, ValueInfo right)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private static List<ValueInfo> castModel(ValueInfo left, ValueInfo right)
    {
        final Type type = ModelTypeCastRules.getCastType(left.getModelType(), right.getModelType());

        if (null == type)
            return null;

        return null;
    }

    private static List<ValueInfo> castNative(ValueInfo left, ValueInfo right)
    {
        // TODO Auto-generated method stub
        return null;
    }
}

class ModelTypeCastRules
{
    private ModelTypeCastRules() {}

    private static final ETypeID CAST_TYPE_MAP[][]=
    {
        { null,          ETypeID.CARD,  ETypeID.INT,  ETypeID.FLOAT,  ETypeID.BOOL },
        { ETypeID.CARD,  ETypeID.CARD,  ETypeID.INT,  null,           null         },
        { ETypeID.INT,   ETypeID.INT,   ETypeID.INT,  null,           null         },
        { ETypeID.FLOAT, null,          null,         ETypeID.FLOAT,  null         },
        { ETypeID.BOOL,  null,          null,         null,           ETypeID.BOOL },
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
