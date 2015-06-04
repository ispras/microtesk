/*
 * Copyright 2007-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link AllocationTable} implements a resource allocation table, which is a finite set of objects
 * (registers, pages, etc.) in couple with allocation / deallocation methods.
 * 
 * @param <T> type of objects.
 * @param <V> type of object values.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AllocationTable<T, V> {
  /** The strategy for object allocation. */
  private AllocationStrategy strategy;

  /** The set of all available objects. */
  private Set<T> objects;

  /** The set of free objects. */
  private Set<T> free;

  /** The set of used objects. */
  private Set<T> used = new LinkedHashSet<>();
  /** The set of values for some of the objects in use. */
  private Map<T, V> init = new LinkedHashMap<>();

  /**
   * Constructs a resource allocation table.
   * 
   * @param strategy the allocation strategy.
   * @param objects the collection of available objects.
   */
  public AllocationTable(final AllocationStrategy strategy, final Collection<T> objects) {
    this.strategy = strategy;
    this.objects = new LinkedHashSet<>(objects);
    reset();
  }

  /**
   * Constructs a resource allocation table with the default allocation strategy
   * ({@code GET_FREE_OBJECT}).
   * 
   * @param objects the collection of available objects.
   */
  public AllocationTable(final Collection<T> objects) {
    this(AllocationStrategy.GET_FREE_OBJECT, objects);
  }

  /**
   * Resets the resource allocation table.
   */
  public void reset() {
    free = new HashSet<>(objects);

    used.clear();
    init.clear();
  }

  /**
   * Returns the number of available objects (both free and used).
   * 
   * @return the number of objects.
   */
  public int size() {
    return objects.size();
  }

  /**
   * Returns the number of free objects.
   * 
   * @return the number of free objects.
   */
  public int countFreeObjects() {
    return free.size();
  }

  /**
   * Returns the number of used objects.
   * 
   * @return the number of used objects.
   */
  public int countUsedObjects() {
    return used.size();
  }

  /**
   * Returns the number of defined (initialized) objects.
   * 
   * @return the number of defined objects.
   */
  public int countDefinedObjects() {
    return init.size();
  }

  /**
   * Checks whether the object is free (belongs to the initial set of objects and not in use).
   *  
   * @param object the object to be checked.
   * @return {@code true} if the object is free; {@code false} otherwise.
   * @throws NullPointerException if {@code object} is null.
   * @throws IllegalArgumentException if {@code object} is unknown.
   */
  public boolean isFree(final T object) {
    checkObject(object);

    return free.contains(object);
  }

  /**
   * Checks whether the object is in use.
   * 
   * @param object the object to be checked.
   * @return {@code true} if the object is in use; {@code false} otherwise.
   * @throws NullPointerException if {@code object} is null.
   * @throws IllegalArgumentException if {@code object} is unknown.
   */
  public boolean isUsed(final T object) {
    checkObject(object);

    return used.contains(object);
  }

  /**
   * Checks whether the object is defined (initialized).
   * 
   * @param object the object to be checked.
   * @return {@code true} if the object is defined; {@code false} otherwise.
   * @throws NullPointerException if {@code object} is null.
   * @throws IllegalArgumentException if {@code object} is unknown.
   */
  public boolean isDefined(final T object) {
    checkObject(object);

    return init.containsKey(object);
  }

  /**
   * Returns the object value (if is defined) or {@code null} (otherwise).
   * 
   * @param object the object whose value to be returned.
   * @return the object value if is defined; {@code null} otherwise.
   * @throws NullPointerException if {@code object} is null.
   * @throws IllegalArgumentException if {@code object} is unknown.
   */
  public V getValue(final T object) {
    checkObject(object);

    return init.get(object);
  }

  /**
   * Frees (deallocates) the object.
   * 
   * @param object the object to be freed.
   * @throws NullPointerException if {@code object} is null.
   * @throws IllegalArgumentException if {@code object} is unknown.
   */
  public void free(final T object) {
    checkObject(object);

    used.remove(object);
    init.remove(object);
    free.add(object);
  }

  /**
   * Marks the object as being in use.
   * 
   * @param object the object to be used.
   * @throws NullPointerException if {@code object} is null.
   * @throws IllegalArgumentException if {@code object} is unknown.
   */
  public void use(final T object) {
    checkObject(object);

    free.remove(object);
    used.add(object);
  }

  /**
   * Defines (initializes) the object.
   * 
   * @param object the object to defined.
   * @param value the object value.
   * @throws NullPointerException if {@code object} is null or {@code value} is null.
   * @throws IllegalArgumentException if {@code object} is unknown.
   */
  public void define(final T object, final V value) {
    checkObject(object);
    InvariantChecks.checkNotNull(value);

    use(object);

    init.put(object, value);
  }

  /**
   * Peeks a free object using the given strategy.
   * 
   * @param strategy the object allocation strategy.
   * @return the peeked object.
   * @throws NullPointerException if {@code strategy} is null.
   * @throws IllegalStateException if an object cannot be peeked.
   */
  public T peek(final AllocationStrategy strategy) {
    InvariantChecks.checkNotNull(strategy);

    final T object = strategy.next(free, used);

    if (object == null) {
      throw new IllegalStateException("Cannot peek an object");
    }

    return object;
  }

  /**
   * Peeks a free object.
   * 
   * @return the peeked object.
   * @throws IllegalStateException if an object cannot be peeked.
   */
  public T peek() {
    return peek(strategy);
  }

  /**
   * Peeks a free object.
   *
   * @param strategy the object allocation strategy.
   * @param exclude the objects that should not be peeked.
   * @return the peeked object.
   * @throws NullPointerException if {@code strategy} or {@code exclude} is null.
   * @throws IllegalStateException if an object cannot be peeked.
   */
  public T peek(final AllocationStrategy strategy, final Set<T> exclude) {
    InvariantChecks.checkNotNull(strategy);
    InvariantChecks.checkNotNull(exclude);

    final Set<T> domain = new HashSet<>(free);
    domain.removeAll(exclude);

    final T object = strategy.next(domain, used);

    if (object == null) {
      throw new IllegalStateException("Cannot peek an object");
    }

    return object;
  }

  /**
   * Peeks a free object.
   *
   * @param exclude the objects that should not be peeked.
   * @return the peeked object.
   * @throws NullPointerException if {@code exclude} is null.
   * @throws IllegalStateException if an object cannot be peeked.
   */
  public T peek(final Set<T> exclude) {
    return peek(strategy, exclude);
  }

  /**
   * Allocates an object (peeks an object and marks it as being in use) using the given strategy.
   * 
   * @param strategy the object allocation strategy.
   * @return the allocated object.
   * @throws NullPointerException if {@code strategy} is null.
   * @throws IllegalStateException if an object cannot be allocated.
   */
  public T allocate(final AllocationStrategy strategy) {
    final T object = peek(strategy);

    use(object);
    return object;
  }

  /**
   * Allocates an object (peeks an object and marks it as being in use).
   * 
   * @return the allocated object.
   * @throws IllegalStateException if an object cannot be allocated.
   */
  public T allocate() {
    return allocate(strategy);
  }

  /**
   * Allocates an object (peeks an object and marks it as being in use) using the given strategy.
   *
   * @param strategy the object allocation strategy.
   * @param exclude the objects that should not be allocated.
   * @return the allocated object.
   * @throws NullPointerException if {@code exclude} or {@code strategy} is null.
   * @throws IllegalStateException if an object cannot be allocated.
   */
  public T allocate(final AllocationStrategy strategy, final Set<T> exclude) {
    final T object = peek(strategy, exclude);

    use(object);
    return object;
  }

  /**
   * Allocates an object and marks it as being in use.
   *
   * @param exclude the objects that should not be allocated.
   * @return the allocated object.
   * @throws NullPointerException if {@code exclude} is null.
   * @throws IllegalStateException if an object cannot be allocated.
   */
  public T allocate(final Set<T> exclude) {
    return allocate(strategy, exclude);
  }

  /**
   * Allocates an object using the given strategy and defines it.
   * 
   * @param strategy the object allocation strategy.
   * @param value the object value.
   * @return the allocated object.
   * @throws NullPointerException if {@code strategy} or {@code value} is null.
   * @throws IllegalStateException if an object cannot be allocated.
   */
  public T allocateAndDefine(final AllocationStrategy strategy, final V value) {
    InvariantChecks.checkNotNull(value);

    final T object = allocate(strategy);
    define(object, value);

    return object;
  }

  /**
   * Allocates an object and defines it.
   * 
   * @param value the object value.
   * @return the allocated object.
   * @throws NullPointerException if {@code value} is null.
   * @throws IllegalStateException if an object cannot be allocated.
   */
  public T allocateAndDefine(final V value) {
    return allocateAndDefine(strategy, value);
  }

  /**
   * Allocates an object using the given strategy and defines it.
   * 
   * @param strategy the object allocation strategy.
   * @param exclude the objects that should not be allocated.
   * @param value the object value.
   * @return the allocated object.
   * @throws NullPointerException if {@code strategy}, {@code exclude} or {@code value} is null.
   * @throws IllegalStateException if an object cannot be allocated.
   */
  public T allocateAndDefine(final AllocationStrategy strategy, final Set<T> exclude, final V value) {
    InvariantChecks.checkNotNull(value);

    final T object = allocate(strategy, exclude);
    define(object, value);

    return object;
  }

  /**
   * Allocates an object and defines it.
   * 
   * @param exclude the objects that should not be allocated.
   * @param value the object value.
   * @return the allocated object.
   * @throws NullPointerException if {@code exclude} or {@code value} is null.
   * @throws IllegalStateException if an object cannot be allocated.
   */
  public T allocateAndDefine(final Set<T> exclude, final V value) {
    return allocateAndDefine(strategy, exclude, value);
  }

  /**
   * Returns the set of free objects.
   * 
   * @return the set of free objects.
   */
  public Set<T> getFreeObjects() {
    return free;
  }

  /**
   * Returns the set of used objects.
   * 
   * @return the set of used objects.
   */
  public Set<T> getUsedObjects() {
    return used;
  }

  private void checkObject(final T object) {
    InvariantChecks.checkNotNull(object);

    if (!objects.contains(object)) {
      throw new IllegalArgumentException(String.format("Unknown object: %s", object));
    }
  }

  @Override
  public String toString() {
    return free.toString();
  }
}
