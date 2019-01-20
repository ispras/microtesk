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
  /** The default allocation data. */
  private AllocationData<T> allocationData;

  /** The set of all available objects. */
  private final Collection<T> objects;
  /** The object supplier (alternative to {@code objects}). */
  private final Supplier<T> supplier;

  /** The set of used objects. */
  private final Map<ResourceOperation, Collection<T>> used;

  /** The set of values for some of the objects in use. */
  private final Map<T, V> init;

  /**
   * Constructs a resource allocation table.
   *
   * @param allocationData the allocation data.
   * @param objects the available objects.
   */
  public AllocationTable(final AllocationData<T> allocationData, final Collection<T> objects) {
    InvariantChecks.checkNotNull(allocationData);
    InvariantChecks.checkNotEmpty(objects);

    this.allocationData = allocationData;
    this.objects = Collections.unmodifiableCollection(objects);
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
   * @param allocationData the allocation data.
   * @param supplier the object generator.
   */
  public AllocationTable(final AllocationData<T> allocationData, final Supplier<T> supplier) {
    InvariantChecks.checkNotNull(supplier);

    this.allocationData = allocationData;
    this.objects = null;
    this.supplier = supplier;
    this.used = new EnumMap<>(ResourceOperation.class);
    for (final ResourceOperation operation : ResourceOperation.values()) {
      this.used.put(operation, new LinkedHashSet<T>());
    }
    this.init = new LinkedHashMap<>();
  }

  /**
   * Returns the default allocation data.
   *
   * @return the default allocation data.
   */
  public AllocationData<T> getAllocationData() {
    return allocationData;
  }

  /**
   * Replaces the default allocation data.
   *
   * @param allocationData new allocation data.
   */
  public void setAllocationData(final AllocationData<T> allocationData) {
    InvariantChecks.checkNotNull(allocationData);
    this.allocationData = allocationData;
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

    for (final Map.Entry<ResourceOperation, Collection<T>> entry : used.entrySet()) {
      entry.getValue().remove(object);
    }

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
      used.get(ResourceOperation.ANY).add(object);
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
   * @param retain the objects that should be used for allocation.
   * @param rate the dependencies biases.
   * @return the peeked object.
   */
  public T peek(
      final Set<T> exclude,
      final Set<T> retain,
      final Map<ResourceOperation, Integer> rate) {
    InvariantChecks.checkNotNull(exclude);
    InvariantChecks.checkNotNull(retain);

    final Allocator allocator = allocationData.getAllocator();

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
   * @param retain the objects that should be used for allocation.
   * @param rate the dependencies biases.
   * @return an allocated object.
   */
  public T allocate(
      final ResourceOperation operation,
      final Set<T> exclude,
      final Set<T> retain,
      final Map<ResourceOperation, Integer> rate) {
    final T object = peek(exclude, retain, rate);

    use(operation, object);
    return object;
  }

  /**
   * Allocates an object and defines it.
   *
   * @param value the object value.
   * @param rate the dependencies biases.
   * @return an allocated object.
   */
  public T allocateAndDefine(final V value, final Map<ResourceOperation, Integer> rate) {
    InvariantChecks.checkNotNull(value);

    final T object = allocate(ResourceOperation.WRITE, Collections.<T>emptySet(), Collections.<T>emptySet(), rate);
    define(object, value);

    return object;
  }

  /**
   * Allocates an object and defines it.
   *
   * @param exclude the objects that should not be allocated.
   * @param value the object value.
   * @param rate the dependencies biases.
   * @return an allocated object.
   */
  public T allocateAndDefine(
      final Set<T> exclude,
      final V value,
      final Map<ResourceOperation, Integer> rate) {
    InvariantChecks.checkNotNull(value);

    final T object = allocate(ResourceOperation.WRITE, exclude, Collections.<T>emptySet(), rate);
    define(object, value);

    return object;
  }

  /**
   * Returns the set of used objects.
   *
   * @return the set of used objects.
   */
  public Map<ResourceOperation, Collection<T>> getUsedObjects() {
    return used;
  }

  /**
   * Returns the set of all objects.
   *
   * @return the set of all objects.
   */
  public Collection<T> getAllObjects() {
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
