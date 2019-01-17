/*
 * Copyright 2016-2019 ISP RAS (http://www.ispras.ru)
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
import java.util.EnumMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.function.Supplier;

/**
 * {@link Allocator} allocates resources using a specific strategy with specific attributes.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Allocator {
  /** The strategy for object allocation. */
  private final AllocationStrategy strategy;

  /** The strategy parameters. */
  private final Map<String, String> attributes;

  public Allocator(
      final AllocationStrategy strategy,
      final Map<String, String> attributes) {
    InvariantChecks.checkNotNull(strategy);

    this.strategy = strategy;
    this.attributes = attributes;
  }

  public <T> T next(
      final Collection<T> retain,
      final Collection<T> exclude,
      final EnumMap<ResourceOperation, Collection<T>> used) {
    return strategy.next(retain, exclude, used, attributes);
  }

  public <T> T next(
      final Supplier<T> supplier,
      final Collection<T> exclude,
      final EnumMap<ResourceOperation, Collection<T>> used) {
    return strategy.next(supplier, exclude, used, attributes);
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder("Allocator(");
    builder.append(strategy.toString());

    if (attributes != null) {
      builder.append(", ");
      builder.append(attributes.toString());
    }

    builder.append(')');
    return builder.toString();
  }
}
