/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.data.fp;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.softfloat.Float128;
import ru.ispras.softfloat.FloatX80;
import ru.ispras.softfloat.JSoftFloat;

final class Float80Operations implements Operations {
  private static Operations instance = null;

  public static Operations get() {
    if (null == instance) {
      instance = new Float80Operations();
    }
    return instance;
  }

  private Float80Operations() {}

  @Override
  public FloatX add(final FloatX lhs, final FloatX rhs) {
    final FloatX80 result = JSoftFloat.floatx80_add(newFloatX80(lhs), newFloatX80(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX sub(final FloatX lhs, final FloatX rhs) {
    final FloatX80 result = JSoftFloat.floatx80_sub(newFloatX80(lhs), newFloatX80(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX mul(final FloatX lhs, final FloatX rhs) {
    final FloatX80 result = JSoftFloat.floatx80_mul(newFloatX80(lhs), newFloatX80(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX div(final FloatX lhs, final FloatX rhs) {
    final FloatX80 result = JSoftFloat.floatx80_div(newFloatX80(lhs), newFloatX80(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX rem(final FloatX lhs, final FloatX rhs) {
    final FloatX80 result = JSoftFloat.floatx80_rem(newFloatX80(lhs), newFloatX80(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX sqrt(final FloatX arg) {
    final FloatX80 result = JSoftFloat.floatx80_sqrt(newFloatX80(arg));
    return newFloatX(result);
  }

  @Override
  public int compare(final FloatX first, final FloatX second) {
    final FloatX80 value1 = newFloatX80(first);
    final FloatX80 value2 = newFloatX80(second);

    if (JSoftFloat.floatx80_eq(value1, value2)){
      return 0;
    }

    return JSoftFloat.floatx80_lt(value1, value2) ? -1 : 1;
  }

  @Override
  public boolean isNan(final FloatX arg) {
    return JSoftFloat.floatx80_is_nan(newFloatX80(arg));
  }

  @Override
  public boolean isSignalingNan(final FloatX arg) {
    return JSoftFloat.floatx80_is_signaling_nan(newFloatX80(arg));
  }

  @Override
  public FloatX toFloat(final FloatX value, final Precision precision) {
    switch (precision) {
      case FLOAT32: {
        final float float32Value = JSoftFloat.floatx80_to_float32(newFloatX80(value));
        return Float32Operations.newFloatX(float32Value);
      }

      case FLOAT64: {
        final double float64Value = JSoftFloat.floatx80_to_float64(newFloatX80(value));
        return Float64Operations.newFloatX(float64Value);
      }

      case FLOAT128: {
        final Float128 float128Value = JSoftFloat.floatx80_to_float128(newFloatX80(value));
        return Float128Operations.newFloatX(float128Value);
      }

      default:
        throw new UnsupportedOperationException(String.format(
            "Conversion from %s to %s is not supported.",
            value.getPrecision().getText(), precision.getText())
        );
    }
  }

  @Override
  public BitVector toInteger(final FloatX value, final int size) {
    if (size == 32) {
      final int intValue = JSoftFloat.floatx80_to_int32(newFloatX80(value));
      return BitVector.valueOf(intValue, size); 
    }

    if (size == 64) {
      final long longValue = JSoftFloat.floatx80_to_int64(newFloatX80(value));
      return BitVector.valueOf(longValue, size); 
    }

    throw new UnsupportedOperationException(String.format(
        "Conversion from %s to a %d-bit integer is not supported.",
        value.getPrecision().getText(), size)
    );
  }

  @Override
  public FloatX fromInteger(final BitVector value) {
    final FloatX80 result;
    final int size = value.getBitSize();

    if (size == 32) {
      result = JSoftFloat.int32_to_floatx80(value.intValue());
    } else if (size == 64) {
      result = JSoftFloat.int64_to_floatx80(value.longValue());
    } else {
      throw new UnsupportedOperationException(String.format(
          "Conversion from a %d-bit integer to %s is not supported.",
          size, Precision.FLOAT80.getText()));
    }

    return newFloatX(result);
  }

  @Override
  public String toString(final FloatX arg) {
    final FloatX80 value = newFloatX80(arg);
    return value.toString();
  }

  @Override
  public String toHexString(final FloatX arg) {
    return toString(arg);
  }

  private static FloatX80 newFloatX80(final FloatX value) {
    final BitVector data = value.getData();

    final long low = data.field(0, 63).longValue();
    final short high = (short)(data.field(64, 79).intValue() & 0x0000FFFF);
 
    return new FloatX80(high, low);
  }

  static FloatX newFloatX(final FloatX80 value) {
    final BitVector data = BitVector.newEmpty(80);

    data.field(0,  63).assign(BitVector.valueOf(value.low,  64));
    data.field(64, 79).assign(BitVector.valueOf(value.high, 16));

    return new FloatX(data, Precision.FLOAT128);
  }
}
