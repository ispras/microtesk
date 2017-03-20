/*
 * Copyright 2007-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.engine.allocator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.function.Supplier;

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
  /** The object that performs allocation. */
  private Allocator allocator;

  /** The set of all available objects. */
  private final Set<T> objects;
  /** The object generator (alternative to {@code objects}). */
  private final Supplier<T> supplier;

  /** The set of used objects. */
  private final Set<T> used = new LinkedHashSet<>();
  /** The set of values for some of the objects in use. */
  private final Map<T, V> init = new LinkedHashMap<>();

  /**
   * Constructs a resource allocation table.
   * 
   * @param strategy the allocation strategy.
   * @param attributes the strategy parameters or {@code null}.
   * @param objects the collection of available objects.
   */
  public AllocationTable(
      final AllocationStrategy strategy,
      final Map<String, String> attributes,
      final Collection<T> objects) {
    InvariantChecks.checkNotNull(strategy);
    InvariantChecks.checkNotNull(attributes);
    InvariantChecks.checkNotEmpty(objects);

    this.allocator = new Allocator(strategy, attributes);
    this.objects = new LinkedHashSet<>(objects);
    this.supplier = null;
  }

  /**
   * Constructs a resource allocation table.
   * 
   * @param strategy the allocation strategy.
   * @param objects the collection of available objects.
   */
  public AllocationTable(final AllocationStrategy strategy, final Collection<T> objects) {
    this(strategy, null, objects);
  }

  /**
   * Constructs a resource allocation table.
   * 
   * @param strategy the allocation strategy.
   * @param attributes the strategy parameters or {@code null}.
   * @param supplier the object generator.
   */
  public AllocationTable(
      final AllocationStrategy strategy,
      final Map<String, String> attributes,
      final Supplier<T> supplier) {
    InvariantChecks.checkNotNull(strategy);
    InvariantChecks.checkNotNull(attributes);
    InvariantChecks.checkNotNull(supplier);

    this.allocator = new Allocator(strategy, attributes);
    this.objects = null;
    this.supplier = supplier;
  }

  /**
   * Constructs a resource allocation table.
   * 
   * @param strategy the allocation strategy.
   * @param supplier the object generator.
   */
  public AllocationTable(final AllocationStrategy strategy, final Supplier<T> supplier) {
    this(strategy, null, supplier);
  }

  /**
   * Returns the currently used allocator.
   * 
   * @return Current allocator.
   */
  public Allocator getAllocator() {
    return allocator;
  }

  /**
   * Replaces the current allocator with a new one.
   * 
   * @param allocator New allocator.
   * 
   * @throws IllegalArgumentException if the argument is {@code null}.
   */
  public void setAllocator(final Allocator allocator) {
    InvariantChecks.checkNotNull(allocator);
    this.allocator = allocator;
  }

  /**
   * Resets the resource allocation table.
   */
  public void reset() {
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
   * Checks whether the object exists in the allocation table.
   * 
   * @param object the object to be checked.
   * @return {@code true} if the object exists; {@code false} otherwise.
   * @throws IllegalArgumentException if {@code object} does not exist (i.e. it is unknown).
   */
  public boolean exists(final T object) {
    InvariantChecks.checkNotNull(object);
    return objects.contains(object);
  }

  /**
   * Checks whether the object is free (belongs to the initial set of objects and not in use).
   *  
   * @param object the object to be checked.
   * @return {@code true} if the object is free; {@code false} otherwise.
   * @throws IllegalArgumentException if {@code object} is null.
   * @throws IllegalArgumentException if {@code object} is unknown.
   */
  public boolean isFree(final T object) {
    checkObject(object);

    return !used.contains(object);
  }

  /**
   * Checks whether the object is in use.
   * 
   * @param object the object to be checked.
   * @return {@code true} if the object is in use; {@code false} otherwise.
   * @throws IllegalArgumentException if {@code object} is null.
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
   * @throws IllegalArgumentException if {@code object} is null.
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
   * @throws IllegalArgumentException if {@code object} is null.
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
   * @throws IllegalArgumentException if {@code object} is null.
   * @throws IllegalArgumentException if {@code object} is unknown.
   */
  public void free(final T object) {
    checkObject(object);

    used.remove(object);
    init.remove(object);
  }

  /**
   * Marks the object as being in use.
   * 
   * @param object the object to be used.
   * @throws IllegalArgumentException if {@code object} is null.
   * @throws IllegalArgumentException if {@code object} is unknown.
   */
  public void use(final T object) {
    checkObject(object);

    used.add(object);
  }

  /**
   * Defines (initializes) the object.
   * 
   * @param object the object to defined.
   * @param value the object value.
   * @throws IllegalArgumentException if {@code object} is null or {@code value} is null.
   * @throws IllegalArgumentException if {@code object} is unknown.
   */
  public void define(final T object, final V value) {
    checkObject(object);
    InvariantChecks.checkNotNull(value);

    use(object);

    init.put(object, value);
  }

  /**
   * Peeks a free object.
   * 
   * @return the peeked object.
   * @throws IllegalStateException if an object cannot be peeked.
   */
  public T peek() {
    return peek(Collections.<T>emptySet());
  }

  /**
   * Peeks a free object.
   *
   * @param exclude the objects that should not be peeked.
   * @return the peeked object.
   * @throws IllegalArgumentException if {@code exclude} is null.
   * @throws IllegalStateException if an object cannot be peeked.
   */
  public T peek(final Set<T> exclude) {
    InvariantChecks.checkNotNull(exclude);

    final T object = allocator.next(objects, exclude, used);
    InvariantChecks.checkNotNull(object, String.format("Cannot peek an object: used=%s", used));

    return object;
  }

  /**
   * Allocates an object (peeks an object and marks it as being in use).
   * 
   * @return the allocated object.
   * @throws IllegalStateException if an object cannot be allocated.
   */
  public T allocate() {
    final T object = peek();

    use(object);
    return object;
  }

  /**
   * Allocates an object and marks it as being in use.
   *
   * @param exclude the objects that should not be allocated.
   * @return the allocated object.
   * @throws IllegalArgumentException if {@code exclude} is null.
   * @throws IllegalStateException if an object cannot be allocated.
   */
  public T allocate(final Set<T> exclude) {
    final T object = peek(exclude);

    use(object);
    return object;
  }

  /**
   * Allocates an object and defines it.
   * 
   * @param value the object value.
   * @return the allocated object.
   * @throws IllegalArgumentException if {@code value} is null.
   * @throws IllegalStateException if an object cannot be allocated.
   */
  public T allocateAndDefine(final V value) {
    InvariantChecks.checkNotNull(value);

    final T object = allocate();
    define(object, value);

    return object;
  }

  /**
   * Allocates an object and defines it.
   * 
   * @param exclude the objects that should not be allocated.
   * @param value the object value.
   * @return the allocated object.
   * @throws IllegalArgumentException if {@code exclude} or {@code value} is null.
   * @throws IllegalStateException if an object cannot be allocated.
   */
  public T allocateAndDefine(final Set<T> exclude, final V value) {
    InvariantChecks.checkNotNull(value);

    final T object = allocate(exclude);
    define(object, value);

    return object;
  }

  /**
   * Returns the set of used objects.
   * 
   * @return the set of used objects.
   */
  public Set<T> getUsedObjects() {
    return used;
  }

  /**
   * Returns the set of all objects.
   * 
   * @return the set of all objects.
   */
  public Set<T> getAllObjects() {
    return objects;
  }

  /**
   * Returns the object generator.
   * 
   * @return the object generator.
   */
  public Supplier<T> getSupplier() {
    return supplier;
  }

  private void checkObject(final T object) {
    InvariantChecks.checkNotNull(object);

    if (objects != null && !objects.contains(object)) {
      throw new IllegalArgumentException(String.format("Unknown object: %s", object));
    }
  }

  @Override
  public String toString() {
    return String.format("used=%s", used);
  }
}
