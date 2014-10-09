/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Preparator.java, Oct 7, 2014 5:00:43 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.test.preparator;

import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.LazyData;
import ru.ispras.microtesk.test.template.Primitive;

public class Preparator {
  private final String target;
  private final LazyData data;
  private final List<Call> calls;

  public Preparator(String target, LazyData data, List<Call> calls) {
    if (null == target) {
      throw new NullPointerException();
    }
    if (null == data) {
      throw new NullPointerException();
    }
    if (null == calls) {
      throw new NullPointerException();
    }

    this.target = target;
    this.data = data;
    this.calls = Collections.unmodifiableList(calls);
  }

  public String getTarget() {
    return target;
  }

  public List<Call> makeInitializer(Primitive target, BitVector value) {
    if (null == target) {
      throw new NullPointerException();
    }
    if (null == value) {
      throw new NullPointerException();
    }

    data.setValue(value);
    return calls;
  }
}
