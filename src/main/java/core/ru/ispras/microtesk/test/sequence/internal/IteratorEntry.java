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

import ru.ispras.microtesk.test.sequence.iterator.BoundedIterator;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;

/**
 * This class represents an iterator entry that is used in some compositors.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class IteratorEntry<T> {
  /** The default point position. */
  public final static float DEFAULT_POINT_POSITION = 50.0f;

  /** The index of the current item. */
  public int index;
  /** The point where the composition (nesting, overlapping, etc.) is applied. */
  public int point;
  /** The overall number of items being iterated. */
  public int count;

  /** The flag indicating that the composition has been done. */
  public boolean done;

  /** The iterator itself. */
  public Iterator<T> iterator;

  /**
   * Constructs an iterator entry.
   * 
   * @param iterator the iterator.
   * @param position the relative position of the composition point.
   */
  public IteratorEntry(final Iterator<T> iterator, float position) {
    assert 0.0 <= position && position <= 100.0;

    if (!(iterator instanceof BoundedIterator)) {
      throw new IllegalArgumentException();
    }

    this.iterator = iterator;

    this.done = false;

    this.count = ((BoundedIterator<T>) iterator).size();
    this.point = (int) ((position * count) / 100.0f);
    this.index = 0;
  }

  /**
   * Constructs an iterator entry whose composition point is in the middle.
   * 
   * @param iterator the iterator.
   */
  public IteratorEntry(final Iterator<T> iterator) {
    this(iterator, DEFAULT_POINT_POSITION);
  }
}
