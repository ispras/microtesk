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

import java.util.NoSuchElementException;
import ru.ispras.testbase.knowledge.iterator.BoundedIterator;

public final class EmptyIterator<T> implements BoundedIterator<T> {
  private static EmptyIterator<?> instance = null;

  @SuppressWarnings("unchecked")
  public static <T> EmptyIterator<T> get() {
    if (null == instance) {
      instance = new EmptyIterator<>();
    }

    return (EmptyIterator<T>) instance;
  }

  private EmptyIterator() { }

  @Override
  public void init() {
    //Empty
  }

  @Override
  public boolean hasValue() {
    return false;
  }

  @Override
  public T value() {
    throw new NoSuchElementException();
  }

  @Override
  public void next() {
    throw new NoSuchElementException();
  }

  @Override
  public void stop() {
    //Empty
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public BoundedIterator<T> clone() {
    return this;
  }
}
