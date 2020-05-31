/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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
 * The {@link BufferPreparatorStore} class stores a collection of buffer preparators.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class BufferPreparatorStore
    implements CodeBlockCollection<BufferPreparator> {
  private final Map<String, BufferPreparator> preparators;

  public BufferPreparatorStore() {
    this.preparators = new HashMap<>();
  }

  @Override
  public void add(BufferPreparator p) {
    addPreparator(p);
  }

  public void addPreparator(final BufferPreparator preparator) {
    InvariantChecks.checkNotNull(preparator);
    final String id = getBufferIdWithLevels(preparator.getBufferId(), preparator.getLevels());
    preparators.put(id, preparator);
  }

  public BufferPreparator getPreparatorFor(final String bufferId, final int levels) {
    InvariantChecks.checkNotNull(bufferId);
    InvariantChecks.checkGreaterOrEqZero(levels);

    final String id = getBufferIdWithLevels(bufferId, levels);
    return preparators.get(id);
  }

  public BufferPreparator getPreparatorFor(final String bufferId) {
    return getPreparatorFor(bufferId, 0);
  }

  private static String getBufferIdWithLevels(final String bufferId, final int levels) {
    return String.format("%s#%d", bufferId, levels);
  }
}
