/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link MemoryPreparatorBuilder} class is responsible for construction
 * of memory preparators.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class MemoryPreparatorBuilder {
  private final int dataSize;
  private final LazyData address;
  private final LazyData data;
  private final List<Call> calls;

  protected MemoryPreparatorBuilder(final int dataSize) {
    InvariantChecks.checkGreaterOrEqZero(dataSize);

    this.dataSize = dataSize;
    this.address = new LazyData();
    this.data = new LazyData();
    this.calls = new ArrayList<>();
  }

  public LazyValue newDataReference() {
    return new LazyValue(data);
  }

  public LazyValue newDataReference(final int start, final int end) {
    return new LazyValue(data, start, end);
  }

  public LazyValue newAddressReference() {
    return new LazyValue(address);
  }

  public LazyValue newAddressReference(final int start, final int end) {
    return new LazyValue(address, start, end);
  }

  public void addCall(final Call call) {
    InvariantChecks.checkNotNull(call);
    calls.add(call);
  }

  public MemoryPreparator build() {
    return new MemoryPreparator(dataSize, address, data, calls);
  }
}
