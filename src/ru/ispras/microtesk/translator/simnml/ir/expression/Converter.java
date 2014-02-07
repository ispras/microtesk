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

package ru.ispras.microtesk.translator.simnml.ir.expression;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operator;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

final class Converter
{
    private Converter() {}

    private static final class OperatorInfo
    {
        private final Enum<?> nativeOp;
        private final Enum<?>  modelOp;

        OperatorInfo(Enum<?> nativeOp, Enum<?> modelOp)
        {
            this.nativeOp = nativeOp;
            this.modelOp  = modelOp;
        }

        Enum<?> getNative() { return nativeOp; }
        Enum<?>  getModel() { return modelOp;  }
    }
    
    private static final Map<Operator, OperatorInfo> operators = createOperators();
    private static Map<Operator, OperatorInfo> createOperators()
    {
        final Map<Operator, OperatorInfo> result =
            new EnumMap<Operator, OperatorInfo>(Operator.class);
      
        result.put(Operator.OR,       new OperatorInfo(StandardOperation.OR,         StandardOperation.BVOR));
        result.put(Operator.AND,      new OperatorInfo(StandardOperation.AND,        StandardOperation.BVAND));

        result.put(Operator.BIT_OR,   new OperatorInfo(null,                         StandardOperation.BVOR));
        result.put(Operator.BIT_XOR,  new OperatorInfo(null,                         StandardOperation.BVXOR));
        result.put(Operator.BIT_AND,  new OperatorInfo(null,                         StandardOperation.BVAND));

        result.put(Operator.EQ,       new OperatorInfo(StandardOperation.EQ,         StandardOperation.EQ));
        result.put(Operator.NOT_EQ,   new OperatorInfo(StandardOperation.NOTEQ,      StandardOperation.NOTEQ));

        result.put(Operator.LEQ,      new OperatorInfo(StandardOperation.LESSEQ,     StandardOperation.BVULE));
        result.put(Operator.GEQ,      new OperatorInfo(StandardOperation.GREATEREQ,  StandardOperation.BVUGE));
        result.put(Operator.LESS,     new OperatorInfo(StandardOperation.LESS,       StandardOperation.BVULT));
        result.put(Operator.GREATER,  new OperatorInfo(StandardOperation.GREATER,    StandardOperation.BVUGT));

        result.put(Operator.L_SHIFT,  new OperatorInfo(null,                         StandardOperation.BVLSHL));
        result.put(Operator.R_SHIFT,  new OperatorInfo(null,                         StandardOperation.BVLSHR));
        result.put(Operator.L_ROTATE, new OperatorInfo(null,                         StandardOperation.BVROL)); 
        result.put(Operator.R_ROTATE, new OperatorInfo(null,                         StandardOperation.BVROR));

        result.put(Operator.PLUS,     new OperatorInfo(StandardOperation.ADD,        StandardOperation.BVADD));
        result.put(Operator.MINUS,    new OperatorInfo(StandardOperation.SUB,        StandardOperation.BVSUB));

        result.put(Operator.MUL,      new OperatorInfo(StandardOperation.MUL,        StandardOperation.BVMUL));
        result.put(Operator.DIV,      new OperatorInfo(StandardOperation.DIV,        null));
        result.put(Operator.MOD,      new OperatorInfo(StandardOperation.MOD,        StandardOperation.BVSMOD));

        result.put(Operator.POW,      new OperatorInfo(StandardOperation.POWER,      null));

        result.put(Operator.UPLUS,    new OperatorInfo(StandardOperation.PLUS,       null));
        result.put(Operator.UMINUS,   new OperatorInfo(StandardOperation.MINUS,      null));
        result.put(Operator.BIT_NOT,  new OperatorInfo(null,                         StandardOperation.BVNOT));
        result.put(Operator.NOT,      new OperatorInfo(StandardOperation.NOT,        null));
        
        result.put(Operator.ITE,      new OperatorInfo(null,                         null));
        
        return Collections.unmodifiableMap(result);
    }

    static Data toFortressData(ValueInfo valueInfo)
    {
        checkValueInfo(valueInfo);

        if (valueInfo.isModel())
            return dataFromModel(valueInfo.getModelType());

        return valueInfo.isConstant() ?
            dataFromNative(valueInfo.getNativeValue()) :
            dataFromNativeType(valueInfo.getNativeType());
    }

    static Enum<?> toFortressOperator(Operator operator, ValueInfo valueInfo)
    {
        checkNotNull(operator);
        checkValueInfo(valueInfo);

        final OperatorInfo oi = operators.get(operator);

        if (null == oi)
            throw new IllegalArgumentException(String.format(ERR_UNSUPPORTED_OP, operator));

        final Enum<?> result = valueInfo.isModel() ? oi.getModel() : oi.getNative();

        if (null == result)
            throw new IllegalArgumentException(String.format(ERR_UNSUPPORTED_FOR, operator, valueInfo.getTypeName()));

        return result;
    }

    private static Data dataFromModel(Type type)
    {
        checkNotNull(type);

        final DataType dataType = getDataTypeForModel(type);
        return dataType.valueUninitialized();
    }

    private static Data dataFromNative(Object value)
    {
        checkNotNull(value);

        final DataType dataType = getDataTypeForNative(value.getClass());
        return new Data(dataType, value);
    }

    private static Data dataFromNativeType(Class<?> type)
    {
        checkNotNull(type);

        final DataType dataType = getDataTypeForNative(type);
        return dataType.valueUninitialized();
    }

    private static DataType getDataTypeForModel(Type type)
    {
        final ETypeID typeId = type.getTypeId();

        if ((typeId != ETypeID.INT) && (typeId != ETypeID.CARD))
            throw new IllegalArgumentException(String.format(ERR_UNSUPPORTED_TYPE, typeId));

        final int bitSize = type.getBitSize();
        return DataType.BIT_VECTOR(bitSize);
    }

    private static DataType getDataTypeForNative(Class<?> type)
    {
        if (Integer.class == type)
            return DataType.INTEGER;

        if (Boolean.class == type)
            return DataType.BOOLEAN;

        throw new IllegalArgumentException(
            String.format(ERR_UNSUPPORTED_TYPE, type.getSimpleName()));
    }

    private static void checkNotNull(Object o)
    {
        if (null == o)
            throw new NullPointerException();
    }

    private static void checkValueInfo(ValueInfo valueInfo)
    {
        checkNotNull(valueInfo);

        if (valueInfo.isModel() || valueInfo.isNative())
            return;

        throw new IllegalArgumentException(
            String.format(ERR_UNKNOWN_KIND, valueInfo.getValueKind()));
    }

    private static final String ERR_UNKNOWN_KIND     = "Unknown kind: %s.";
    private static final String ERR_UNSUPPORTED_TYPE = "Unsupported type: %s.";
    private static final String ERR_UNSUPPORTED_OP   = "Unsupported operator: %s.";
    private static final String ERR_UNSUPPORTED_FOR  = "The %s operator is not supported for %s.";
}
