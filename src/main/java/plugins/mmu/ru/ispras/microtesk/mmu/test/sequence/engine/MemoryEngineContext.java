/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.sequence.engine;

import java.util.Collection;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MemoryEngineContext} stores information required by an {@link MemoryEngine}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryEngineContext {
  // TODO: Integration with MMU TestGen (to be removed).
  private final Iterator<MemoryAccessStructure> structureIterator;

  private final Map<MmuAddressType, Predicate<Long>> hitCheckers;
  private final Collection<IntegerConstraint<IntegerField>> constraints;

  public MemoryEngineContext(
      final Iterator<MemoryAccessStructure> structureIterator,
      final Map<MmuAddressType, Predicate<Long>> hitCheckers,
      final Collection<IntegerConstraint<IntegerField>> constraints) {
    InvariantChecks.checkNotNull(hitCheckers);
    InvariantChecks.checkNotNull(constraints);

    this.structureIterator = structureIterator;
    this.hitCheckers = hitCheckers;
    this.constraints = constraints;
  }

  public Iterator<MemoryAccessStructure> getStructureIterator() {
    return structureIterator;
  }

  public Map<MmuAddressType, Predicate<Long>> getHitCheckers() {
    return hitCheckers;
  }

  public Collection<IntegerConstraint<IntegerField>> getConstraints() {
    return constraints;
  }
}
