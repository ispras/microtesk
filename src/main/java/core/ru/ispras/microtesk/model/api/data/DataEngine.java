/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.api.data;

import java.math.BigInteger;
import java.util.EnumMap;
import java.util.Map;

import ru.ispras.fortress.data.types.Radix;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorMath.Operations;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.fp.FloatX;
import ru.ispras.microtesk.model.api.data.operations.ArithmBinary;
import ru.ispras.microtesk.model.api.data.operations.ArithmDiv;
import ru.ispras.microtesk.model.api.data.operations.ArithmMul;
import ru.ispras.microtesk.model.api.data.operations.ArithmUnary;
import ru.ispras.microtesk.model.api.data.operations.BitBinary;
import ru.ispras.microtesk.model.api.data.operations.BitRotateShift;
import ru.ispras.microtesk.model.api.data.operations.BitUnary;
import ru.ispras.microtesk.model.api.data.operations.FloatBinary;
import ru.ispras.microtesk.model.api.data.operations.FloatUnary;
import ru.ispras.microtesk.model.api.data.operations.IntCardConverter;
import ru.ispras.microtesk.model.api.data.operations.LogicBinary;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.type.TypeId;

public final class DataEngine {
  private DataEngine() {}

  private static final Map<TypeId, IValueConverter> VALUE_CONVERTERS =
    new EnumMap<TypeId, IValueConverter>(TypeId.class);

  private static final Map<EOperatorID, IBinaryOperator> BINARY_OPERATORS =
    new EnumMap<EOperatorID, IBinaryOperator>(EOperatorID.class);

  private static final Map<EOperatorID, IUnaryOperator> UNARY_OPERATORS =
    new EnumMap<EOperatorID, IUnaryOperator>(EOperatorID.class);

  private static final Map<EOperatorID, IBinaryOperator> FLOAT_BINARY_OPERATORS =
    new EnumMap<EOperatorID, IBinaryOperator>(EOperatorID.class);

  private static final Map<EOperatorID, IUnaryOperator> FLOAT_UNARY_OPERATORS =
    new EnumMap<EOperatorID, IUnaryOperator>(EOperatorID.class);

