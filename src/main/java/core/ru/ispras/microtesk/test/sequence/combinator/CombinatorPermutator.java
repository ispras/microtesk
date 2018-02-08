/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.permutator.Permutator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.List;

public final class CombinatorPermutator<T> implements Combinator<T> {
  private final Combinator<T> combinator;
  private final Permutator<T> permutator;

  private boolean hasValue;

  public CombinatorPermutator(final Combinator<T> combinator, final Permutator<T> permutator) {
    InvariantChecks.checkNotNull(combinator);
    InvariantChecks.checkNotNull(permutator);

    this.combinator = combinator;
    this.permutator = permutator;
  }

  private void initCombinator() {
    combinator.init();
  }

  private void initPermutator() {
    permutator.initialize(combinator.value());
    permutator.init();
  }

  @Override
  public void initialize(final List<Iterator<T>> iterators) {
    InvariantChecks.checkNotNull(iterators);
    combinator.initialize(iterators);
  }

  @Override
  public void init() {
    initCombinator();

    if (combinator.hasValue()) {
      initPermutator();
    }

    hasValue = combinator.hasValue() && permutator.hasValue();
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  @Override
  public List<T> value() {
    return permutator.value();
  }

  @Override
  public void next() {
    permutator.next();
    if (permutator.hasValue()) {
      return;
    }

    combinator.next();
    if (combinator.hasValue()) {
      initPermutator();
      return;
    }

    hasValue = false;
  }

  @Override
  public void stop() {
    hasValue = false;
  }

  @Override
  public Iterator<List<T>> clone() {
    throw new UnsupportedOperationException();
  }
}
