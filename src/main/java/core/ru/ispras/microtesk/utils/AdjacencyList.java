/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.utils;

import ru.ispras.fortress.util.InvariantChecks;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The {@link AdjacencyList} class is an implementation of list that facilitates
 * dealing with adjacent items.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <T> List item type.
 */
public final class AdjacencyList<T> implements Iterable<T> {
  private static final class Entry<T> {
    private final T value;
    private Entry<T> previous;
    private Entry<T> next;

    private Entry(final T value, final Entry<T> previous, final Entry<T> next) {
      this.value = value;
      this.previous = previous;
      this.next = next;
    }
  }

  private static final class ValueIterator<T> implements Iterator<T> {
    private Entry<T> current;

    private ValueIterator(final Entry<T> current) {
      this.current = current;
    }

    @Override
    public boolean hasNext() {
      return null != current;
    }

    @Override
    public T next() {
      InvariantChecks.checkNotNull(current);
      final T result = current.value;
      current = current.next;
      return result;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private final Map<T, Entry<T>> entries;
  private Entry<T> head;
  private Entry<T> tail;

  public AdjacencyList() {
    this.entries = new IdentityHashMap<>();
    this.head = null;
    this.tail = null;
  }

  @Override
  public Iterator<T> iterator() {
    return new ValueIterator<>(head);
  }

  public boolean isEmpty() {
    return entries.isEmpty();
  }

  public boolean contains(final T obj) {
    return entries.containsKey(obj);
  }

  public void clear() {
    entries.clear();
    head = null;
    tail = null;
  }

  public T getFirst() {
    return null != head ? head.value : null;
  }

  public T getLast() {
    return null != tail ? tail.value : null;
  }

  public T getPrevious(final T obj) {
    InvariantChecks.checkNotNull(obj);
    final Entry<T> entry = entries.get(obj);
    InvariantChecks.checkNotNull(entry);

    return null != entry.previous ? entry.previous.value : null;
  }

  public T getNext(final T obj) {
    InvariantChecks.checkNotNull(obj);
    final Entry<T> entry = entries.get(obj);
    InvariantChecks.checkNotNull(entry);

    return null != entry.next ? entry.next.value : null;
  }

  public void add(final T obj) {
    InvariantChecks.checkNotNull(obj);

    final Entry<T> entry = new Entry<>(obj, tail, null);
    entries.put(obj, entry);

    if (null != tail) {
      tail.next = entry;
    }

    if (null == head) {
      head = entry;
    }

    tail = entry;
  }

  public void addAfter(final T previous, final T current) {
    InvariantChecks.checkNotNull(previous);
    final Entry<T> previousEntry = entries.get(previous);
    InvariantChecks.checkNotNull(previousEntry);

    final Entry<T> entry = new Entry<>(current, previousEntry, previousEntry.next);
    entries.put(current, entry);

    if (null != previousEntry.next) {
      previousEntry.next.previous = entry;
    } else {
      tail = entry;
    }

    previousEntry.next = entry;
  }

  public void replaceWith(final T previous, final T current) {
    InvariantChecks.checkNotNull(previous);
    final Entry<T> previousEntry = entries.remove(previous);
    InvariantChecks.checkNotNull(previousEntry);

    final Entry<T> entry = new Entry<>(current, previousEntry.previous, previousEntry.next);
    entries.put(current, entry);

    if (null != previousEntry.previous) {
      previousEntry.previous.next = entry;
    } else {
      head = entry;
    }

    if (null != previousEntry.next) {
      previousEntry.next.previous = entry;
    } else {
      tail = entry;
    }
  }

  public void remove(final T obj) {
    InvariantChecks.checkNotNull(obj);

    final Entry<T> entry = entries.remove(obj);
    if (null == entry) {
      return;
    }

    if (entry == head) {
      head = entry.next;
    }

    if (entry == tail) {
      tail = entry.previous;
    }

    if (null != entry.previous) {
      entry.previous.next = entry.next;
    }

    if (null != entry.next) {
      entry.next.previous = entry.previous;
    }
  }
}
