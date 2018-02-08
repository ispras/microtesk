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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * {@link HierarchicalMap} implements a map composed of two ones.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 *
 * @param <K> Map key type.
 * @param <V> Map key type.
 */
public class HierarchicalMap<K, V> implements Map<K, V> {

  private final HierarchicalCollection.Kind kind;
  private final Map<K, V> upper;
  private final Map<K, V> local;

  public HierarchicalMap(
      final HierarchicalCollection.Kind kind,
      final Map<K, V> upper,
      final Map<K, V> local) {
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(upper);
    InvariantChecks.checkNotNull(local);

    final boolean isReadOnly = (kind == HierarchicalCollection.Kind.READ_ONLY);

    this.kind = kind;
    this.upper =  isReadOnly ? Collections.<K, V>unmodifiableMap(upper) : upper;
    this.local = local;
  }

  public HierarchicalMap(final Map<K, V> upper, final Map<K, V> local) {
    this(HierarchicalCollection.Kind.READ_ONLY, upper, local);
  }

  public HierarchicalCollection.Kind getKind() {
    return kind;
  }

  public Map<K, V> getUpper() {
    return upper;
  }

  public Map<K, V> getLocal() {
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
  public boolean containsKey(final Object key) {
    return local.containsKey(key) || upper.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return local.containsValue(value) || upper.containsValue(value);
  }

  @Override
  public V get(final Object key) {
    final V value = local.get(key);

    if (value != null) {
      return value;
    }

    return upper.get(key);
  }

  @Override
  public V put(final K key, final V value) {
    return local.put(key, value);
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> map) {
    local.putAll(map);
  }

  @Override
  public V remove(final Object key) {
    final V value = local.remove(key);

    if (value != null) {
      return value;
    }

    return upper.remove(key);
  }

  @Override
  public void clear() {
    local.clear();
    upper.clear();
  }

  @Override
  public Set<K> keySet() {
    return new HierarchicalSet<K>(kind, upper.keySet(), local.keySet());
  }

  @Override
  public Collection<V> values() {
    return new HierarchicalCollection<V>(kind, upper.values(), local.values());
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return new HierarchicalSet<Entry<K, V>>(kind, upper.entrySet(), local.entrySet());
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

    if (!(other instanceof HierarchicalMap<?, ?>)) {
      return false;
    }

    final HierarchicalMap<?, ?> map = (HierarchicalMap<?, ?>) other;

    return kind.equals(map.kind)
        && upper.equals(map.upper)
        && local.equals(map.local);
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append(upper);
    builder.append(local);

    return builder.toString();
  }
}
