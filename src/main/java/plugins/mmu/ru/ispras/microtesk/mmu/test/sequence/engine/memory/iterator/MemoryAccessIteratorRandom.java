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
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryAccessPathChooser;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MemoryAccessIteratorRandom} implements a random iterator of memory access skeletons, i.e.
 * sequences of memory access paths.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessIteratorRandom implements Iterator<List<MemoryAccessPath>> {
  private final List<Collection<MemoryAccessPathChooser>> pathChoosers;

  private List<MemoryAccessPath> paths = null;

  private MemoryAccessIteratorRandom(
      final List<Collection<MemoryAccessPathChooser>> pathChoosers) {
    InvariantChecks.checkNotNull(pathChoosers);
    this.pathChoosers = pathChoosers;
  }

  @Override
  public void init() {
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
    final List<MemoryAccessPath> result = new ArrayList<>(pathChoosers.size());

    for (final Collection<MemoryAccessPathChooser> choosers : pathChoosers) {
      while (!choosers.isEmpty()) {
        final MemoryAccessPathChooser chooser = Randomizer.get().choose(choosers);
        final MemoryAccessPath path = chooser.get();

        if (path == null) {
          choosers.remove(chooser);
        } else {
          result.add(path);
          break;
        }
      }

      if (choosers.isEmpty()) {
        paths = null;
        return;
      }
    }

    paths = result;
  }

  @Override
  public void stop() {
    paths = null;
  }

  @Override
  public MemoryAccessIteratorRandom clone() {
    throw new UnsupportedOperationException();
  }
}
