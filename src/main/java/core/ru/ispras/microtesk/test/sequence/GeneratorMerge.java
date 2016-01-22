/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.combinator.Combinator;
import ru.ispras.microtesk.test.sequence.compositor.Compositor;
import ru.ispras.microtesk.test.sequence.permutator.Permutator;
import ru.ispras.testbase.knowledge.iterator.CollectionIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link GeneratorMerge} implements the test sequence generator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class GeneratorMerge<T> implements Generator<T> {
  /** Produces different combinations of the sequences. */
  private final Combinator<List<T>> combinator;
  /** Merges several sequences into one. */
  private final Compositor<T> compositor;
  /** Permutes a single sequence. */
  private final Permutator<T> permutator;

  /** The list of iterators. */
  private final List<Iterator<List<T>>> iterators;

  private boolean hasValue = true;

  public GeneratorMerge(
      final Combinator<List<T>> combinator,
      final Compositor<T> compositor,
      final Permutator<T> permutator,
      final List<Iterator<List<T>>> iterators) {
    InvariantChecks.checkNotNull(combinator);
    InvariantChecks.checkNotNull(compositor);
    InvariantChecks.checkNotNull(permutator);
    InvariantChecks.checkNotNull(iterators);

    this.combinator = combinator;
    this.compositor = compositor;
    this.permutator = permutator;
    this.iterators = iterators;
  }

  private void initCombinator() {
    combinator.setIterators(iterators);
    combinator.init();
  }

  private void initPermutator() {
    final List<List<T>> combination = combinator.value();

    compositor.removeIterators();
    for (final List<T> sequence : combination) {
      compositor.addIterator(new CollectionIterator<T>(sequence));
    }

    final List<T> sequence = new ArrayList<T>();
    for (compositor.init(); compositor.hasValue(); compositor.next()) {
      sequence.add(compositor.value());
    }

    permutator.setSequence(sequence);
    permutator.init();
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
