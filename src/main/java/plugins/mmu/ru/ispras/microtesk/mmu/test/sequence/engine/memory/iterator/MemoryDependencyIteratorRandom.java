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

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryDependency;
import ru.ispras.microtesk.utils.function.Predicate;

/**
 * {@link MemoryDependencyIteratorRandom} implements a random iterator of dependencies between
 * memory accesses.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryDependencyIteratorRandom extends MemoryDependencyIterator {

  private boolean hasValue;

  public MemoryDependencyIteratorRandom(
      final MemoryAccess access1,
      final MemoryAccess access2,
      final Predicate<MemoryAccessStructure> checker) {
    super(access1, access2, checker);
  }

  @Override
  public void init() {
    hasValue = allPossibleDependencies.length != 0;
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  @Override
  public MemoryDependency value() {
    return Randomizer.get().choose(allPossibleDependencies);
  }

  @Override
  public void next() {
    // Do nothing.
  }

  @Override
  public void stop() {
    hasValue = false;
  }
}
