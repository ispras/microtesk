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

  private static FloatX newFloatX(final FloatX80 value) {
    final BitVector data = BitVector.newEmpty(80);

    data.field(0,  63).assign(BitVector.valueOf(value.low,  64));
    data.field(64, 79).assign(BitVector.valueOf(value.high, 16));

    return new FloatX(data, Precision.FLOAT128);
  }
}
