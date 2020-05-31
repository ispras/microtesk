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

import ru.ispras.fortress.util.InvariantChecks;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link MemoryPreparatorStore} class stores a collection of memory preparators.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class MemoryPreparatorStore
    implements CodeBlockCollection<MemoryPreparator> {
  private final Map<Integer, MemoryPreparator> preparators;

  public MemoryPreparatorStore() {
    this.preparators = new HashMap<>();
  }

  @Override
  public void add(MemoryPreparator p) {
    addPreparator(p);
  }

  public void addPreparator(final MemoryPreparator preparator) {
    InvariantChecks.checkNotNull(preparator);
    preparators.put(preparator.getDataSize(), preparator);
  }

  public MemoryPreparator getPreparatorFor(final int dataSize) {
    InvariantChecks.checkGreaterOrEqZero(dataSize);
    return preparators.get(dataSize);
  }
}
