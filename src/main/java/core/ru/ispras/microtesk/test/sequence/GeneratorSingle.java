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

package ru.ispras.microtesk.test.sequence;

import java.util.Iterator;
import java.util.List;
import ru.ispras.microtesk.test.sequence.iterator.IIterator;

public final class GeneratorSingle<T> implements Generator<T> {
  private final Sequence<T> sequence;
  private boolean hasValue;

  public GeneratorSingle(List<IIterator<Sequence<T>>> iterators) {
    if (null == iterators) {
      throw new NullPointerException(); 
    }

    this.sequence = createSingleSequence(iterators);
    this.hasValue = false;
  }

  private static <T> Sequence<T> createSingleSequence(List<IIterator<Sequence<T>>> iterators) {
    final Sequence<T> result = new Sequence<T>();
    final Iterator<IIterator<Sequence<T>>> it = iterators.iterator();

    while (it.hasNext()) {
      final IIterator<Sequence<T>> sequenceIterator = it.next();
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
    hasValue = true;
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  @Override
  public Sequence<T> value() {
    assert hasValue;
    return sequence;
  }

  @Override
  public void next() {
    hasValue = false;
  }
}
