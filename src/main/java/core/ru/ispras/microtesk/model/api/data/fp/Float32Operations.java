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
    final float value = JSoftFloat.float32_add(lhs.floatValue(), rhs.floatValue());
    return newFloat32(value);
  }

  @Override
  public FloatX sub(final FloatX lhs, final FloatX rhs) {
    final float value = JSoftFloat.float32_sub(lhs.floatValue(), rhs.floatValue());
    return newFloat32(value);
  }

  @Override
  public FloatX mul(final FloatX lhs, final FloatX rhs) {
    final float value = JSoftFloat.float32_mul(lhs.floatValue(), rhs.floatValue());
    return newFloat32(value);
  }

  @Override
  public FloatX div(final FloatX lhs, final FloatX rhs) {
    final float value = JSoftFloat.float32_div(lhs.floatValue(), rhs.floatValue());
    return newFloat32(value);
  }

  @Override
  public FloatX rem(FloatX lhs, FloatX rhs) {
    final float value = JSoftFloat.float32_rem(lhs.floatValue(), rhs.floatValue());
    return newFloat32(value);
  }

  @Override
  public FloatX sqrt(final FloatX arg) {
    final float value = JSoftFloat.float32_sqrt(arg.floatValue());
    return newFloat32(value);
  }

  private static FloatX newFloat32(final float floatData) {
    return new FloatX(
        BitVector.valueOf(Float.floatToRawIntBits(floatData), Float.SIZE),
        Precision.FLOAT32
    );
  }
}
