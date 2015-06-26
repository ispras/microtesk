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

package ru.ispras.microtesk.test.template;

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

public final class LazyLabel {
  private final MemoryMap memoryMap; 
  private final LazyData data;
  private final LazyValue value;

  private String name;

  protected LazyLabel(final MemoryMap memoryMap) {
    InvariantChecks.checkNotNull(memoryMap);

    this.memoryMap = memoryMap;
    this.data = new LazyData();
    this.value = new LazyValue(data);

    this.name = null;
  }

  protected LazyLabel(final LazyLabel other) {
    InvariantChecks.checkNotNull(other);

    final LazyValue copyValue = new LazyValue(other.value);
    final LazyData copyData = copyValue.getData();

    this.memoryMap = other.memoryMap;
    this.data = copyData;
    this.value = copyValue;

    this.name = other.name;
  }

  public void setSource(final String labelName) {
    InvariantChecks.checkNotNull(labelName);

    this.name = labelName;

    final BigInteger fakeValue = BigInteger.ZERO;
    final BigInteger address = memoryMap.resolveWithDefault(name, fakeValue);

    // TODO: It would be better to have here precise size.
    data.setValue(BitVector.valueOf(address, Long.SIZE + 1));
  }

  public String getName() {
    return name;
  }

  public LazyValue getValue() {
    return value;
  }
}
