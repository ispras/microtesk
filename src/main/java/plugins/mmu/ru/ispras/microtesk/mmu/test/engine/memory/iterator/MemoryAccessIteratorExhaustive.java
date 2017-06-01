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

package ru.ispras.microtesk.mmu.test.engine.memory.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.engine.memory.MemoryAccessChooser;
import ru.ispras.testbase.knowledge.iterator.CollectionIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.ProductIterator;

/**
 * {@link MemoryAccessIteratorExhaustive} implements an exhaustive iterator of memory access
 * skeletons, i.e. sequences of memory accesses.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessIteratorExhaustive implements Iterator<List<MemoryAccess>> {
  private final ProductIterator<MemoryAccessChooser> iterator = new ProductIterator<>();

  private List<MemoryAccess> accesses = null;

  public MemoryAccessIteratorExhaustive(final List<Collection<MemoryAccessChooser>> accessChoosers) {
    InvariantChecks.checkNotNull(accessChoosers);

    for (final Collection<MemoryAccessChooser> choosers : accessChoosers) {
      iterator.registerIterator(new CollectionIterator<>(choosers));
    }
  }

  @Override
  public void init() {
    iterator.init();
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
    while (iterator.hasValue()) {
      final List<MemoryAccess> result = new ArrayList<>(iterator.size());
      final List<MemoryAccessChooser> choosers = iterator.value();

      for (final MemoryAccessChooser chooser : choosers) {
        final MemoryAccess access = chooser.get();

        if (access == null) {
          iterator.next();
          break;
        }

        result.add(access);
      }

      if (result.size() == iterator.size()) {
        accesses = result;
        return;
      }
    }

    accesses = null;
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
