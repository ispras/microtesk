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
import java.util.List;

import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.coverage.MemoryAccessPathChooser;
import ru.ispras.testbase.knowledge.iterator.CollectionIterator;
import ru.ispras.testbase.knowledge.iterator.ProductIterator;

/**
 * {@link MemoryAccessIteratorExhaustive} implements an exhaustive iterator of memory access
 * skeletons, i.e. sequences of memory accesses.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessIteratorExhaustive extends MemoryAccessIterator {
  private final ProductIterator<MemoryAccessPathChooser> iterator = new ProductIterator<>();

  private List<MemoryAccessPath> paths = null;

  public MemoryAccessIteratorExhaustive(
      final List<MemoryAccessType> accessTypes,
      final List<Collection<MemoryAccessPathChooser>> accessPathChoosers) {
    super(accessTypes, accessPathChoosers);

    for (final Collection<MemoryAccessPathChooser> choosers : accessPathChoosers) {
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
  public List<MemoryAccessPath> getAccessPaths() {
    return paths;
  }

  @Override
  public void next() {
    while (iterator.hasValue()) {
      final List<MemoryAccessPath> result = new ArrayList<>(iterator.size());
      final List<MemoryAccessPathChooser> choosers = iterator.value();

      for (final MemoryAccessPathChooser chooser : choosers) {
        final MemoryAccessPath path = chooser.get();

        if (path == null) {
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
}
