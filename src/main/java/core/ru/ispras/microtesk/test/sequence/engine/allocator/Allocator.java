/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

import java.util.Map;
import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * Allocates resources using a specific strategy with specific attributes.
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
      final Collection<T> domain,
      final Collection<T> used) {
    return strategy.next(domain, used, attributes);
  }
}
