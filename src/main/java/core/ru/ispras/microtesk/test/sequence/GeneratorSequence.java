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

import ru.ispras.testbase.knowledge.iterator.CollectionIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

public final class GeneratorSequence<T> implements Generator<T> {

  private final CollectionIterator<List<T>> collectionIterator; 

  public GeneratorSequence(List<Iterator<List<T>>> iterators) {
    if (null == iterators) {
      throw new NullPointerException(); 
    }

    final List<List<T>> sequences = new ArrayList<List<T>>();
    for (Iterator<List<T>> sequenceIterator : iterators) {
      final List<T> sequence = createSingleSequence(sequenceIterator);
      sequences.add(sequence);
    }

    this.collectionIterator = new CollectionIterator<List<T>>(sequences);
  }

  private static <T> List<T> createSingleSequence(Iterator<List<T>> sequenceIterator) {
    final List<T> result = new ArrayList<T>();

    sequenceIterator.init();
    while (sequenceIterator.hasValue()) {
      result.addAll(sequenceIterator.value());
      sequenceIterator.next();
    }

    return result;
  }

  @Override
  public void init() {
    collectionIterator.init();
  }

  @Override
  public boolean hasValue() {
    return collectionIterator.hasValue();
  }

  @Override
  public List<T> value() {
    return collectionIterator.value();
  }

  @Override
  public void next() {
    collectionIterator.next();
  }

  @Override
  public void stop() {
    collectionIterator.stop();
  }

  @Override
  public GeneratorSequence<T> clone() {
    throw new UnsupportedOperationException();
  }
}