  static // Initialization section
  {
    final IntCardConverter converter = new IntCardConverter();

    // The current prototype supports only the INT and CARD data
    // types. We use common converters for them.

    VALUE_CONVERTERS.put(TypeId.INT, converter);
    VALUE_CONVERTERS.put(TypeId.CARD, converter);
    VALUE_CONVERTERS.put(TypeId.BOOL, converter);

    // Bitwise operators:

    BINARY_OPERATORS.put(EOperatorID.BIT_AND, new BitBinary(Operations.AND));
    BINARY_OPERATORS.put(EOperatorID.BIT_OR, new BitBinary(Operations.OR));
    BINARY_OPERATORS.put(EOperatorID.BIT_XOR, new BitBinary(Operations.XOR));
    UNARY_OPERATORS.put(EOperatorID.BIT_NOT, new BitUnary(Operations.NOT));
    BINARY_OPERATORS.put(EOperatorID.L_SHIFT, new BitRotateShift(Operations.SHL));
    BINARY_OPERATORS.put(EOperatorID.R_SHIFT, new BitRotateShift(Operations.LSHR, Operations.ASHR));
    BINARY_OPERATORS.put(EOperatorID.L_ROTATE, new BitRotateShift(Operations.ROTL));
    BINARY_OPERATORS.put(EOperatorID.R_ROTATE, new BitRotateShift(Operations.ROTR));

    // Comparison operators

    BINARY_OPERATORS.put(EOperatorID.GREATER, new LogicBinary(Operations.UGT, Operations.SGT));
    BINARY_OPERATORS.put(EOperatorID.LESS, new LogicBinary(Operations.ULT, Operations.SLT));
    BINARY_OPERATORS.put(EOperatorID.GREATER_EQ, new LogicBinary(Operations.UGE, Operations.SGE));
    BINARY_OPERATORS.put(EOperatorID.LESS_EQ, new LogicBinary(Operations.ULE, Operations.SLE));
    BINARY_OPERATORS.put(EOperatorID.EQ, new LogicBinary(Operations.EQ));
    BINARY_OPERATORS.put(EOperatorID.NOT_EQ, new LogicBinary(Operations.NEQ));

    // Arithmetic operators:
    // NOTE: The current prototype supports only the following basic arithmetic
    // operations: PLUS, MINUS, UNARY_PLUS and UNARY_MINUS.

    BINARY_OPERATORS.put(EOperatorID.PLUS, new ArithmBinary(Operations.ADD));
    BINARY_OPERATORS.put(EOperatorID.MINUS, new ArithmBinary(Operations.SUB));

    UNARY_OPERATORS.put(EOperatorID.UNARY_PLUS, new ArithmUnary(Operations.PLUS));
    UNARY_OPERATORS.put(EOperatorID.UNARY_MINUS, new ArithmUnary(Operations.NEG));

    BINARY_OPERATORS.put(EOperatorID.MUL, new ArithmMul());
    BINARY_OPERATORS.put(EOperatorID.DIV, new ArithmDiv());

    // /////////////////////////////////////////////////////////////////////////////////

    final BitVector BV_TRUE = BitVector.valueOf(true);
    final Data TRUE = new Data(BV_TRUE, Type.BOOL(BV_TRUE.getBitSize()));

    final BitVector BV_FALSE = BitVector.valueOf(false);
    final Data FALSE = new Data(BV_FALSE, Type.BOOL(BV_FALSE.getBitSize()));

    FLOAT_BINARY_OPERATORS.put(EOperatorID.GREATER, new FloatBinary() {
      @Override
      protected Data calculate(FloatX lhs, FloatX rhs) {
        return (lhs.compareTo(rhs) > 0) ? TRUE : FALSE;
      }
    });

    FLOAT_BINARY_OPERATORS.put(EOperatorID.LESS, new FloatBinary() {
      @Override
      protected Data calculate(FloatX lhs, FloatX rhs) {
        return (lhs.compareTo(rhs) < 0) ? TRUE : FALSE;
      }
    });

    FLOAT_BINARY_OPERATORS.put(EOperatorID.GREATER_EQ, new FloatBinary() {
      @Override
      protected Data calculate(FloatX lhs, FloatX rhs) {
        return (lhs.compareTo(rhs) >= 0) ? TRUE : FALSE;
      }
    });

    FLOAT_BINARY_OPERATORS.put(EOperatorID.LESS_EQ, new FloatBinary() {
      @Override
      protected Data calculate(FloatX lhs, FloatX rhs) {
        return (lhs.compareTo(rhs) < 0) ? TRUE : FALSE;
      }
    });

    FLOAT_BINARY_OPERATORS.put(EOperatorID.EQ, new FloatBinary() {
      @Override
      protected Data calculate(FloatX lhs, FloatX rhs) {
        return (lhs.compareTo(rhs) == 0) ? TRUE : FALSE;
      }
    });

    FLOAT_BINARY_OPERATORS.put(EOperatorID.NOT_EQ, new FloatBinary() {
      @Override
      protected Data calculate(FloatX lhs, FloatX rhs) {
        return (lhs.compareTo(rhs) != 0) ? TRUE : FALSE;
      }
    });

    FLOAT_BINARY_OPERATORS.put(EOperatorID.PLUS, new FloatBinary() {
      @Override
      protected Data calculate(FloatX lhs, FloatX rhs) {
        return floatXToData(lhs.add(rhs));
      }
    });

    FLOAT_BINARY_OPERATORS.put(EOperatorID.MINUS, new FloatBinary() {
      @Override
      protected Data calculate(FloatX lhs, FloatX rhs) {
        return floatXToData(lhs.sub(rhs));
      }
    });

    FLOAT_BINARY_OPERATORS.put(EOperatorID.MUL, new FloatBinary() {
      @Override
      protected Data calculate(FloatX lhs, FloatX rhs) {
        return floatXToData(lhs.mul(rhs));
      }
    });

    FLOAT_BINARY_OPERATORS.put(EOperatorID.DIV, new FloatBinary() {
      @Override
      protected Data calculate(FloatX lhs, FloatX rhs) {
        return floatXToData(lhs.div(rhs));
      }
    });

    FLOAT_BINARY_OPERATORS.put(EOperatorID.MOD, new FloatBinary() {
      @Override
      protected Data calculate(FloatX lhs, FloatX rhs) {
        return floatXToData(lhs.mod(rhs));
      }
    });

    FLOAT_UNARY_OPERATORS.put(EOperatorID.UNARY_PLUS, new FloatUnary() {
      @Override
      protected Data calculate(FloatX arg) {
        return floatXToData(arg);
      }
    });

    FLOAT_UNARY_OPERATORS.put(EOperatorID.UNARY_MINUS, new FloatUnary() {
      @Override
      protected Data calculate(FloatX arg) {
        return floatXToData(arg.neg());
      }
    });

    FLOAT_UNARY_OPERATORS.put(EOperatorID.SQRT, new FloatUnary() {
      @Override
      protected Data calculate(FloatX arg) {
        return floatXToData(arg.sqrt());
      }
    });
  }

