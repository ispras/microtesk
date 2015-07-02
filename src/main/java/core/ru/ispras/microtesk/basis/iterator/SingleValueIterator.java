/*
 * Copyright 2008-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.basis.iterator;

/**
 * {@link SingleValueIterator} implements a single-value iterator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class SingleValueIterator<T> implements BoundedIterator<T> {
  /** The value itself. */
  private final T value;

  /** The flag that refrects availability of the value. */
  private boolean hasValue;

  /**
   * Constructs a single-value iterator.
   * 
   * @param value the value to be returned by the iterator.
   */
  public SingleValueIterator(T value) {
    this.value = value;
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
  public T value() {
    return value;
  }

  @Override
  public void next() {
    hasValue = false;
  }

  @Override
  public int size() {
    return 1;
  }
}
