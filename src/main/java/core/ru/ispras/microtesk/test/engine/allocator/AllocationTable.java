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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.function.Supplier;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link AllocationTable} implements a resource allocation table, which is a finite set of objects
 * (registers, pages, etc.) in couple with allocation / deallocation methods.
 *
 * @param <T> type of objects.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AllocationTable<T> {
  /** The default allocation data. */
  private AllocationData<T> allocationData;

  /** The set of all available objects. */
  private final Collection<T> objects;
  /** The object supplier (alternative to {@code objects}). */
  private final Supplier<T> supplier;

  /** The set of used objects. */
  private final Map<ResourceOperation, Collection<T>> used;

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
   * Frees (deallocates) the object.
   *
   * @param object the object to be freed.
   */
  public void free(final T object) {
    checkObject(object);

    for (final Map.Entry<ResourceOperation, Collection<T>> entry : used.entrySet()) {
      entry.getValue().remove(object);
    }
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
   * Peeks an object.
   *
   * @param retain the objects that should be used for allocation.
   * @param exclude the objects that should not be peeked.
   * @param rate the dependencies biases.
   * @return the peeked object.
   */
  public T peek(
      final Set<T> retain,
      final Set<T> exclude,
      final Map<ResourceOperation, Integer> rate) {
    InvariantChecks.checkNotNull(retain);
    InvariantChecks.checkNotNull(exclude);

    final Allocator allocator = allocationData.getAllocator();
    InvariantChecks.checkNotNull(allocator);

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
   * @param retain the objects that should be used for allocation.
   * @param exclude the objects that should not be allocated.
   * @param rate the dependencies biases.
   * @return an allocated object.
   */
  public T allocate(
      final ResourceOperation operation,
      final Set<T> retain,
      final Set<T> exclude,
      final Map<ResourceOperation, Integer> rate) {
    final T object = peek(retain, exclude, rate);

    use(operation, object);
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
