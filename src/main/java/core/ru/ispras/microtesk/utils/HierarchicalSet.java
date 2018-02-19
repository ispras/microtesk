/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * {@link HierarchicalSet} implements a set composed of two ones.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 *
 * @param <T> Set item type.
 */
public class HierarchicalSet<T> implements Set<T> {
  private final HierarchicalCollection<T> collection;

  public HierarchicalSet(
      final HierarchicalCollection.Kind kind,
      final Set<T> upper,
      final Set<T> local) {
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(upper);
    InvariantChecks.checkNotNull(local);

    this.collection = new HierarchicalCollection<T>(kind, upper, local);
  }

  public HierarchicalSet(final Set<T> upper, final Set<T> local) {
    this(HierarchicalCollection.Kind.READ_ONLY, upper, local);
  }

  public HierarchicalCollection.Kind getKind() {
    return collection.getKind();
  }

  public Set<T> getUpper() {
    return (Set<T>) collection.getUpper();
  }

  public Set<T> getLocal() {
    return (Set<T>) collection.getLocal();
  }

  @Override
  public boolean isEmpty() {
    return collection.isEmpty();
  }

  @Override
  public int size() {
    return collection.size();
  }

  @Override
  public boolean contains(final Object o) {
    return collection.contains(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return collection.containsAll(c);
  }

  @Override
  public boolean add(final T e) {
    return collection.add(e);
  }

  @Override
  public boolean addAll(final Collection<? extends T> c) {
    return collection.addAll(c);
  }

  @Override
  public Iterator<T> iterator() {
    return collection.iterator();
  }

  @Override
  public Object[] toArray() {
    return collection.toArray();
  }

  @Override
  public <V> V[] toArray(final V[] a) {
    return collection.toArray(a);
  }

  @Override
  public boolean remove(final Object o) {
    return collection.remove(o);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    return collection.removeAll(c);
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    return collection.retainAll(c);
  }

  @Override
  public void clear() {
    collection.clear();
  }

  @Override
  public int hashCode() {
    return collection.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof HierarchicalSet<?>)) {
      return false;
    }

    final HierarchicalSet<?> other = (HierarchicalSet<?>) o;
    return collection.equals(other.collection);
  }

  @Override
  public String toString() {
    return collection.toString();
  }
}
