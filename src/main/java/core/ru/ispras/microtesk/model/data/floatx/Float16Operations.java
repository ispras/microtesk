/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.data.floatx;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.softfloat.Float128;
import ru.ispras.softfloat.Float16;
import ru.ispras.softfloat.Float16Functions;

/**
 * {@link Float128Operations} implements floating-point operations
 * for 16-bit types.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class Float16Operations implements Operations {
  private static Operations instance = null;

  public static Operations get() {
    if (null == instance) {
      instance = new Float16Operations();
    }
    return instance;
  }

  private Float16Operations() {}

  @Override
  public FloatX add(final FloatX lhs, final FloatX rhs) {
    final Float16 result = Float16Functions.float16_add(newFloat16(lhs), newFloat16(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX sub(final FloatX lhs, final FloatX rhs) {
    final Float16 result = Float16Functions.float16_sub(newFloat16(lhs), newFloat16(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX mul(final FloatX lhs, final FloatX rhs) {
    final Float16 result = Float16Functions.float16_mul(newFloat16(lhs), newFloat16(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX div(final FloatX lhs, final FloatX rhs) {
    final Float16 result = Float16Functions.float16_div(newFloat16(lhs), newFloat16(rhs));
    return newFloatX(result);
  }

  @Override
  public FloatX rem(final FloatX lhs, final FloatX rhs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public FloatX sqrt(final FloatX arg) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public int compare(final FloatX first, final FloatX second) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isNan(final FloatX arg) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSignalingNan(final FloatX arg) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public FloatX round(final FloatX value) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public FloatX toFloat(final FloatX value, final Precision precision) {
    switch (precision) {
      case FLOAT32: {
        final float float32Value = Float16Functions.f16_to_f32(newFloat16(value));
        return Float32Operations.newFloatX(float32Value);
      }

      case FLOAT64: {
        final double float64Value = Float16Functions.f16_to_f64(newFloat16(value));
        return Float64Operations.newFloatX(float64Value);
      }

      case FLOAT128: {
        final Float128 float128Value = Float16Functions.f16_to_f128(newFloat16(value));
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
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public FloatX fromInteger(final BitVector value) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString(final FloatX arg) {
    final Float16 value = newFloat16(arg);
    return value.toString();
  }

  @Override
  public String toHexString(final FloatX arg) {
    return toString(arg);
  }

  private static Float16 newFloat16(final FloatX value) {
    final BitVector data = value.getData();
    final int intValue = data.intValue() & 0x0000FFFF;
    return new Float16(intValue);
  }

  static FloatX newFloatX(final Float16 value) {
    return new FloatX(
        BitVector.valueOf(value.float16b & 0x0000FFFF, 16),
        Precision.FLOAT16
    );
  }
}
