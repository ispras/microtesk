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
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.randomizer.VariateBiased;
import ru.ispras.microtesk.utils.function.Supplier;

/**
 * {@link AllocationStrategyId} defines some resource allocation strategies.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public enum AllocationStrategyId implements AllocationStrategy {
  /** Returns a free object or {@code null} if all the objects are in use. */
  FREE() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {
      final Collection<T> free = new LinkedHashSet<>(retain);

      free.removeAll(exclude);
      free.removeAll(getUsedObjects(used));

      return !free.isEmpty() ? Randomizer.get().choose(free) : null;
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {
      final int tries = 7;
      final Collection<T> usedObjects = getUsedObjects(used);

      for (int i = 0; i < tries; i++) {
        final T object = supplier.get();

        if (object == null) {
          return null;
        }

        if (!exclude.contains(object) && !usedObjects.contains(object)) {
          return object;
        }
      }

      return null;
    }
  },

  /** Returns a used object or {@code null} if no object is in use. */
  USED() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {
      final Collection<T> array = new LinkedHashSet<>(retain);

      array.removeAll(exclude);
      array.retainAll(getUsedObjects(used));

      return !array.isEmpty() ? Randomizer.get().choose(array) : null;
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {
      return USED.next(Collections.<T>emptyList(), exclude, used, rate, attributes);
    }
  },

  /** Returns a free object (if it exists) or a used one (otherwise). */
  TRY_FREE() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {
      final T object = FREE.next(retain, exclude, used, rate, attributes);
      return object != null ? object : USED.next(retain, exclude, used, rate, attributes);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {
      final T object = FREE.next(supplier, exclude, used, rate, attributes);
      return object != null ? object : USED.next(supplier, exclude, used, rate, attributes);
    }
  },

  /** Returns a used object (if such is available) or a free one (otherwise). */
  TRY_USED() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {
      final T object = USED.next(retain, exclude, used, rate, attributes);
      return object != null ? object : FREE.next(retain, exclude, used, rate, attributes);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {
      final T object = USED.next(supplier, exclude, used, rate, attributes);
      return object != null ? object : FREE.next(supplier, exclude, used, rate, attributes);
    }
  },

  /** Returns a randomly chosen object. */
  BIASED() {
    private static final String ATTR_FREE_BIAS = "free-bias";
    private static final String ATTR_USED_BIAS = "used-bias";

    private final <T> AllocationStrategy getAllocationStrategy(
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {

      if (attributes != null
          && attributes.containsKey(ATTR_FREE_BIAS)
          && attributes.containsKey(ATTR_USED_BIAS)) {
        final List<AllocationStrategy> values = new ArrayList<>();
        values.add(TRY_FREE);
        values.add(TRY_USED);

        final List<Integer> biases = new ArrayList<>();
        biases.add(Integer.parseInt(attributes.get(ATTR_FREE_BIAS)));
        biases.add(Integer.parseInt(attributes.get(ATTR_USED_BIAS)));

        final VariateBiased<AllocationStrategy> variate = new VariateBiased<>(values, biases);
        return variate.value();
      }

      return TRY_FREE;
    }

    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {
      final AllocationStrategy strategy = getAllocationStrategy(exclude, used, rate, attributes);
      return strategy.next(retain, exclude, used, rate, attributes);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {
      final AllocationStrategy strategy = getAllocationStrategy(exclude, used, rate, attributes);
      return strategy.next(supplier, exclude, used, rate, attributes);
    }
  },

  /** Returns a randomly chosen free or used object. */
  RANDOM() {
    @Override
    public <T> T next(
        final Collection<T> retain,
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {
      final Collection<T> array = new LinkedHashSet<>(retain);
      array.removeAll(exclude);
      return !array.isEmpty() ? Randomizer.get().choose(array) : null;
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final EnumMap<ResourceOperation, Collection<T>> used,
        final EnumMap<ResourceOperation, Integer> rate,
        final Map<String, String> attributes) {
      final int N = 5;

      for (int i = 0; i < N; i++) {
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
  };

  public static <T> Collection<T> getUsedObjects(
      final EnumMap<ResourceOperation, Collection<T>> used) {
    final Collection<T> result = new LinkedHashSet<>();

    for (final Map.Entry<ResourceOperation, Collection<T>> entry : used.entrySet()) {
      if (entry.getKey() != ResourceOperation.NOP) {
        result.addAll(entry.getValue());
      }
    }

    return result;
  }
}
