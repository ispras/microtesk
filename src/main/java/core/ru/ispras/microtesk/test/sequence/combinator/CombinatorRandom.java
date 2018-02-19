/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.combinator;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link CombinatorRandom} implements the random combinator of iterators.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class CombinatorRandom<T> extends CombinatorBase<T> {
  private final List<List<T>> sequences = new ArrayList<>();
  private final int iterationLimit = 1;
  private int iterationNum = 0;

  @Override
  public void onInit() {
    sequences.clear();
    iterationNum = 0;

    for (final Iterator<T> it : iterators) {
      final List<T> sequence = new ArrayList<>();
      for (; it.hasValue(); it.next()) {
        sequence.add(it.value());
      }
      shuffle(sequence);

      sequences.add(sequence);
    }
  }

  @Override
  public T getValue(final int i) {
    final List<T> s = sequences.get(i);
    return s.get(iterationNum % s.size());
  }

  @Override
  public boolean doNext() {
    return ++iterationNum < iterationLimit;
  }

  /**
   * Randomize list elements order using internal RNG.
   */
  private static <U> void shuffle(final List<U> list) {
    for (int i = 0; i < list.size(); ++i) {
      final int j = Randomizer.get().nextIntRange(i, list.size() - 1);
      Collections.swap(list, i, j);
    }
  }
}
