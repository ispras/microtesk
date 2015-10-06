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
    if (first.equals(second)){
      return 0;
    }

    return JSoftFloat.float128_lt(
        newFloat128(first),
        newFloat128(second)) ? -1 : 1;
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

  private static FloatX newFloatX(final Float128 value) {
    final BitVector data = BitVector.newEmpty(128);

    data.field(0,   63).assign(BitVector.valueOf(value.low,  64));
    data.field(64, 127).assign(BitVector.valueOf(value.high, 64));

    return new FloatX(data, Precision.FLOAT128);
  }
}
