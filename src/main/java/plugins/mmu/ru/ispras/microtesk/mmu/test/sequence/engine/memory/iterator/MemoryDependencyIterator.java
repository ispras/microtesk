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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryHazard;
import ru.ispras.microtesk.mmu.translator.coverage.CoverageExtractor;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MemoryDependencyIterator} is a base iterator of dependencies between memory accesses.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public abstract class MemoryDependencyIterator implements Iterator<MemoryDependency> {

  protected final MemoryDependency[] allPossibleDependencies;

  protected MemoryDependencyIterator(
      final MemoryAccess access1,
      final MemoryAccess access2,
      final Predicate<MemoryAccessStructure> checker) {
    InvariantChecks.checkNotNull(access1);
    InvariantChecks.checkNotNull(access2);
    InvariantChecks.checkNotNull(checker);

    this.allPossibleDependencies = getAllPossibleDependencies(access1, access2, checker);
  }

  @Override
  public MemoryDependencyIterator clone() {
    throw new UnsupportedOperationException();
  }

  private static MemoryDependency[] getAllPossibleDependencies(
      final MemoryAccess access1,
      final MemoryAccess access2,
      final Predicate<MemoryAccessStructure> checker) {
    final Collection<MmuBuffer> buffers1 = access1.getPath().getBuffers();
    final Collection<MmuBuffer> buffers2 = access2.getPath().getBuffers();

    // Intersect the sets of buffers used in the memory accesses.
    final Collection<MmuBuffer> buffers = new ArrayList<>(buffers1);
    buffers.retainAll(buffers2);

    final Set<MmuAddressInstance> addresses = new LinkedHashSet<>();
    for (final MmuBuffer buffer : buffers) {
      addresses.add(buffer.getAddress());
    }

    Collection<MemoryDependency> addressDependencies =
        Collections.<MemoryDependency>singleton(new MemoryDependency());

    for (final MmuAddressInstance address : addresses) {
      final Collection<MemoryHazard> hazards = CoverageExtractor.get().getHazards(address);

      addressDependencies = refineDependencies(
          addressDependencies, access1, access2, hazards, checker);
    }

    final List<MemoryDependency> allPossibleDependencies = new ArrayList<>();

    for (final MemoryDependency addressDependency : addressDependencies) {
      Collection<MemoryDependency> bufferDependencies =
          Collections.<MemoryDependency>singleton(addressDependency);

      for (final MmuBuffer buffer : buffers) {
        final Collection<MemoryHazard> hazards = CoverageExtractor.get().getHazards(buffer);

        bufferDependencies = refineDependencies(
            bufferDependencies, access1, access2, hazards, checker);

        if (bufferDependencies.isEmpty()) {
          break;
        }
      }

      allPossibleDependencies.addAll(bufferDependencies);
    }

    return allPossibleDependencies.toArray(new MemoryDependency[]{});
  }

  private static Collection<MemoryDependency> refineDependencies(
      final Collection<MemoryDependency> oldDependencies,
      final MemoryAccess access1,
      final MemoryAccess access2,
      final Collection<MemoryHazard> hazards,
      final Predicate<MemoryAccessStructure> checker) {

    if (hazards.isEmpty()) {
      return oldDependencies;
    }

    final Collection<MemoryDependency> newDependencies =
        new ArrayList<>(oldDependencies.size() * hazards.size());

    for (final MemoryDependency oldDependency : oldDependencies) {
      for (final MemoryHazard hazard : hazards) {
        final MemoryDependency newDependency = new MemoryDependency(oldDependency);
        newDependency.addHazard(hazard);

        final MemoryAccessStructure structure =
            new MemoryAccessStructure(access1, access2, newDependency);

        if (checker.test(structure)) {
          newDependencies.add(newDependency);
        }
      }
    }

    return newDependencies;
  }
}
