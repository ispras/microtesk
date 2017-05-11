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
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.test.engine.memory.BufferDependency;
import ru.ispras.microtesk.mmu.test.engine.memory.BufferHazard;
import ru.ispras.microtesk.mmu.test.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.test.engine.memory.MemoryEngineUtils;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
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
      final MemoryAccess access2) {
    InvariantChecks.checkNotNull(access1);
    InvariantChecks.checkNotNull(access2);

    this.allPossibleDependencies = getAllPossibleDependencies(access1, access2);
  }

  @Override
  public MemoryDependencyIterator clone() {
    throw new UnsupportedOperationException();
  }

  private static BufferDependency[] getAllPossibleDependencies(
      final MemoryAccess access1,
      final MemoryAccess access2) {
    final Collection<MmuBufferAccess> bufferAccesses1 = access1.getPath().getBufferReads();
    final Collection<MmuBufferAccess> bufferAccesses2 = access2.getPath().getBufferReads();

    final List<MmuBufferAccess> bufferAccesses =
        new ArrayList<>(bufferAccesses1.size() + bufferAccesses2.size());

    bufferAccesses.addAll(bufferAccesses1);
    bufferAccesses.addAll(bufferAccesses2);

    Collection<BufferDependency> bufferDependencies =
        Collections.<BufferDependency>singleton(new BufferDependency());

    for (int i = 0; i < bufferAccesses.size() - 1; i++) {
      for (int j = i + 1; j < bufferAccesses.size(); j++) {
        final MmuBufferAccess bufferAccess1 = bufferAccesses.get(i);
        final MmuBufferAccess bufferAccess2 = bufferAccesses.get(j);

        final MmuBuffer buffer1 = bufferAccess1.getBuffer();
        final MmuBuffer buffer2 = bufferAccess2.getBuffer();

        final MemoryAccessContext context1 = bufferAccess1.getContext();
        final MemoryAccessContext context2 = bufferAccess2.getContext();

        Logger.debug("Checking dependencies between %s and %s", bufferAccess1, bufferAccess2);

        // FIXME: Some restrictions need to be weakened.
        if (!buffer1.isFake()
            && buffer1.getKind() != MmuBuffer.Kind.MEMORY
            && buffer1 == buffer2
            && context1.isEmptyStack()
            && context2.isEmptyStack()
            && context1.getBufferAccessId(buffer1) == MemoryAccessContext.BUFFER_ACCESS_INITIAL_ID
            && context2.getBufferAccessId(buffer1) == MemoryAccessContext.BUFFER_ACCESS_INITIAL_ID) {
          Logger.debug("Enumerating dependencies between %s and %s", bufferAccess1, bufferAccess2);

          final Collection<BufferHazard> hazardTypes = BufferHazard.getHazards(buffer1);
          final Collection<BufferHazard.Instance> hazardInstances = new ArrayList<>(hazardTypes.size());

          for (final BufferHazard hazardType : hazardTypes) {
            final BufferHazard.Instance hazardInstance =
                hazardType.makeInstance(bufferAccess1, bufferAccess2);

            Logger.debug("Possible dependencies: hazard %s", hazardInstance);
            hazardInstances.add(hazardInstance);
          }

          bufferDependencies = refineDependencies(
              bufferDependencies, access1, access2, hazardInstances);

          if (bufferDependencies.isEmpty()) {
            Logger.debug("Possible dependencies: break");
            break;
          }
        }
      }
    }

    Logger.debug("Possible dependencies: %d", bufferDependencies.size());
    return bufferDependencies.toArray(new BufferDependency[]{});
  }

  private static Collection<BufferDependency> refineDependencies(
      final Collection<BufferDependency> oldDependencies,
      final MemoryAccess access1,
      final MemoryAccess access2,
      final Collection<BufferHazard.Instance> hazards) {

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

        if (MemoryEngineUtils.isFeasibleStructure(structure)) {
          newDependencies.add(newDependency);
        }
      }
    }

    return newDependencies;
  }
}
