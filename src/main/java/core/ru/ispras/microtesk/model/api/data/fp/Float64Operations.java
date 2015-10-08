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

final class Float64Operations implements Operations {
  private static Operations instance = null;

  public static Operations get() {
    if (null == instance) {
      instance = new Float64Operations();
    }
    return instance;
  }

  private Float64Operations() {}

  @Override
  public FloatX add(final FloatX lhs, final FloatX rhs) {
    final double result = JSoftFloat.float64_add(lhs.doubleValue(), rhs.doubleValue());
    return newFloatX(result);
  }

  @Override
  public FloatX sub(final FloatX lhs, final FloatX rhs) {
    final double result = JSoftFloat.float64_sub(lhs.doubleValue(), rhs.doubleValue());
    return newFloatX(result);
  }

  @Override
  public FloatX mul(final FloatX lhs, final FloatX rhs) {
    final double result = JSoftFloat.float64_mul(lhs.doubleValue(), rhs.doubleValue());
    return newFloatX(result);
  }

  @Override
  public FloatX div(final FloatX lhs, final FloatX rhs) {
    final double result = JSoftFloat.float64_div(lhs.doubleValue(), rhs.doubleValue());
    return newFloatX(result);
  }

  @Override
  public FloatX rem(final FloatX lhs, final FloatX rhs) {
    final double result = JSoftFloat.float64_rem(lhs.doubleValue(), rhs.doubleValue());
    return newFloatX(result);
  }

  @Override
  public FloatX sqrt(final FloatX arg) {
    final double result = JSoftFloat.float64_sqrt(arg.doubleValue());
    return newFloatX(result);
  }

  @Override
  public int compare(final FloatX first, final FloatX second) {
    final double value1 = first.doubleValue();
    final double value2 = second.doubleValue();

    if (JSoftFloat.float64_eq(value1, value2)){
      return 0;
    }

    return JSoftFloat.float64_lt(value1, value2) ? -1 : 1;
  }

  @Override
  public FloatX toFloat(final FloatX value, final Precision precision) {
    switch (precision) {
      case FLOAT32: {
        final float float32Value = JSoftFloat.float64_to_float32(value.doubleValue());
        return Float32Operations.newFloatX(float32Value);
      }

      case FLOAT80: {
        final FloatX80 float80Value = JSoftFloat.float64_to_floatx80(value.doubleValue());
        return Float80Operations.newFloatX(float80Value);
      }

      case FLOAT128: {
        final Float128 float128Value = JSoftFloat.float64_to_float128(value.doubleValue());
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
      final int intValue = JSoftFloat.float64_to_int32(value.doubleValue());
      return BitVector.valueOf(intValue, size); 
    }

    if (size == 64) {
      final long longValue = JSoftFloat.float64_to_int64(value.doubleValue());
      return BitVector.valueOf(longValue, size); 
    }

    throw new UnsupportedOperationException(String.format(
        "Conversion from %s to a %d-bit integer is not supported.",
        value.getPrecision().getText(), size)
    );
  }

  @Override
  public FloatX fromInteger(final BitVector value) {
    final double result;
    final int size = value.getBitSize();

    if (size == 32) {
      result = JSoftFloat.int32_to_float64(value.intValue());
    } else if (size == 64) {
      result = JSoftFloat.int64_to_float64(value.longValue());
    } else {
      throw new UnsupportedOperationException(String.format(
          "Conversion from a %d-bit integer to %s is not supported.",
          size, Precision.FLOAT64.getText()));
    }

    return newFloatX(result);
  }

  @Override
  public String toString(final FloatX arg) {
    return Double.toString(arg.doubleValue());
  }

  @Override
  public String toHexString(final FloatX arg) {
    return Double.toHexString(arg.doubleValue());
  }

  static FloatX newFloatX(final double value) {
    return new FloatX(
        BitVector.valueOf(Double.doubleToRawLongBits(value), Double.SIZE),
        Precision.FLOAT64
    );
  }
}
