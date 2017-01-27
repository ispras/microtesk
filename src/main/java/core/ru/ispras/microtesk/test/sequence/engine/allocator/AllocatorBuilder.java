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

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.test.GenerationAbortedException;

/**
 * Builds an allocator.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class AllocatorBuilder {
  private final String strategy;
  private Map<String, String> attributes;

  public AllocatorBuilder(final String strategy) {
    this.strategy = strategy;
    this.attributes = null;
  }

  public void setAttribute(final String name, final String value) {
    if (null == attributes) {
      attributes = new HashMap<>();
    }

    attributes.put(name, value);
  }

  public Allocator build() {
    if (null == strategy) {
      throw new GenerationAbortedException(
          "Allocation strategy is not specified.");
    }

    final AllocationStrategyId strategyId =
        AllocationStrategyId.valueOf(strategy.toUpperCase());

    if (null == strategyId) {
      throw new GenerationAbortedException(
          "Unsupported allocation strategy: " + strategy);
    }

    return new Allocator(strategyId, attributes);
  }
}
