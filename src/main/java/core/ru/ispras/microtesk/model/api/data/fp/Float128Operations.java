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

final class Float128Operations implements Operations {
  private static Operations instance = null;

  public static Operations get() {
    if (null == instance) {
      instance = new Float128Operations();
    }
    return instance;
  }

  private Float128Operations() {}

  @Override
  public FloatX add(final FloatX lhs, final FloatX rhs) {
    final Float128 result = JSoftFloat.float128_add(newFloat128(lhs), newFloat128(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX sub(final FloatX lhs, final FloatX rhs) {
    final Float128 result = JSoftFloat.float128_sub(newFloat128(lhs), newFloat128(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX mul(final FloatX lhs, final FloatX rhs) {
    final Float128 result = JSoftFloat.float128_mul(newFloat128(lhs), newFloat128(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX div(final FloatX lhs, final FloatX rhs) {
    final Float128 result = JSoftFloat.float128_div(newFloat128(lhs), newFloat128(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX rem(final FloatX lhs, final FloatX rhs) {
    final Float128 result = JSoftFloat.float128_rem(newFloat128(lhs), newFloat128(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX sqrt(final FloatX arg) {
    final Float128 result = JSoftFloat.float128_sqrt(newFloat128(arg));
    return newFloatX(result);
  }

  @Override
  public int compare(final FloatX first, final FloatX second) {
    final Float128 value1 = newFloat128(first);
    final Float128 value2 = newFloat128(second);

    if (JSoftFloat.float128_eq(value1, value2)){
      return 0;
    }

    return JSoftFloat.float128_lt(value1, value2) ? -1 : 1;
  }

  @Override
  public boolean isNan(final FloatX arg) {
    // TODO
    throw new UnsupportedOperationException(
        "JSoftFloat.float128_is_nan is not supported");
    //return JSoftFloat.float128_is_nan(newFloat128(arg));
  }

  @Override
  public boolean isSignalingNan(final FloatX arg) {
    // TODO
    throw new UnsupportedOperationException(
        "JSoftFloat.float128_is_signaling_nan is not supported");
    //return JSoftFloat.float128_is_signaling_nan(newFloat128(arg));
  }

  @Override
  public FloatX toFloat(final FloatX value, final Precision precision) {
    switch (precision) {
      case FLOAT32: {
        final float float32Value = JSoftFloat.float128_to_float32(newFloat128(value));
        return Float32Operations.newFloatX(float32Value);
      }

      case FLOAT64: {
        final double float64Value = JSoftFloat.float128_to_float64(newFloat128(value));
        return Float64Operations.newFloatX(float64Value);
      }

      case FLOAT80: {
        final FloatX80 float80Value = JSoftFloat.float128_to_floatx80(newFloat128(value));
        return Float80Operations.newFloatX(float80Value);
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
      final int intValue = JSoftFloat.float128_to_int32(newFloat128(value));
      return BitVector.valueOf(intValue, size); 
    }

    if (size == 64) {
      final long longValue = JSoftFloat.float128_to_int64(newFloat128(value));
      return BitVector.valueOf(longValue, size); 
    }

    throw new UnsupportedOperationException(String.format(
        "Conversion from %s to a %d-bit integer is not supported.",
        value.getPrecision().getText(), size)
    );
  }

  @Override
  public FloatX fromInteger(final BitVector value) {
    final Float128 result;
    final int size = value.getBitSize();

    if (size == 32) {
      result = JSoftFloat.int32_to_float128(value.intValue());
    } else if (size == 64) {
      result = JSoftFloat.int64_to_float128(value.longValue());
    } else {
      throw new UnsupportedOperationException(String.format(
          "Conversion from a %d-bit integer to %s is not supported.",
          size, Precision.FLOAT128.getText()));
    }

    return newFloatX(result);
  }

  @Override
  public String toString(final FloatX arg) {
    final Float128 value = newFloat128(arg); 
    return value.toString();
  }

  @Override
  public String toHexString(final FloatX arg) {
    return toString(arg);
  }

  private static Float128 newFloat128(final FloatX value) {
    final BitVector data = value.getData();

    final long low = data.field(0, 63).longValue();
    final long high = data.field(64, 127).longValue();
 
    return new Float128(high, low);
  }

  static FloatX newFloatX(final Float128 value) {
    final BitVector data = BitVector.newEmpty(128);

    data.field(0,   63).assign(BitVector.valueOf(value.low,  64));
    data.field(64, 127).assign(BitVector.valueOf(value.high, 64));

    return new FloatX(data, Precision.FLOAT128);
  }
}
