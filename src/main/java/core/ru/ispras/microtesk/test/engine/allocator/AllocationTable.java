/*
 * Copyright 2007-2019 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine.allocator;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
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
  /** The object supplier (alternative to {@code objects}). */
  private final Supplier<T> supplier;

  /** The set of used objects. */
  private final EnumMap<ResourceOperation, Collection<T>> used;

  /** The set of values for some of the objects in use. */
  private final Map<T, V> init;

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
    // Parameter attributes can be null.
    InvariantChecks.checkNotEmpty(objects);

    this.allocator = new Allocator(strategy, attributes);
    this.objects = new LinkedHashSet<>(objects);
    this.supplier = null;
    this.used = new EnumMap<>(ResourceOperation.class);
    for (final ResourceOperation operation : ResourceOperation.values()) {
      this.used.put(operation, new LinkedHashSet<T>());
    }
    this.init = new LinkedHashMap<>();
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
    // Parameter attributes can be null.
    InvariantChecks.checkNotNull(supplier);

    this.allocator = new Allocator(strategy, attributes);
    this.objects = null;
    this.supplier = supplier;
    this.used = new EnumMap<>(ResourceOperation.class);
    for (final ResourceOperation operation : ResourceOperation.values()) {
      this.used.put(operation, new LinkedHashSet<T>());
    }
    this.init = new LinkedHashMap<>();
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
   */
  public void setAllocator(final Allocator allocator) {
    InvariantChecks.checkNotNull(allocator);
    this.allocator = allocator;
  }

  /**
   * Resets the resource allocation table.
   */
  public void reset() {
    for (final Map.Entry<ResourceOperation, Collection<T>> entry : used.entrySet()) {
      entry.getValue().clear();
    }

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
   */
  public boolean isFree(final T object) {
    checkObject(object);

    for (final Map.Entry<ResourceOperation, Collection<T>> entry : used.entrySet()) {
      if (entry.getKey() != ResourceOperation.NOP && entry.getValue().contains(object)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Checks whether the object is defined (initialized).
   *
   * @param object the object to be checked.
   * @return {@code true} if the object is defined; {@code false} otherwise.
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
   */
  public V getValue(final T object) {
    checkObject(object);

    return init.get(object);
  }

  /**
   * Frees (deallocates) the object.
   *
   * @param object the object to be freed.
   */
  public void free(final T object) {
    checkObject(object);

    used.remove(object);
    init.remove(object);
  }

  /**
   * Marks the object as being in use.
   *
   * @param operation the operation on the object.
   * @param object the object to be used.
   */
  public void use(final ResourceOperation operation, final T object) {
    checkObject(object);

    if (operation != ResourceOperation.NOP) {
      used.get(operation).add(object);
    }
  }

  /**
   * Defines (initializes) the object.
   *
   * @param object the object to defined.
   * @param value the object value.
   */
  public void define(final T object, final V value) {
    checkObject(object);
    InvariantChecks.checkNotNull(value);

    use(ResourceOperation.WRITE, object);

    init.put(object, value);
  }

  /**
   * Peeks an object.
   *
   * @param exclude the objects that should not be peeked.
   * @param rate the dependencies biases.
   * @return the peeked object.
   */
  public T peek(final Set<T> exclude, final EnumMap<ResourceOperation, Integer> rate) {
    return peek(exclude, Collections.<T>emptySet(), rate);
  }

  /**
   * Peeks an object.
   *
   * @param exclude the objects that should not be peeked.
   * @param retain the objects that should be used for allocation.
   * @param rate the dependencies biases.
   * @return the peeked object.
   */
  public T peek(
      final Set<T> exclude,
      final Set<T> retain,
      final EnumMap<ResourceOperation, Integer> rate) {
    InvariantChecks.checkNotNull(exclude);
    InvariantChecks.checkNotNull(retain);

    final T object;
    if (retain.isEmpty()) {
      object = objects != null
          ? allocator.next(objects, exclude, used, rate)
          : allocator.next(supplier, exclude, used, rate);
    } else {
      object = allocator.next(retain, exclude, used, rate);
    }

    InvariantChecks.checkNotNull(
        object, String.format("Cannot peek an object: used=%s, excluded=%s", used, exclude));

    return object;
  }

  /**
   * Allocates an object and marks it as being in use.
   *
   * @param operation the operation.
   * @param exclude the objects that should not be allocated.
   * @param rate the dependencies biases.
   * @return the allocated object.
   */
  public T allocate(
      final ResourceOperation operation,
      final Set<T> exclude,
      final EnumMap<ResourceOperation, Integer> rate) {
    final T object = peek(exclude, rate);

    use(operation, object);
    return object;
  }

  /**
   * Allocates an object and marks it as being in use.
   *
   * @param operation the operation.
   * @param exclude the objects that should not be allocated.
   * @param retain the objects that should be used for allocation.
   * @param rate the dependencies biases.
   * @return the allocated object.
   */
  public T allocate(
      final ResourceOperation operation,
      final Set<T> exclude,
      final Set<T> retain,
      final EnumMap<ResourceOperation, Integer> rate) {
    final T object = peek(exclude, retain, rate);

    use(operation, object);
    return object;
  }

  /**
   * Allocates an object and defines it.
   *
   * @param value the object value.
   * @param rate the dependencies biases.
   * @return the allocated object.
   */
  public T allocateAndDefine(final V value, final EnumMap<ResourceOperation, Integer> rate) {
    InvariantChecks.checkNotNull(value);

    final T object = allocate(ResourceOperation.WRITE, Collections.<T>emptySet(), rate);
    define(object, value);

    return object;
  }

  /**
   * Allocates an object and defines it.
   *
   * @param exclude the objects that should not be allocated.
   * @param value the object value.
   * @param rate the dependencies biases.
   * @return the allocated object.
   */
  public T allocateAndDefine(
      final Set<T> exclude,
      final V value,
      final EnumMap<ResourceOperation, Integer> rate) {
    InvariantChecks.checkNotNull(value);

    final T object = allocate(ResourceOperation.WRITE, exclude, rate);
    define(object, value);

    return object;
  }

  /**
   * Returns the set of used objects.
   *
   * @return the set of used objects.
   */
  public EnumMap<ResourceOperation, Collection<T>> getUsedObjects() {
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
