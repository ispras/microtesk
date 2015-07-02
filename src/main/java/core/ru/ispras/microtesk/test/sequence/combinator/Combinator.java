/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.basis.iterator.Iterator;
import ru.ispras.microtesk.test.sequence.internal.CompositeIterator;

/**
 * This class is a basic combinator of iterators. It takes several iterators and produces different
 * combinations of their results.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Combinator<T> extends CompositeIterator<T> implements Iterator<List<T>> {
  /** Availability of the value. */
  private boolean hasValue;

  /**
   * Constructs a compositor with the empty list of iterators.
   */
  public Combinator() {}

  /**
   * Constructs a compositor with the given list of iterators.
   * 
   * @param iterators the list of iterators to be composed.
   */
  public Combinator(final List<Iterator<T>> iterators) {
    addIterators(iterators);
  }

  // /////////////////////////////////////////////////////////////////////////
  // Callbacks that should be overloaded in subclasses
  // /////////////////////////////////////////////////////////////////////////

  /**
   * The callback method called in the <code>init</code> method.
   */
  protected abstract void onInit();

  /**
   * The callback method called in the <code>value</code> method.
   * 
   * @param i the iterator index.
   * @return the value of the i-th iterator (<code>null</code> if the iterator has been exhausted,
   *         i.e., no value is available).
   */
  protected abstract T getValue(int i);

  /**
   * The callback method called in the <code>next</code> method.
   * 
   * @return false iff it the combinator has been exhausted.
   */
  protected abstract boolean doNext();

  // /////////////////////////////////////////////////////////////////////////
  // Callback-based implementation of the iterator method
  // /////////////////////////////////////////////////////////////////////////

  @Override
  public void init() {
    for (Iterator<T> iterator : iterators) {
      iterator.init();
    }

    onInit();

    hasValue = true;
  }

  @Override
  public boolean hasValue() {
    if (!hasValue || iterators.isEmpty()) {
      return false;
    }

    for (int i = 0; i < iterators.size(); i++) {
      if (getValue(i) == null) {
        return false;
      }
    }

    return true;
  }

  @Override
  public List<T> value() {
    final List<T> result = new ArrayList<T>(iterators.size());

    for (int i = 0; i < iterators.size(); i++) {
      result.add(getValue(i));
    }

    return result;
  }

  @Override
  public void next() {
    hasValue = doNext();
  }
}
