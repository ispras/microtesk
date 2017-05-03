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

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.coverage.MemoryAccessChooser;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MemoryAccessIteratorRandom} implements a random iterator of memory access skeletons,
 * i.e. sequences of memory accesses.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessIteratorRandom implements Iterator<List<MemoryAccess>> {
  final private List<Collection<MemoryAccessChooser>> accessChoosers;

  private List<MemoryAccess> accesses = null;

  public MemoryAccessIteratorRandom(final List<Collection<MemoryAccessChooser>> accessChoosers) {
    InvariantChecks.checkNotNull(accessChoosers);
    this.accessChoosers = accessChoosers;
  }

  @Override
  public void init() {
    next();
  }

  @Override
  public boolean hasValue() {
    return accesses != null;
  }

  @Override
  public List<MemoryAccess> value() {
    return accesses;
  }

  @Override
  public void next() {
    final List<MemoryAccess> result = new ArrayList<>(accessChoosers.size());

    for (final Collection<MemoryAccessChooser> choosers : accessChoosers) {
      while (!choosers.isEmpty()) {
        final MemoryAccessChooser chooser = Randomizer.get().choose(choosers);
        final MemoryAccess access = chooser.get();

        if (access == null) {
          choosers.remove(chooser);
        } else {
          result.add(access);
          break;
        }
      }

      if (choosers.isEmpty()) {
        accesses = null;
        return;
      }
    }

    accesses = result;
  }

  @Override
  public void stop() {
    accesses = null;
  }

  @Override
  public MemoryAccessIteratorExhaustive clone() {
    throw new UnsupportedOperationException();
  }
}
