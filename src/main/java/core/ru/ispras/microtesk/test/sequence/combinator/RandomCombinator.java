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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;

/**
 * This class implements the random combinator of iterators.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RandomCombinator<T> extends Combinator<T> {
  /** Maps an iterator to the list of previous values. */
  private Map<Integer, ArrayList<T>> caches = new HashMap<Integer, ArrayList<T>>();
  /** Maps an iterator to the current value. */
  private Map<Integer, T> values = new HashMap<Integer, T>();

  /** Contains exhausted iterators. */
  private Set<Integer> exhausted = new HashSet<Integer>();

  @Override
  public void onInit() {
    caches.clear();
    values.clear();

    exhausted.clear();

    for (int i = 0; i < iterators.size(); i++) {
      Iterator<T> iterator = iterators.get(i);

      if (iterator.hasValue()) {
        addValue(i, iterator.value());
        iterator.next();
      }

      if (!iterator.hasValue()) {
        exhausted.add(i);
      }
    }
  }

  @Override
  public T getValue(int i) {
    return values.get(i);
  }

  @Override
  public boolean doNext() {
    if (exhausted.size() == iterators.size()) {
      return false;
    }

    for (int i = 0; i < iterators.size(); i++) {
      final Iterator<T> iterator = iterators.get(i);

      // If the iterator is not exhausted, with probability 0.5 use new value
      if (iterator.hasValue() && Randomizer.get().nextBoolean()) {
        addValue(i, iterator.value());
        iterator.next();

        if (!iterator.hasValue()) {
          exhausted.add(i);
        }
      } else {
        // Randomly choose one of the previous values.
        values.put(i, Randomizer.get().choose(caches.get(i)));
      }
    }

    return true;
  }

  /**
   * Adds the iterator value into the cache.
   *
   * @param i the iterator index.
   * @param value the value to be added into the cache.
   */
  private void addValue(int i, final T value) {
    final ArrayList<T> trace = caches.containsKey(i) ? caches.get(i) : new ArrayList<T>();
    trace.add(value);

    caches.put(i, trace);
    values.put(i, value);
  }
}
