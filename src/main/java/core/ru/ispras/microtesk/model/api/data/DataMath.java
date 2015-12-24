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

package ru.ispras.microtesk.model.api.data;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.fp.FloatX;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.type.TypeId;

public final class DataMath {
  private DataMath() {}

  public static Data sqrt(final Data value) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(value.isType(TypeId.FLOAT));

    final FloatX result = value.floatXValue().sqrt();
    return new Data(value.getType(), result.getData());
  }

  public static boolean isNan(final Data value) {
    InvariantChecks.checkNotNull(value);
    return value.floatXValue().isNan();
  }

  public static boolean isSignalingNan(final Data value) {
    InvariantChecks.checkNotNull(value);
    return value.floatXValue().isSignalingNan();
  }

  public static Data intToFloat(final Type type, final Data value) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(type.getTypeId() == TypeId.FLOAT);
    InvariantChecks.checkTrue(value.getType().getTypeId().isInteger());

    final BitVector source = value.getRawData();
    final FloatX target = FloatX.fromInteger(type.getFieldSize(0), type.getFieldSize(1), source);

    return new Data(type, target.getData());
  }

  public static Data floatToInt(final Type type, final Data value) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(type.getTypeId().isInteger());
    InvariantChecks.checkTrue(value.isType(TypeId.FLOAT));

    final FloatX source = value.floatXValue();
    final BitVector target = source.toInteger(type.getBitSize());

    return new Data(type, target);
  }

  public static Data floatToFloat(final Type type, final Data value) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(type.getTypeId() == TypeId.FLOAT);
    InvariantChecks.checkTrue(value.isType(TypeId.FLOAT));

    final FloatX source = value.floatXValue();
    final FloatX target = source.toFloat(type.getFieldSize(0), type.getFieldSize(1));

    return new Data(type, target.getData());
  }
}
