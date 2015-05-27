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

package ru.ispras.microtesk.test.sequence.internal;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.test.sequence.iterator.Iterator;

/**
 * This class is a basic class for composite iterators (e.g., combinators or compositors).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class CompositeIterator<T> {
  /** The sub-iterators (i.e., iterators to be combined or composed). */
  protected ArrayList<Iterator<T>> iterators = new ArrayList<Iterator<T>>();

  /**
   * Returns the list of sub-iterators.
   * 
   * @return the list of sub-iterators.
   */
  public List<Iterator<T>> getIterators() {
    return iterators;
  }

  /**
   * Adds the sub-iterator into the list.
   * 
   * @param iterator the sub-iterator to be added to the list.
   */
  public void addIterator(final Iterator<T> iterator) {
    iterators.add(iterator);
  }

  /**
   * Adds the sub-iterators into the list.
   * 
   * @param iterators the sub-iterators to be added to the list.
   */
  public void addIterators(final List<Iterator<T>> iterators) {
    this.iterators.addAll(iterators);
  }

  /**
   * Removes the i-th sub-iterator from the list.
   * 
   * @param i the index of the sub-iterator to be removed from the list.
   */
  public void removeIterator(int i) {
    iterators.remove(i);
  }

  /**
   * Removes all sub-iterators from the list.
   */
  public void removeIterators() {
    iterators.clear();
  }

  /**
   * Returns the number of iterators in the list.
   * 
   * @return the size of the sub-iterator list.
   */
  public int size() {
    return iterators.size();
  }
}
