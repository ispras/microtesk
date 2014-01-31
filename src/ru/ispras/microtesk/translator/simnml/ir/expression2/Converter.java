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

    private static final Map<Operator, OperatorInfo> operators =
        new EnumMap<Operator, OperatorInfo>(Operator.class);

    private static void registerOperator(
        Operator operator, Enum<?> nativeOp, Enum<?> modelOp)
    {
        operators.put(operator, new OperatorInfo(nativeOp, modelOp));
    }

    static
    {
        registerOperator(Operator.OR,        StandardOperation.OR,         null);
        registerOperator(Operator.AND,       StandardOperation.AND,        null);

        registerOperator(Operator.BIT_OR,    null,                         StandardOperation.BVOR);
        registerOperator(Operator.BIT_XOR,   null,                         StandardOperation.BVXOR);
        registerOperator(Operator.BIT_AND,   null,                         StandardOperation.BVAND);

        registerOperator(Operator.EQ,        StandardOperation.EQ,         StandardOperation.EQ);
        registerOperator(Operator.NOT_EQ,    StandardOperation.NOTEQ,      StandardOperation.NOTEQ);

        registerOperator(Operator.LEQ,       StandardOperation.LESSEQ,     StandardOperation.BVULE);
        registerOperator(Operator.GEQ,       StandardOperation.GREATEREQ,  StandardOperation.BVUGE);
        registerOperator(Operator.LESS,      StandardOperation.LESS,       StandardOperation.BVULT);
        registerOperator(Operator.GREATER,   StandardOperation.GREATER,    StandardOperation.BVUGT);

        registerOperator(Operator.L_SHIFT,   null,                         StandardOperation.BVLSHL);
        registerOperator(Operator.R_SHIFT,   null,                         StandardOperation.BVLSHR);
        registerOperator(Operator.L_ROTATE,  null,                         null); 
        registerOperator(Operator.R_ROTATE,  null,                         null);

        registerOperator(Operator.PLUS,      StandardOperation.ADD,        StandardOperation.BVADD);
        registerOperator(Operator.MINUS,     StandardOperation.SUB,        StandardOperation.BVSUB);

        registerOperator(Operator.MUL,       StandardOperation.MUL,        StandardOperation.BVMUL);
        registerOperator(Operator.DIV,       StandardOperation.DIV,        null);
        registerOperator(Operator.MOD,       StandardOperation.MOD,        StandardOperation.BVSMOD);

        registerOperator(Operator.POW,       StandardOperation.POWER,      null);

        registerOperator(Operator.UPLUS,     StandardOperation.PLUS,       null);
        registerOperator(Operator.UMINUS,    StandardOperation.MINUS,      null);
        registerOperator(Operator.BIT_NOT,   null,                         StandardOperation.BVNOT);
        registerOperator(Operator.NOT,       StandardOperation.NOT,        null);
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
            throw new IllegalArgumentException(String.format(ERR_UNSUPPORTED_FOR, operator, valueInfo));

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
    private static final String ERR_UNSUPPORTED_FOR  = "The % operator is not supported for %s.";
}