  public static Data valueOf(Type type, BigInteger value) {
    checkConversionSupported(type, "BigInteger", type.getTypeId().name());
    return VALUE_CONVERTERS.get(type.getTypeId()).fromBigInteger(type, value);
  }

  public static Data valueOf(Type type, long value) {
    checkConversionSupported(type, "long", type.getTypeId().name());
    return VALUE_CONVERTERS.get(type.getTypeId()).fromLong(type, value);
  }

  public static Data valueOf(Type type, int value) {
    checkConversionSupported(type, "int", type.getTypeId().name());
    return VALUE_CONVERTERS.get(type.getTypeId()).fromInt(type, value);
  }

  public static Data valueOf(Type type, String value) {
    return valueOf(type, value, Radix.BIN);
  }

  public static Data valueOf(Type type, String value, Radix radix) {
    checkConversionSupported(type, "String", type.getTypeId().name());
    return VALUE_CONVERTERS.get(type.getTypeId()).fromString(type, value, radix);
  }

  public static int intValue(Data data) {
    checkConversionSupported(data.getType(), data.getType().getTypeId().name(), "int");
    return VALUE_CONVERTERS.get(data.getType().getTypeId()).toInt(data);
  }
  
  public static BigInteger bigIntegerValue(Data data) {
    return data.getRawData().bigIntegerValue(false);
  }

  public static boolean booleanValue(Data data) {
    checkConversionSupported(data.getType(), data.getType().getTypeId().name(), "long");
    return 0 != VALUE_CONVERTERS.get(data.getType().getTypeId()).toLong(data);
  }

  public static boolean isIntValueSupported(Type type) {
    if (!VALUE_CONVERTERS.containsKey(type.getTypeId())) {
      return false;
    }

    // If the source value is exceeds the size of an integer value,
    // it will be truncated and we will receive incorrect results.
    // For this reason, this conversion does not make sense.

    if (type.getBitSize() > Integer.SIZE) {
      return false;
    }

    return true;
  }

  public static long longValue(Data data) {
    checkConversionSupported(data.getType(), data.getType().getTypeId().name(), "boolean");
    return VALUE_CONVERTERS.get(data.getType().getTypeId()).toLong(data);
  }

  public static boolean isLongValueSupported(Type type) {
    if (!VALUE_CONVERTERS.containsKey(type.getTypeId())) {
      return false;
    }

    // If the source value is exceeds the size of a long value,
    // it will be truncated and we will receive incorrect results.
    // For this reason, this conversion does not make sense.

    if (type.getBitSize() > Long.SIZE) {
      return false;
    }

    return true;
  }

  public static Data execute(EOperatorID oid, Data arg) {
    checkOperationSupported(oid, arg.getType());

    final IUnaryOperator op = getUnaryOperator(oid, arg.getType());
    return op.execute(arg);
  }

  public static Data execute(EOperatorID oid, Data left, Data right) {
    checkOperationSupported(oid, left.getType(), right.getType());

    final IBinaryOperator op = getBinaryOperator(oid, left.getType(), right.getType());
    return op.execute(left, right);
  }

  public static boolean isSupported(EOperatorID oid, Type arg) {
    final IUnaryOperator op = getUnaryOperator(oid, arg);

    if (null == op) {
      return false;
    }

    return op.supports(arg);
  }

  public static boolean isSupported(EOperatorID oid, Type left, Type right) {
    final IBinaryOperator op = getBinaryOperator(oid, left, right);

    if (null == op) {
      return false;
    }

    return op.supports(left, right);
  }

  public static Data sign_extend(final Type type, final Data value) {
    if (type.equals(value.getType())) {
      return value;
    }

    InvariantChecks.checkTrue(type.getBitSize() >= value.getType().getBitSize());

    final BitVector newRawData =
        value.getRawData().resize(type.getBitSize(), true);

    return new Data(newRawData, type);
  }

  public static Data zero_extend(final Type type, final Data value) {
    if (type.equals(value.getType())) {
      return value;
    }

    InvariantChecks.checkTrue(type.getBitSize() >= value.getType().getBitSize());

    final BitVector newRawData =
        value.getRawData().resize(type.getBitSize(), false);

    return new Data(newRawData, type);
  }

