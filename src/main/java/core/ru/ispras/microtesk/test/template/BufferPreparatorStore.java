/*
 * Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

public final class BufferPreparatorStore {
  private final Map<String, BufferPreparator> preparators;

  public BufferPreparatorStore() {
    this.preparators = new HashMap<>();
  }

  public void addPreparator(final BufferPreparator preparator) {
    InvariantChecks.checkNotNull(preparator);

    final String id = getBufferIdWithLevel(preparator.getBufferId(), preparator.getLevel());
    preparators.put(id, preparator);
  }

  public BufferPreparator getPreparatorFor(final String bufferId, final int level) {
    InvariantChecks.checkNotNull(bufferId);
    InvariantChecks.checkGreaterOrEqZero(level);

    final String id = getBufferIdWithLevel(bufferId, level);
    return preparators.get(id);
  }

  public BufferPreparator getPreparatorFor(final String bufferId) {
    return getPreparatorFor(bufferId, 0);
  }

  private static String getBufferIdWithLevel(final String bufferId, final int level) {
    return String.format("%s#%d", bufferId, level);
  }
}
