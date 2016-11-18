/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use buffer file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.mmu.translator.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.utils.function.Chooser;

public final class MemoryAccessPathExtractor implements Chooser<MemoryAccessPath> {
  private final Collection<Iterator<MemoryAccessPath>> iterators = new ArrayList<>();
  private final Collection<MemoryAccessPath> paths = new ArrayList<>(); 

  public MemoryAccessPathExtractor(
      final MmuSubsystem memory,
      final Set<MemoryGraph> graphs) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(graphs);

    for (final MemoryGraph graph : graphs) {
      iterators.add(new MemoryAccessPathIterator(memory, graph));
    }
  }

  @Override
  public MemoryAccessPath get() {
    while (!iterators.isEmpty()) {
      final Iterator<MemoryAccessPath> iterator = Randomizer.get().choose(iterators);

      if (iterator.hasNext()) {
        final MemoryAccessPath path = iterator.next();
        InvariantChecks.checkNotNull(path);

        paths.add(path);
        return path;
      }

      iterators.remove(iterator);
    }

    return Randomizer.get().choose(paths);
  }
}
