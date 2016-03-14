/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

import java.util.List;
import java.util.NoSuchElementException;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.testbase.knowledge.iterator.Iterator;

public final class GeneratorIterate<T> implements Generator<T> {
  private final List<Iterator<List<T>>> iterators;

  private java.util.Iterator<Iterator<List<T>>> listIterator;
  private Iterator<List<T>> valueIterator;

  private boolean hasValue;

  public GeneratorIterate(final List<Iterator<List<T>>> iterators) {
    InvariantChecks.checkNotNull(iterators);

    this.iterators = iterators;
    init();
  }

  @Override
  public void init() {
    listIterator = iterators.iterator();
    nextValueIterator();
  }

  private void nextValueIterator() {
    if (listIterator.hasNext()) {
      valueIterator = listIterator.next();
      valueIterator.init();
      hasValue = valueIterator.hasValue();
    } else {
      valueIterator = null;
      hasValue = false;
    }
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

    return valueIterator.value();
  }

  @Override
  public void next() {
    if (!hasValue) {
      return;
    }

    valueIterator.next();
    if (valueIterator.hasValue()) {
      hasValue = true;
    } else {
      nextValueIterator();
    }
  }

  @Override
  public void stop() {
    hasValue = false;
  }

  @Override
  public GeneratorSingle<T> clone() {
    throw new UnsupportedOperationException();
  }
}
