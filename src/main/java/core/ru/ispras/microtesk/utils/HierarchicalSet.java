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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;

public class HierarchicalSet<T> implements Set<T> {
  private final HierarchicalMap.Kind kind;
  private final Set<T> upper;
  private final Set<T> local;

  public HierarchicalSet(final HierarchicalMap.Kind kind, final Set<T> upper, final Set<T> local) {
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(upper);
    InvariantChecks.checkNotNull(local);

    this.kind = kind;
    this.upper = kind == HierarchicalMap.Kind.READ_ONLY ? Collections.<T>unmodifiableSet(upper) : upper;
    this.local = local;
  }

  public HierarchicalSet(final Set<T> upper, final Set<T> local) {
    this(HierarchicalMap.Kind.READ_ONLY, upper, local);
  }

  public HierarchicalMap.Kind getKind() {
    return kind;
  }

  public Set<T> getUpperSet() {
    return upper;
  }

  public Set<T> getLocalSet() {
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
    // TODO:
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray() {
    // TODO:
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> V[] toArray(final V[] a) {
    // TODO:
    throw new UnsupportedOperationException();
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
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof HierarchicalSet<?>)) {
      return false;
    }

    final HierarchicalSet<?> set = (HierarchicalSet<?>) other;

    return kind.equals(set.kind)
        && upper.equals(set.upper)
        && local.equals(set.local);
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append(upper);
    builder.append(local);

    return builder.toString();
  }
}
