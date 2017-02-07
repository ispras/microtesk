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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryAccessPathChooser;
import ru.ispras.testbase.knowledge.iterator.CollectionIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.ProductIterator;

/**
 * {@link MemoryAccessIterator} implements exhaustive iterator of c memory access skeletons.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessIteratorExhaustive implements Iterator<List<MemoryAccessPath>> {
  private final ProductIterator<MemoryAccessPathChooser> iterator = new ProductIterator<>();
  private final Set<MemoryAccessPathChooser> infeasible = new HashSet<>();

  private List<MemoryAccessPath> paths = null;

  private MemoryAccessIteratorExhaustive(
      final List<Collection<MemoryAccessPathChooser>> pathChoosers) {
    InvariantChecks.checkNotNull(pathChoosers);

    for (final Collection<MemoryAccessPathChooser> choosers : pathChoosers) {
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
    return paths != null;
  }

  @Override
  public List<MemoryAccessPath> value() {
    return paths;
  }

  @Override
  public void next() {
    while (iterator.hasValue()) {
      final List<MemoryAccessPath> result = new ArrayList<>(iterator.size());
      final List<MemoryAccessPathChooser> choosers = iterator.value();

      for (final MemoryAccessPathChooser chooser : choosers) {
        final MemoryAccessPath path = !infeasible.contains(chooser) ? chooser.get() : null;

        if (path == null) {
          infeasible.add(chooser);
          iterator.next();
          break;
        }

        result.add(path);
      }

      if (result.size() == iterator.size()) {
        paths = result;
        return;
      }
    }

    paths = null;
  }

  @Override
  public void stop() {
    paths = null;
  }

  @Override
  public MemoryAccessIteratorExhaustive clone() {
    throw new UnsupportedOperationException();
  }
}
