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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.BufferHazard;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MemoryDependencyIterator} is a base iterator of dependencies between memory accesses.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public abstract class MemoryDependencyIterator implements Iterator<BufferDependency> {

  protected final BufferDependency[] allPossibleDependencies;

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

  private static BufferDependency[] getAllPossibleDependencies(
      final MemoryAccess access1,
      final MemoryAccess access2,
      final Predicate<MemoryAccessStructure> checker) {
    final Collection<MmuBufferAccess> bufferAccesses1 = access1.getPath().getBufferAccesses();
    final Collection<MmuBufferAccess> bufferAccesses2 = access2.getPath().getBufferAccesses();

    final Collection<MmuBufferAccess> bufferAccesses =
        new ArrayList<>(bufferAccesses1.size() + bufferAccesses2.size());

    bufferAccesses.addAll(bufferAccesses1);
    bufferAccesses.addAll(bufferAccesses2);

    Collection<BufferDependency> bufferDependencies =
        Collections.<BufferDependency>singleton(new BufferDependency());

    for (final MmuBufferAccess bufferAccess1 : bufferAccesses) {
      for (final MmuBufferAccess bufferAccess2 : bufferAccesses) {
        final MmuBuffer buffer1 = bufferAccess1.getBuffer();
        final MmuBuffer buffer2 = bufferAccess2.getBuffer();

        // Different accesses to the same buffer.
        if (bufferAccess1 != bufferAccess2 && buffer1 == buffer2) {
          final Collection<BufferHazard> hazardTypes = BufferHazard.getHazards(buffer1);
          final Collection<BufferHazard.Instance> hazardInstances = new ArrayList<>(hazardTypes.size());

          for (final BufferHazard hazardType : hazardTypes) {
            hazardInstances.add(hazardType.makeInstance(bufferAccess1, bufferAccess2));
          }

          bufferDependencies = refineDependencies(
              bufferDependencies, access1, access2, hazardInstances, checker);

          if (bufferDependencies.isEmpty()) {
            break;
          }
        }
      }
    }

    return bufferDependencies.toArray(new BufferDependency[]{});
  }

  private static Collection<BufferDependency> refineDependencies(
      final Collection<BufferDependency> oldDependencies,
      final MemoryAccess access1,
      final MemoryAccess access2,
      final Collection<BufferHazard.Instance> hazards,
      final Predicate<MemoryAccessStructure> checker) {

    if (hazards.isEmpty()) {
      return oldDependencies;
    }

    final Collection<BufferDependency> newDependencies =
        new ArrayList<>(oldDependencies.size() * hazards.size());

    for (final BufferDependency oldDependency : oldDependencies) {
      for (final BufferHazard.Instance hazard : hazards) {
        final BufferDependency newDependency = new BufferDependency(oldDependency);
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
