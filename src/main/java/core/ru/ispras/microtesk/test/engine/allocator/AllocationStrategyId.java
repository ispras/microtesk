/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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
 */
public enum AllocationStrategyId implements AllocationStrategy {
  /** Always returns a free object (throws an exception if all the objects are in use). */
  FREE() {
    @Override
    public <T> T next(
        final Collection<T> domain,
        final Collection<T> exclude,
        final Collection<T> used,
        final Map<String, String> attributes) {
      final Collection<T> free = new LinkedHashSet<>(domain);

      free.removeAll(exclude);
      free.removeAll(used);

      return !free.isEmpty() ? Randomizer.get().choose(free) : null;
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Collection<T> used,
        final Map<String, String> attributes) {
      final int N = 5;

      for (int i = 0; i < N; i++) {
        final T object = supplier.get();

        if (object == null) {
          return null;
        }

        if (!exclude.contains(object) && !used.contains(object)) {
          return object;
        }
      }

      return null;
    }
  },

  /** Always returns a used object (throws an exception if there are no object are in use). */
  USED() {
    @Override
    public <T> T next(
        final Collection<T> domain,
        final Collection<T> exclude,
        final Collection<T> used,
        final Map<String, String> attributes) {
      return !used.isEmpty() ? Randomizer.get().choose(used) : null;
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Collection<T> used,
        final Map<String, String> attributes) {
      return USED.next(Collections.<T>emptyList(), exclude, used, attributes);
    }
  },

  /** Returns a free object (if it exists) or a used one (otherwise). */
  TRY_FREE() {
    @Override
    public <T> T next(
        final Collection<T> domain,
        final Collection<T> exclude,
        final Collection<T> used,
        final Map<String, String> attributes) {
      final T object = FREE.next(domain, exclude, used, attributes);
      return object != null ? object : USED.next(domain, exclude, used, attributes);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Collection<T> used,
        final Map<String, String> attributes) {
      final T object = FREE.next(supplier, exclude, used, attributes);
      return object != null ? object : USED.next(supplier, exclude, used, attributes);
    }
  },

  /** Returns a randomly chosen object. */
  RANDOM() {
    private static final String ATTR_FREE_BIAS = "free-bias";
    private static final String ATTR_USED_BIAS = "used-bias";

    private final <T> AllocationStrategy getAllocationStrategy(
        final Collection<T> used,
        final Map<String, String> attributes) {

      if (used.isEmpty()) {
        return FREE;
      }

      if (attributes != null
          && attributes.containsKey(ATTR_FREE_BIAS)
          && attributes.containsKey(ATTR_USED_BIAS)) {
        final List<AllocationStrategy> values = new ArrayList<>();
        values.add(TRY_FREE);
        values.add(USED);

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
        final Collection<T> domain,
        final Collection<T> exclude,
        final Collection<T> used,
        final Map<String, String> attributes) {
      final AllocationStrategy strategy = getAllocationStrategy(used, attributes);
      return strategy.next(domain, exclude, used, attributes);
    }

    @Override
    public <T> T next(
        final Supplier<T> supplier,
        final Collection<T> exclude,
        final Collection<T> used,
        final Map<String, String> attributes) {
      final AllocationStrategy strategy = getAllocationStrategy(used, attributes);
      return strategy.next(supplier, exclude, used, attributes);
    }
  };
}
