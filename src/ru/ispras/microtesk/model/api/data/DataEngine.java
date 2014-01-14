/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * DataEngine.java, Nov 14, 2012 2:07:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data;

import java.util.EnumMap;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorAlgorithm;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.data.operations.*;

public final class DataEngine
{   
    private DataEngine() {}
    
    private static final Map<ETypeID, IValueConverter> VALUE_CONVERTERS =
        new EnumMap<ETypeID, IValueConverter>(ETypeID.class);

    private static final Map<EOperatorID, IBinaryOperator> BINARY_OPERATORS =
        new EnumMap<EOperatorID, IBinaryOperator>(EOperatorID.class);
    
    private static final Map<EOperatorID, IUnaryOperator> UNARY_OPERATORS =
        new EnumMap<EOperatorID, IUnaryOperator>(EOperatorID.class);

    static // Initialization section
    {        
        final IntCardConverter converter = new IntCardConverter();

        // The current prototype supports only the INT and CARD data
        // types. We use common converters for them.

        VALUE_CONVERTERS.put(ETypeID.INT,  converter);
        VALUE_CONVERTERS.put(ETypeID.CARD, converter);
        VALUE_CONVERTERS.put(ETypeID.BOOL, converter);

        // Bitwise operators:

        BINARY_OPERATORS.put(EOperatorID.L_SHIFT,  new BitShiftLeft());
        BINARY_OPERATORS.put(EOperatorID.R_SHIFT,  new BitShiftRight());

        BINARY_OPERATORS.put(EOperatorID.L_ROTATE, new BitRotateLeft());
        BINARY_OPERATORS.put(EOperatorID.R_ROTATE, new BitRotateRight());

        BINARY_OPERATORS.put(EOperatorID.BIT_AND,  new BitAnd());
        BINARY_OPERATORS.put(EOperatorID.BIT_OR,   new BitOr());

        BINARY_OPERATORS.put(EOperatorID.BIT_XOR,  new BitXor());
        UNARY_OPERATORS.put(EOperatorID.BIT_NOT,   new BitNot());

        // Arithmetic operators: 
        // NOTE: The current prototype supports only the following basic arithmetic
        // operations: PLUS, MINUS, UNARY_PLUS and UNARY_MINUS.

        BINARY_OPERATORS.put(EOperatorID.PLUS,  new ArithmPlus());
        BINARY_OPERATORS.put(EOperatorID.MINUS, new ArithmMinus());

        UNARY_OPERATORS.put(EOperatorID.UNARY_PLUS,  new ArithmUnaryPlus());
        UNARY_OPERATORS.put(EOperatorID.UNARY_MINUS, new ArithmUnaryMinus());
        
        BINARY_OPERATORS.put(EOperatorID.MUL,  new ArithmMul());
    }

    public static Data valueOf(Type type, long value)
    { 
        checkConversionSupported(type, "long", type.getTypeID().name());

        return VALUE_CONVERTERS.get(type.getTypeID()).fromLong(type, value);
    }

    public static Data valueOf(Type type, int value)
    { 
        checkConversionSupported(type, "int", type.getTypeID().name());

        return VALUE_CONVERTERS.get(type.getTypeID()).fromInt(type, value);
    }

    public static Data valueOf(Type type, String value)
    {
        return valueOf(type, value, ERadix.BIN);
    }

    public static Data valueOf(Type type, String value, ERadix radix)
    { 
        checkConversionSupported(type, "String", type.getTypeID().name());

        return VALUE_CONVERTERS.get(type.getTypeID()).fromString(type, value, radix);
    }
    
    public static int intValue(Data data)
    {
        checkConversionSupported(data.getType(), data.getType().getTypeID().name(), "int");

        return VALUE_CONVERTERS.get(data.getType().getTypeID()).toInt(data);
    }
    
    public static boolean booleanValue(Data data)
    {
        checkConversionSupported(data.getType(), data.getType().getTypeID().name(), "long");

        return 0 != VALUE_CONVERTERS.get(data.getType().getTypeID()).toLong(data);
    }

    public static boolean isIntValueSupported(Type type)
    {
        if (!VALUE_CONVERTERS.containsKey(type.getTypeID()))
            return false;
        
        // If the source value is exceeds the size of an integer value,
        // it will be truncated and we will receive incorrect results.
        // For this reason, this conversion does not make sense.

        if (type.getBitSize() > Integer.SIZE)
            return false;
        
        return true;
    }

    public static long longValue(Data data)
    {
        checkConversionSupported(data.getType(), data.getType().getTypeID().name(), "boolean");

        return VALUE_CONVERTERS.get(data.getType().getTypeID()).toLong(data);
    }
    
    public static boolean isLongValueSupported(Type type)
    {
        if (!VALUE_CONVERTERS.containsKey(type.getTypeID()))
            return false;
        
        // If the source value is exceeds the size of a long value,
        // it will be truncated and we will receive incorrect results.
        // For this reason, this conversion does not make sense.

        if (type.getBitSize() > Long.SIZE)
            return false;
        
        return true;
    }

    public static Data execute(EOperatorID oid, Data arg)
    {
        checkOperationSupported(oid, arg.getType());

        final IUnaryOperator op = UNARY_OPERATORS.get(oid);
        return op.execute(arg);
    }

    public static Data execute(EOperatorID oid, Data left, Data right)
    {
        checkOperationSupported(oid, left.getType(), right.getType());

        final IBinaryOperator op = BINARY_OPERATORS.get(oid);
        return op.execute(left, right);
    }

    public static boolean isSupported(EOperatorID oid, Type arg)
    {
        if (!UNARY_OPERATORS.containsKey(oid))
            return false;

        final IUnaryOperator op = UNARY_OPERATORS.get(oid);
        return op.supports(arg);
    }

    public static boolean isSupported(EOperatorID oid, Type left, Type right)
    {
        if (!BINARY_OPERATORS.containsKey(oid))
            return false;

        final IBinaryOperator op = BINARY_OPERATORS.get(oid);
        return op.supports(left, right);
    }

    public static Data coerce(Type type, Data value)
    {
        // TODO: MAY NEED REVIEW (CODED IN A HURRY)
        //assert false : "NOT IMPLEMENTED";

        final BitVector newRawData = BitVector.createEmpty(type.getBitSize());
        final int copyBitSize = Math.min(value.getType().getBitSize(), type.getBitSize());

        BitVectorAlgorithm.copy(value.getRawData(), 0, newRawData, 0, copyBitSize);
        return new Data(newRawData, type);
    }
    
    private static void checkConversionSupported(Type type, String fromName, String toName)
    {
        if (!VALUE_CONVERTERS.containsKey(type.getTypeID()))
            assert false : String.format(
                "Unsupported coversion: %s values cannot be converted to %s.", fromName, toName);
    }
    
    private static void checkOperationSupported(EOperatorID oid, Type argType)
    {
        if (!isSupported(oid, argType))
            assert false : 
                String.format("The %s operation cannot be performed for an %s(%d) operand.",
                    oid.name(),
                    argType.getTypeID().name(),
                    argType.getBitSize()
                    );
    }
    
    private static void checkOperationSupported(EOperatorID oid, Type left, Type right) 
    {
        if (!isSupported(oid, left, right))
            assert false : 
                String.format("The %s operation cannot be performed for %s(%d) and %s(%d) operands.",
                    oid.name(),
                    left.getTypeID().name(),
                    left.getBitSize(),
                    right.getTypeID().name(),
                    right.getBitSize()
                    );
    }
}
