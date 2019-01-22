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
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.utils.function.Supplier;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * {@link AllocationTable} implements a resource allocation table, which is a finite set of objects
 * (registers, pages, etc.) in couple with allocation / deallocation methods.
 *
 * @param <T> type of objects.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AllocationTable<T> {
  /** Default allocation data (settings). */
  private AllocationData<T> allocationData;

  /** Set of all available objects. */
  private final Collection<T> objects;
  /** Object supplier (alternative to {@code objects}). */
  private final Supplier<T> supplier;

  /** Set of used objects. */
  private final Map<ResourceOperation, Collection<T>> used;

  /** Tracker used to relax the set of used objects. */
  private final Map<T, Integer> where = new HashMap<>();
  private final Map<Integer, Pair<T, ResourceOperation>> track = new HashMap<>();
  private int count = 0;

  private AllocationTable(
      final AllocationData<T> allocationData,
      final Collection<T> objects,
      final Supplier<T> supplier) {
    InvariantChecks.checkNotNull(allocationData);
    InvariantChecks.checkTrue((objects == null) != (supplier == null));

    this.allocationData = allocationData;
    this.objects = objects != null ? Collections.unmodifiableCollection(objects) : null;
    this.supplier = supplier;
    this.used = new EnumMap<>(ResourceOperation.class);
    for (final ResourceOperation operation : ResourceOperation.values()) {
      this.used.put(operation, new LinkedHashSet<T>());
    }
  }

  /**
   * Constructs a resource allocation table.
   *
   * @param allocationData the allocation data.
   * @param objects the available objects.
   */
  public AllocationTable(final AllocationData<T> allocationData, final Collection<T> objects) {
    this(allocationData, objects, null);
  }

  /**
   * Constructs a resource allocation table.
   *
   * @param allocationData the allocation data.
   * @param supplier the object generator.
   */
  public AllocationTable(final AllocationData<T> allocationData, final Supplier<T> supplier) {
    this(allocationData, null, supplier);
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
    for (final ResourceOperation operation : ResourceOperation.values()) {
      used.get(operation).clear();
    }

    where.clear();
    track.clear();

    count = 0;
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
   * Frees (deallocates) the object.
   *
   * @param object the object to be freed.
   */
  public void free(final T object) {
    checkObject(object);

    for (final ResourceOperation operation : ResourceOperation.values()) {
      used.get(operation).remove(object);
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

    if (allocationData.getTrack() > 0) {
      final Integer index = where.get(object);
      final Pair<T, ResourceOperation> entry = track.get(count);

      // Remove the previous usage of the object.
      if (index != null) {
        track.remove(index);
      }

      // Free a previously used object.
      if (entry != null) {
        free(entry.first);
        where.remove(entry.first);
      }

      // Track the object.
      where.put(object, count);
      track.put(count, new Pair<>(object, operation));

      count = (count + 1) % allocationData.getTrack();
    }

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
      final Collection<T> retain,
      final Collection<T> exclude,
      final Map<ResourceOperation, Integer> rate) {
    InvariantChecks.checkNotNull(retain);
    InvariantChecks.checkNotNull(exclude);

    final Allocator allocator = allocationData.getAllocator();
    InvariantChecks.checkNotNull(allocator);

    final T object;
    if (retain.isEmpty()) {
      object = (objects != null)
          ? allocator.next(objects,  exclude, used, rate)
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
      final Collection<T> retain,
      final Collection<T> exclude,
      final Map<ResourceOperation, Integer> rate) {
    final T object = peek(retain, exclude, rate);

    use(operation, object);
    return object;
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
