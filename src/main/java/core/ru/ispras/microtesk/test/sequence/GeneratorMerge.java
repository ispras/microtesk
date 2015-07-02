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

import ru.ispras.microtesk.test.sequence.combinator.Combinator;
import ru.ispras.microtesk.test.sequence.compositor.Compositor;
import ru.ispras.testbase.knowledge.iterator.CollectionIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link GeneratorMerge} implements the test sequence generator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class GeneratorMerge<T> implements Generator<T> {
  /**
   * The combinator used by the generator (it produces different combinations of the sequences).
   */
  private final Combinator<List<T>> combinator;

  /** The compositor used by the generator (it merges several sequences into one). */
  private final Compositor<T> compositor;

  /** The list of iterators. */
  private final List<Iterator<List<T>>> iterators;

  /**
   * Constructs a test sequence generator.
   * 
   * @param combinator the combinator.
   * @param compositor the compositor.
   */

  public GeneratorMerge(final Combinator<List<T>> combinator, final Compositor<T> compositor,
      final List<Iterator<List<T>>> iterators) {
    this.combinator = combinator;
    this.compositor = compositor;
    this.iterators = iterators;
  }

  @Override
  public void init() {
    combinator.removeIterators();
    combinator.addIterators(iterators);

    combinator.init();
  }

  @Override
  public boolean hasValue() {
    return combinator.hasValue();
  }

  @Override
  public List<T> value() {
    final List<List<T>> combination = combinator.value();

    compositor.removeIterators();
    for (final List<T> sequence : combination) {
      compositor.addIterator(new CollectionIterator<T>(sequence));
    }

    final List<T> result = new ArrayList<T>();
    for (compositor.init(); compositor.hasValue(); compositor.next()) {
      result.add(compositor.value());
    }

    return result;
  }

  @Override
  public void next() {
    combinator.next();
  }

  @Override
  public void stop() {
    combinator.stop();
  }

  @Override
  public Iterator<List<T>> clone() {
    throw new UnsupportedOperationException();
  }
}
