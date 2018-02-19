/*
 * Copyright 2017-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.engine.memory;

import ru.ispras.fortress.randomizer.Randomizer;

/**
 * {@link DependencyIteratorRandom} implements a random iterator of dependencies between
 * two memory accesses.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class DependencyIteratorRandom extends DependencyIterator {

  private boolean hasValue;

  public DependencyIteratorRandom(
      final Access access1,
      final Access access2) {
    super(access1, access2);
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
  public BufferDependency value() {
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
