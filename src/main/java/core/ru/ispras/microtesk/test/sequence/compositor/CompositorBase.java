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

package ru.ispras.microtesk.test.sequence.compositor;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.internal.CompositeIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.List;

/**
 * {@link CompositorBase} is a basic compositor of iterators. It takes several iterators and merges
 * them into a single iterator. The main restriction is that a a compositor should not change
 * the order of items returned by an iterator.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
abstract class CompositorBase<T> extends CompositeIterator<T> implements Compositor<T> {
  /** The currently chosen iterator. */
  private Iterator<T> chosen;

  /**
   * Constructs a compositor with the empty list of iterators.
   */
  public CompositorBase() {
    // Do nothing.
  }

  /**
   * Constructs a compositor with the given list of iterators.
   *
   * @param iterators the list of iterators to be composed.
   */
  public CompositorBase(final List<Iterator<T>> iterators) {
    addIterators(iterators);
  }

  //------------------------------------------------------------------------------------------------
  // Callback methods that should be overloaded in subclasses
  //------------------------------------------------------------------------------------------------

  /**
   * The callback method called in the {@code init} method.
   */
  protected abstract void onInit();

  /**
   * The callback method called in the {@code next} method.
   */
  protected abstract void onNext();

  /**
   * Selects an iterator whose value will be used at the current step.
   *
   * @return one of the iterators from the compositor's list.
   */
  protected abstract Iterator<T> choose();

  //------------------------------------------------------------------------------------------------
  // Callback-based implementation of the iterator method
  //------------------------------------------------------------------------------------------------

  @Override
  public void initialize(final List<Iterator<T>> iterators) {
    InvariantChecks.checkNotNull(iterators);
    setIterators(iterators);
  }

  @Override
  public void init() {
    for (Iterator<T> iterator : iterators) {
      iterator.init();
    }

    onInit();

    chosen = choose();
  }

  @Override
  public boolean hasValue() {
    while (chosen != null) {
      if (chosen.hasValue()) {
        return true;
      }

      chosen = choose();
    }

    return false;
  }

  @Override
  public T value() {
    return chosen.value();
  }

  @Override
  public void next() {
    chosen.next();

    onNext();

    chosen = choose();
  }

  @Override
  public void stop() {
    chosen = null;
  }

  @Override
  public CompositorBase<T> clone() {
    throw new UnsupportedOperationException();
  }
}
