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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.LazyData;
import ru.ispras.microtesk.test.template.Primitive;

public final class Preparator {
  private final LazyPrimitive targetHolder;
  private final LazyData dataHolder;
  private final List<Call> calls;

  private final BitVector mask;
  private final BitVector value;
  private final Map<String, BitVector> arguments;

  Preparator(
      final LazyPrimitive targetHolder,
      final LazyData dataHolder,
      final List<Call> calls,
      final BitVector mask,
      final BitVector value,
      final Map<String, BitVector> arguments) {
    checkNotNull(targetHolder);
    checkNotNull(dataHolder);
    checkNotNull(calls);
    checkNotNull(arguments);

    this.targetHolder = targetHolder;
    this.dataHolder = dataHolder;
    this.calls = Collections.unmodifiableList(calls);

    this.mask = mask;
    this.value = value;
    this.arguments = arguments;
  }

  public String getTargetName() {
    return targetHolder.getName();
  }

  public boolean isDefault() {
    return null == mask && null == value && arguments.isEmpty();
  }

  public List<Call> makeInitializer(final Primitive target, final BitVector data) {
    checkNotNull(target);
    checkNotNull(data);

    targetHolder.setSource(target);
    dataHolder.setValue(data);

    return calls;
  }
}
