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

package ru.ispras.microtesk.test.sequence;

import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.List;
import java.util.NoSuchElementException;

public final class GeneratorSequence<T> implements Generator<T> {
  private final List<T> sequence;
  private boolean hasValue;

  public GeneratorSequence(final List<Iterator<List<T>>> iterators) {
    this.sequence = GeneratorUtils.expandAll(iterators);
    this.hasValue = false;
  }

  private GeneratorSequence(final GeneratorSequence<T> other) {
    this.sequence = other.sequence;
    this.hasValue = other.hasValue;
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

  @Override
  public void stop() {
    hasValue = false;
  }

  @Override
  public GeneratorSequence<T> clone() {
    return new GeneratorSequence<>(this);
  }
}
