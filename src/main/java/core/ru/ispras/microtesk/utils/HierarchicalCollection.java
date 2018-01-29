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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;

public class HierarchicalCollection<T> implements Collection<T> {

  public enum Kind {
    READ_ONLY,
    READ_WRITE
  }

  private final Kind kind;
  private final Collection<T> upper;
  private final Collection<T> local;

  public HierarchicalCollection(
      final Kind kind,
      final Collection<T> upper,
      final Collection<T> local) {
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(upper);
    InvariantChecks.checkNotNull(local);

    final boolean isReadOnly = (kind == HierarchicalCollection.Kind.READ_ONLY);

    this.kind = kind;
    this.upper = isReadOnly ? Collections.<T>unmodifiableCollection(upper) : upper;
    this.local = local;
  }

  public HierarchicalCollection(final Set<T> upper, final Set<T> local) {
    this(Kind.READ_ONLY, upper, local);
  }

  public Kind getKind() {
    return kind;
  }

  public Collection<T> getUpper() {
    return upper;
  }

  public Collection<T> getLocal() {
    return local;
  }

  @Override
  public boolean isEmpty() {
    return local.isEmpty() && upper.isEmpty();
  }

  @Override
  public int size() {
    return local.size() + upper.size();
  }

  @Override
  public boolean contains(final Object o) {
    return local.contains(o) || upper.contains(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    for (final Object o : c) {
      if (!contains(o)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean add(final T e) {
    return local.add(e);
  }

  @Override
  public boolean addAll(final Collection<? extends T> c) {
    return local.addAll(c);
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      final Iterator<T> upperIterator = upper.iterator();
      final Iterator<T> localIterator = local.iterator();

      @Override
      public boolean hasNext() {
        return upperIterator.hasNext() || localIterator.hasNext();
      }

      @Override
      public T next() {
        return upperIterator.hasNext() ? upperIterator.next() : localIterator.next();
      }
    };
  }

  @Override
  public Object[] toArray() {
    final Object[] upperArray = upper.toArray();
    final Object[] localArray = local.toArray();

    final Object[] array = Arrays.copyOf(upperArray, upperArray.length + localArray.length);
    System.arraycopy(localArray, 0, array, upperArray.length, localArray.length);

    return array;
  }

  @Override
  public <V> V[] toArray(final V[] a) {
    final V[] upperArray = upper.toArray(a);
    final V[] localArray = local.toArray(a);

    final V[] array = Arrays.<V>copyOf(upperArray, upperArray.length + localArray.length);
    System.arraycopy(localArray, 0, array, upperArray.length, localArray.length);

    return array;
  }

  @Override
  public boolean remove(final Object o) {
    return local.remove(o) || upper.remove(o);
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    final boolean changed1 = local.removeAll(c);
    final boolean changed2 = upper.removeAll(c);

    return changed1 || changed2;
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    final boolean changed1 = local.retainAll(c);
    final boolean changed2 = upper.retainAll(c);

    return changed1 || changed2;
  }

  @Override
  public void clear() {
    local.clear();
    upper.clear();
  }

  @Override
  public int hashCode() {
    int hashCode = kind.hashCode();

    hashCode += 31 * upper.hashCode();
    hashCode += 31 * local.hashCode();

    return hashCode;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof HierarchicalCollection<?>)) {
      return false;
    }

    final HierarchicalCollection<?> other = (HierarchicalCollection<?>) o;

    return kind.equals(other.kind)
        && upper.equals(other.upper)
        && local.equals(other.local);
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append(upper);
    builder.append(local);

    return builder.toString();
  }
}
