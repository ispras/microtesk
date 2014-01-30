/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Converter.java, Jan 30, 2014 4:53:53 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operator;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

final class Converter
{
    private Converter() {}

    static Data toFortressData(ValueInfo valueInfo)
    {
        checkNotNull(valueInfo);

        if (valueInfo.isModel())
        {
            return dataFromModel(valueInfo.getModelType());
        }

        if (valueInfo.isNative())
        {
            return valueInfo.isConstant() ?
                dataFromNative(valueInfo.getNativeValue()) :
                dataFromNativeType(valueInfo.getNativeType());
        }

        throw new IllegalArgumentException(
            String.format("Unknown kind: %s.", valueInfo.getValueKind()));
    }

    private static Data dataFromModel(Type type)
    {
        checkNotNull(type);

        final ETypeID typeId = type.getTypeId();

        if ((typeId != ETypeID.INT) && (typeId != ETypeID.CARD))
            throw new IllegalArgumentException(String.format("Unsupported type: %s.", typeId));

        final int bitSize = type.getBitSize();
        final DataType bitVectorType = DataType.BIT_VECTOR(bitSize);

        return bitVectorType.valueUninitialized();
    }

    private static Data dataFromNative(Object value)
    {
        checkNotNull(value);
        
        // TODO
        return null;
    }

    private static Data dataFromNativeType(Class<?> type)
    {
        // TODO: Not completed
        
        checkNotNull(type);

        final DataType dataType;

        if (Integer.class == type)
        {
            dataType = DataType.INTEGER;
        }
        else if (Boolean.class == type)
        {
            dataType = DataType.BOOLEAN;
        }
        else
        {
            throw new IllegalArgumentException(
               String.format("Unsupported type: %s.", type.getSimpleName()));
        }

        return dataType.valueUninitialized();
    }

    static Enum<?> toFortressOperator(Operator operator, ValueInfo valueInfo)
    {
        checkNotNull(operator);
        checkNotNull(valueInfo);

        return null;
    }

    private static void checkNotNull(Object o)
    {
        if (null == o)
            throw new NullPointerException();
    }
}
