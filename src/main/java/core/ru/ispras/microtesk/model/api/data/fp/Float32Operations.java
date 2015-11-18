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

final class Float32Operations implements Operations {
  private static Operations instance = null;

  public static Operations get() {
    if (null == instance) {
      instance = new Float32Operations();
    }
    return instance;
  }

  private Float32Operations() {}

  @Override
  public FloatX add(final FloatX lhs, final FloatX rhs) {
    final float result = JSoftFloat.float32_add(lhs.floatValue(), rhs.floatValue());
    return newFloatX(result);
  }

  @Override
  public FloatX sub(final FloatX lhs, final FloatX rhs) {
    final float result = JSoftFloat.float32_sub(lhs.floatValue(), rhs.floatValue());
    return newFloatX(result);
  }

  @Override
  public FloatX mul(final FloatX lhs, final FloatX rhs) {
    final float result = JSoftFloat.float32_mul(lhs.floatValue(), rhs.floatValue());
    return newFloatX(result);
  }

  @Override
  public FloatX div(final FloatX lhs, final FloatX rhs) {
    final float result = JSoftFloat.float32_div(lhs.floatValue(), rhs.floatValue());
    return newFloatX(result);
  }

  @Override
  public FloatX rem(final FloatX lhs, final FloatX rhs) {
    final float result = JSoftFloat.float32_rem(lhs.floatValue(), rhs.floatValue());
    return newFloatX(result);
  }

  @Override
  public FloatX sqrt(final FloatX arg) {
    final float result = JSoftFloat.float32_sqrt(arg.floatValue());
    return newFloatX(result);
  }

  @Override
  public int compare(final FloatX first, final FloatX second) {
    final float value1 = first.floatValue();
    final float value2 = second.floatValue();

    if (JSoftFloat.float32_eq(value1, value2)){
      return 0;
    }

    return JSoftFloat.float32_lt(value1, value2) ? -1 : 1;
  }

  @Override
  public boolean isNan(final FloatX arg) {
    return JSoftFloat.float32_is_nan(arg.floatValue());
  }

  @Override
  public boolean isSignalingNan(final FloatX arg) {
    return JSoftFloat.float32_is_signaling_nan(arg.floatValue());
  }

  @Override
  public FloatX toFloat(final FloatX value, final Precision precision) {
    switch (precision) {
      case FLOAT64: {
        final double float64Value = JSoftFloat.float32_to_float64(value.floatValue());
        return Float64Operations.newFloatX(float64Value);
      }

      case FLOAT80: {
        final FloatX80 float80Value = JSoftFloat.float32_to_floatx80(value.floatValue());
        return Float80Operations.newFloatX(float80Value);
      }

      case FLOAT128: {
        final Float128 float128Value = JSoftFloat.float32_to_float128(value.floatValue());
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
      final int intValue = JSoftFloat.float32_to_int32(value.floatValue());
      return BitVector.valueOf(intValue, size); 
    }

    if (size == 64) {
      final long longValue = JSoftFloat.float32_to_int64(value.floatValue());
      return BitVector.valueOf(longValue, size); 
    }

    throw new UnsupportedOperationException(String.format(
        "Conversion from %s to a %d-bit integer is not supported.",
        value.getPrecision().getText(), size)
    );
  }

  @Override
  public FloatX fromInteger(final BitVector value) {
    final float result;
    final int size = value.getBitSize();

    if (size == 32) {
      result = JSoftFloat.int32_to_float32(value.intValue());
    } else if (size == 64) {
      result = JSoftFloat.int64_to_float32(value.longValue());
    } else {
      throw new UnsupportedOperationException(String.format(
          "Conversion from a %d-bit integer to %s is not supported.",
          size, Precision.FLOAT32.getText()));
    }

    return newFloatX(result);
  }

  @Override
  public String toString(final FloatX arg) {
    return Float.toString(arg.floatValue());
  }

  @Override
  public String toHexString(final FloatX arg) {
    return Float.toHexString(arg.floatValue());
  }

  static FloatX newFloatX(final float value) {
    return new FloatX(
        BitVector.valueOf(Float.floatToRawIntBits(value), Float.SIZE),
        Precision.FLOAT32
    );
  }
}
