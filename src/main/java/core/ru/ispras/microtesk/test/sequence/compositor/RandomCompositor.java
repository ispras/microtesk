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

package ru.ispras.microtesk.test.sequence.compositor;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.randomizer.VariateBiased;
import ru.ispras.microtesk.test.sequence.iterator.BoundedIterator;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;

/**
 * This class implements the random composition (merging) of iterators.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RandomCompositor<T> extends Compositor<T> {
  /** Random distribution for choosing iterators. */
  private Variate<Integer> distribution;

  /** The iterator indices. */
  private List<Integer> values = new ArrayList<>();
  /** The iterator biases. */
  private List<Integer> biases = new ArrayList<>();

  @Override
  protected void onInit() {
    // Check whether there are unbounded iterators.
    boolean bounded = true;

    for (final Iterator<T> iterator : iterators) {
      if (!(iterator instanceof BoundedIterator)) {
        bounded = false;
        break;
      }
    }

    for (int i = 0; i < iterators.size(); i++) {
      final Iterator<T> iterator = iterators.get(i);

      // If all of the iterators are bounded (i.e., their sequences' sizes are known),
      // the iterator choice probability is proportional to the sequence size.
      // If there are unbounded iterators (i.e., iterators with unknown size),
      // the uniform probability distribution is used for choosing iterators.
      values.add(i);
      biases.add(bounded ? ((BoundedIterator<T>) iterator).size() : 1);
    }

    distribution = new VariateBiased<>(values, biases);
  }

  @Override
  protected void onNext() {
    // Do nothing.
  }

  @Override
  protected Iterator<T> choose() {
    // If there are non-exhausted iterators, choose one of them.
    while (!values.isEmpty()) {
      final int i = distribution.value();

      if (iterators.get(i).hasValue()) {
        return iterators.get(i);
      }

      // If the iterator has been exhausted, remove it from the set.
      final int j = values.indexOf(i);

      values.remove(j);
      biases.remove(j);

      if (!values.isEmpty()) {
        distribution = new VariateBiased<Integer>(values, biases);
      }
    }

    return null;
  }
}
