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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.coverage.MemoryAccessPathChooser;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MemoryAccessIterator} is a base iterator of memory access skeletons, i.e.
 * sequences of memory accesses.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class MemoryAccessIterator implements Iterator<List<MemoryAccess>> {
  protected final List<MemoryAccessType> accessTypes;
  protected final List<Collection<MemoryAccessPathChooser>> accessPathChoosers;

  protected MemoryAccessIterator(
      final List<MemoryAccessType> accessTypes,
      final List<Collection<MemoryAccessPathChooser>> accessPathChoosers) {
    InvariantChecks.checkNotNull(accessTypes);
    InvariantChecks.checkNotNull(accessPathChoosers);

    this.accessTypes = accessTypes;
    this.accessPathChoosers = accessPathChoosers;
  }

  protected abstract List<MemoryAccessPath> getAccessPaths();

  @Override
  public List<MemoryAccess> value() {
    final List<MemoryAccess> result = new ArrayList<>(accessTypes.size());
    final List<MemoryAccessPath> accessPaths = getAccessPaths();

    for (int i = 0; i < accessTypes.size(); i++) {
      final MemoryAccessType accessType = accessTypes.get(i);
      final MemoryAccessPath accessPath = accessPaths.get(i);

      result.add(new MemoryAccess(accessType, accessPath));
    }

    return result;
  }

  @Override
  public MemoryAccessIterator clone() {
    throw new UnsupportedOperationException();
  }
}
