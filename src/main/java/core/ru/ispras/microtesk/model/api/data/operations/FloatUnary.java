/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.data.operations;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IUnaryOperator;
import ru.ispras.microtesk.model.api.data.Type;
import ru.ispras.microtesk.model.api.data.TypeId;
import ru.ispras.microtesk.model.api.data.fp.FloatX;
import ru.ispras.microtesk.model.api.data.fp.Precision;

public abstract class FloatUnary implements IUnaryOperator {
  public static FloatX dataToFloatX(final Data data) {
    InvariantChecks.checkNotNull(data);

    final Type type = data.getType();
    InvariantChecks.checkTrue(type.getTypeId() == TypeId.FLOAT);

    return new FloatX(
      data.getRawData(), type.getFieldSize(0), type.getFieldSize(1));
  }

  public static Data floatXToData(final FloatX floatX) {
    final Precision precision = floatX.getPrecision();
    return new Data(floatX.getData(), Type.FLOAT(
        precision.getFractionSize(), precision.getExponentSize()));
  }

  @Override
  public final Data execute(final Data data) {
    final FloatX arg = dataToFloatX(data);
    return calculate(arg);
  }

  protected abstract Data calculate(final FloatX arg);

  @Override
  public final boolean supports(final Type argType) {
    return argType.getTypeId() == TypeId.FLOAT;
  }
}
