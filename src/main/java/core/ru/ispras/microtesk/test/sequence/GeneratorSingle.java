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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import ru.ispras.microtesk.basis.iterator.Iterator;

public final class GeneratorSingle<T> implements Generator<T> {
  private final List<T> sequence;
  private boolean hasValue;

  public GeneratorSingle(List<Iterator<List<T>>> iterators) {
    checkNotNull(iterators);

    this.sequence = createSingleSequence(iterators);
    this.hasValue = false;
  }

  private static <T> List<T> createSingleSequence(List<Iterator<List<T>>> iterators) {
    if (iterators.isEmpty()) {
      return null;
    }

    final List<T> result = new ArrayList<T>();

    for (final Iterator<List<T>> sequenceIterator : iterators) {
      sequenceIterator.init();
      while (sequenceIterator.hasValue()) {
        result.addAll(sequenceIterator.value());
        sequenceIterator.next();
      }
    }

    return result;
  }

  @Override
  public void init() {
    hasValue = (sequence != null);
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  @Override
  public List<T> value() {
    if (!hasValue) {
      throw new NoSuchElementException();
    }

    return sequence;
  }

  @Override
  public void next() {
    hasValue = false;
  }
}
