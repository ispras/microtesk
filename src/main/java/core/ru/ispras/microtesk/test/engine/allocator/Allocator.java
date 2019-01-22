/*
 * Copyright 2014-2019 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.randomizer.VariateBiased;
import ru.ispras.microtesk.utils.function.Supplier;

/**
 * {@link Allocator} defines some resource allocation strategies.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public enum Allocator {
  /** Returns a randomly chosen free or used object. */
  RANDOM() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final Collection<T> objects = new LinkedHashSet<>(retain);
      objects.removeAll(exclude);

      return !objects.isEmpty() ? Randomizer.get().choose(objects) : null;
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final int tries = 7;
      for (int i = 0; i < tries; i++) {
        final T object = supplier.get();

        if (object == null) {
          return null;
        }

        if (!exclude.contains(object)) {
          return object;
        }
      }

      return null;
    }
  },

  /** Returns a free object or {@code null} if all the objects are in use. */
  FREE() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final Collection<T> free = new LinkedHashSet<>(retain);

      free.removeAll(exclude);
      free.removeAll(used.get(ResourceOperation.ANY));

      return !free.isEmpty() ? Randomizer.get().choose(free) : null;
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final int tries = 7;
      for (int i = 0; i < tries; i++) {
        final T object = supplier.get();

        if (object == null) {
          return null;
        }

        if (!exclude.contains(object) && !used.get(ResourceOperation.ANY).contains(object)) {
          return object;
        }
      }

      return null;
    }
  },

  /** Returns a used object or {@code null} there are no such objects. */
  USED() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      return Allocator.<T>next(ResourceOperation.ANY, exclude, used, rate);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      return Allocator.<T>next(ResourceOperation.ANY, supplier, exclude, used, rate);
    }
  },

  /** Returns an object being read or {@code null} if there are no such objects. */
  READ() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      return Allocator.<T>next(ResourceOperation.READ, exclude, used, rate);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      return Allocator.<T>next(ResourceOperation.READ, supplier, exclude, used, rate);
    }
  },

  /** Returns an object being written or {@code null} if there are no such objects. */
  WRITE() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      return Allocator.<T>next(ResourceOperation.WRITE, exclude, used, rate);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      return Allocator.<T>next(ResourceOperation.WRITE, supplier, exclude, used, rate);
    }
  },

  /** Returns a free object (if available) or a used one (otherwise). */
  TRY_FREE() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final T object = FREE.next(retain, exclude, used, rate);
      return object != null ? object : USED.next(retain, exclude, used, rate);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final T object = FREE.next(supplier, exclude, used, rate);
      return object != null ? object : USED.next(supplier, exclude, used, rate);
    }
  },

  /** Returns a used object (if available) or a random one (otherwise). */
  TRY_USED() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final T object = USED.next(retain, exclude, used, rate);
      return object != null ? object : RANDOM.next(retain, exclude, used, rate);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final T object = USED.next(supplier, exclude, used, rate);
      return object != null ? object : RANDOM.next(supplier, exclude, used, rate);
    }
  },

  /** Returns an object being read (if available) or a random one (otherwise). */
  TRY_READ() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final T object = READ.next(retain, exclude, used, rate);
      return object != null ? object : RANDOM.next(retain, exclude, used, rate);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final T object = READ.next(supplier, exclude, used, rate);
      return object != null ? object : RANDOM.next(supplier, exclude, used, rate);
    }
  },

  /** Returns an object being written (if available) or a random one (otherwise). */
  TRY_WRITE() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final T object = WRITE.next(retain, exclude, used, rate);
      return object != null ? object : RANDOM.next(retain, exclude, used, rate);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final T object = WRITE.next(supplier, exclude, used, rate);
      return object != null ? object : RANDOM.next(supplier, exclude, used, rate);
    }
  },

  /** Returns a randomly chosen object. */
  BIASED() {
    private final <T> Allocator getAllocator(
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {

      // Dependencies rates are not specified.
      if (rate == null || rate.isEmpty()) {
        return TRY_FREE;
      }

      final List<Allocator> values = new ArrayList<>();
      values.add(TRY_FREE);
      values.add(TRY_USED);
      values.add(TRY_READ);
      values.add(TRY_WRITE);

      final List<Integer> biases = new ArrayList<>();
      biases.add(rate.get(ResourceOperation.NOP));
      biases.add(rate.get(ResourceOperation.ANY));
      biases.add(rate.get(ResourceOperation.READ));
      biases.add(rate.get(ResourceOperation.WRITE));

      final VariateBiased<Allocator> variate = new VariateBiased<>(values, biases);
      return variate.value();
    }

    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final Allocator allocator = getAllocator(exclude, used, rate);
      System.out.println("USED: " + used);
      System.out.println("ALLOCATOR: " + allocator.name() + ", " + rate);
      return allocator.next(retain, exclude, used, rate);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Map<ResourceOperation, Collection<T>> used,
        final Map<ResourceOperation, Integer> rate) {
      final Allocator allocator = getAllocator(exclude, used, rate);
      return allocator.next(supplier, exclude, used, rate);
    }
  };

  private static <T> T next(
      final ResourceOperation operation,
      final Collection<T> exclude,
      final Map<ResourceOperation, Collection<T>> used,
      final Map<ResourceOperation, Integer> rate) {
    final Collection<T> objects = new LinkedHashSet<>(used.get(operation));
    objects.removeAll(exclude);

    return !objects.isEmpty() ? Randomizer.get().choose(objects) : null;
  }

  private static <T> T next(
      final ResourceOperation operation,
      final Supplier<T> supplier,
      final Collection<T> exclude,
      final Map<ResourceOperation, Collection<T>> used,
      final Map<ResourceOperation, Integer> rate) {
    return next(operation, exclude, used, rate);
  }

  /**
   * Chooses an object.
   *
   * @param <T> type of objects.
   * @param retain the set of all available objects.
   * @param exclude the set of objects to be excluded.
   * @param used the of used objects.
   * @param rate the dependencies biases.
   * @return the chosen object or {@code null}.
   */
  public abstract <T> T next(
      final Collection<T> retain,
      final Collection<T> exclude,
      final Map<ResourceOperation, Collection<T>> used,
      final Map<ResourceOperation, Integer> rate);

  /**
   * Generates an object.
   *
   * @param <T> type of objects.
   * @param supplier the object generator.
   * @param exclude the set of objects to be excluded.
   * @param used the set of used objects.
   * @param rate the dependencies biases.
   * @return the chosen object or {@code null}.
   */
  public abstract <T> T next(
      final Supplier<T> supplier,
      final Collection<T> exclude,
      final Map<ResourceOperation, Collection<T>> used,
      final Map<ResourceOperation, Integer> rate);
}
