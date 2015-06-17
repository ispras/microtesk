/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.LazyData;
import ru.ispras.microtesk.test.template.LazyValue;

public final class PreparatorBuilder {
  private final MetaAddressingMode targetMetaData;

  private final LazyPrimitive target;
  private final LazyData data;
  private final List<Call> calls;

  private BitVector mask;
  private BitVector value;
  private final Map<String, BitVector> arguments;

  PreparatorBuilder(final MetaAddressingMode targetMetaData) {
    checkNotNull(targetMetaData);

    this.targetMetaData = targetMetaData;
    final String targetName = targetMetaData.getName();

    this.target = new LazyPrimitive(Primitive.Kind.MODE, targetName, targetName);
    this.data = new LazyData();
    this.calls = new ArrayList<Call>();

    this.mask = null;
    this.value = null;
    this.arguments = new LinkedHashMap<>();
  }

  public void setMask(final BigInteger maskValue) {
    checkNotNull(maskValue);
    mask = BitVector.valueOf(maskValue, targetMetaData.getDataType().getBitSize());
  }

  public void setValue(final BigInteger valueValue) {
    checkNotNull(valueValue);
    value = BitVector.valueOf(valueValue, targetMetaData.getDataType().getBitSize());
  }

  public void addArgument(final String argumentName, final BigInteger argumentValue) {
    checkNotNull(argumentName);
    checkNotNull(argumentValue);

    final MetaArgument metaArgument = targetMetaData.getArgument(argumentName);
    if (null == metaArgument) {
      throw new IllegalArgumentException(String.format(
          "The %s argument is not defined for the %s addressing mode.",
          argumentName, getTargetName()));
    }

    final int argumentValueBitSize = metaArgument.getDataType().getBitSize();
    arguments.put(argumentName, BitVector.valueOf(argumentValue, argumentValueBitSize));
  }

  public String getTargetName() {
    return target.getName();
  }

  public LazyValue newValue() {
    return new LazyValue(data);
  }

  public LazyValue newValue(final int start, final int end) {
    return new LazyValue(data, start, end);
  }

  public Primitive getTarget() {
    return target;
  }

  public void addCall(final Call call) {
    checkNotNull(call);
    calls.add(call);
  }

  public Preparator build() {
    return new Preparator(target, data, calls, mask, value, arguments);
  }
}