  public static Data coerce(final Type type, final Data value) {
    if (type.equals(value.getType())) {
      return value;
    }

    // Restriction: only integer types (INT, CARD, BOOL) are supported.
    InvariantChecks.checkTrue(
        type.getTypeId().isInteger(),
        String.format("Coercion to %s is not supported.", type));

    InvariantChecks.checkTrue(
        value.getType().getTypeId().isInteger(),
        String.format("Coercion from %s is not supported.", value.getType()));

    // Sign extension applies only to INT.
    final boolean signExt =
        value.getType().getTypeId() == TypeId.INT;

    final BitVector newRawData =
        value.getRawData().resize(type.getBitSize(), signExt);

    return new Data(newRawData, type);
  }

  public static Data cast(final Type type, final Data value) {
    if (type.equals(value.getType())) {
      return value;
    }

    InvariantChecks.checkTrue(type.getBitSize() == value.getType().getBitSize());
    return new Data(value.getRawData(), type);
  }

  public static Data int_to_float(final Type type, final Data value) {
    InvariantChecks.checkTrue(type.getTypeId() == TypeId.FLOAT);
    InvariantChecks.checkTrue(value.getType().getTypeId().isInteger());

    final BitVector source = value.getRawData();
    final FloatX target = FloatX.fromInteger(
        type.getFieldSize(0), type.getFieldSize(1), source);

    return new Data(target.getData(), type);
  }

  public static Data float_to_int(final Type type, final Data value) {
    InvariantChecks.checkTrue(type.getTypeId().isInteger());
    InvariantChecks.checkTrue(value.getType().getTypeId() == TypeId.FLOAT);

    final FloatX source = FloatBinary.dataToFloatX(value);
    final BitVector target = source.toInteger(type.getBitSize());

    return new Data(target, type);
  }

  public static Data float_to_float(final Type type, final Data value) {
    InvariantChecks.checkTrue(type.getTypeId() == TypeId.FLOAT);
    InvariantChecks.checkTrue(value.getType().getTypeId() == TypeId.FLOAT);

    final FloatX source = FloatBinary.dataToFloatX(value);
    final FloatX target = source.toFloat(
        type.getFieldSize(0), type.getFieldSize(1));

    return new Data(target.getData(), type);
  }

  private static IUnaryOperator getUnaryOperator(EOperatorID oid, Type type) {
    if (type.getTypeId() == TypeId.FLOAT) {
      return FLOAT_UNARY_OPERATORS.get(oid);
    }

    return UNARY_OPERATORS.get(oid);
  }

  private static IBinaryOperator getBinaryOperator(EOperatorID oid, Type type1, Type type2) {
    if (type1.getTypeId() == TypeId.FLOAT && type2.getTypeId() == TypeId.FLOAT) {
      return FLOAT_BINARY_OPERATORS.get(oid);
    }

    return BINARY_OPERATORS.get(oid);
  }

  private static void checkConversionSupported(Type type, String fromName, String toName) {
    if (!VALUE_CONVERTERS.containsKey(type.getTypeId())) {
      throw new IllegalArgumentException(String.format(
        "Unsupported coversion: %s values cannot be converted to %s.", fromName, toName));
    }
  }

  private static void checkOperationSupported(EOperatorID oid, Type argType) {
    if (!isSupported(oid, argType)) {
      throw new IllegalArgumentException(String.format(
        "The %s operation cannot be performed for an %s operand.", oid.name(), argType));
    }
  }

  private static void checkOperationSupported(EOperatorID oid, Type left, Type right) {
    if (!isSupported(oid, left, right)) {
      throw new IllegalArgumentException(String.format(
        "The %s operation cannot be performed for %s and %s operands.", oid.name(), left, right));
    }
  }

  /**
   * Checks whether the significant bits are lost when the specified integer is converted to
   * the specified Model API type. This happens when the type is shorter than the value
   * and the truncated part goes beyond sign extension bits.
   * 
   * @param type Conversion target type.
   * @param value Value to be converted.
   * @return {@code true} if significant bits will be lost during the conversion
   * or {@code false} otherwise.
   */

  public static boolean isLossOfSignificantBits(
      final Type type, final BigInteger value) {

    final int valueBitSize = value.bitLength() + 1; // Minimal two's complement + sign bit
    if (type.getBitSize() >= valueBitSize) {
      return false;
    }

    final BitVector whole = BitVector.valueOf(value, valueBitSize);
    final BitVector truncated = BitVector.newMapping(
        whole, type.getBitSize(), whole.getBitSize() - type.getBitSize());

    final long truncatedValue = truncated.longValue();
    if (truncatedValue == 0) {
      return false;
    }

    final boolean isNegative = whole.getBit(type.getBitSize() - 1);
    final long allOnesPattern = (-1L >>> valueBitSize - truncated.getBitSize());

    if (isNegative && (truncatedValue == allOnesPattern)) {
      return false;
    }

    return true;
  }
}
