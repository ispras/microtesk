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

package ru.ispras.microtesk.test.template;

import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.LazyData;
import ru.ispras.microtesk.test.template.Primitive;

public final class Preparator {
  private final LazyPrimitive targetHolder;
  private final LazyData dataHolder;
  private final List<Call> calls;

  Preparator(LazyPrimitive targetHolder, LazyData dataHolder, List<Call> calls) {
    checkNotNull(targetHolder);
    checkNotNull(dataHolder);
    checkNotNull(calls);

    this.targetHolder = targetHolder;
    this.dataHolder = dataHolder;
    this.calls = Collections.unmodifiableList(calls);
  }

  public String getTargetName() {
    return targetHolder.getName();
  }

  public List<Call> makeInitializer(Primitive target, BitVector data) {
    checkNotNull(target);
    checkNotNull(data);

    targetHolder.setSource(target);
    dataHolder.setValue(data);

    return calls;
  }

  private static void checkNotNull(Object o) {
    if (null == o) {
      throw new NullPointerException();
    }
  }
}
