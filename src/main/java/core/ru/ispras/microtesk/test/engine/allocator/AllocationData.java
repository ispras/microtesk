/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.test.template.Value;

import java.util.List;

/**
 * The {@link AllocationData} class holds data used for register allocation.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class AllocationData {
  private final Allocator allocator;
  private final List<Value> retain;
  private final List<Value> exclude;

  public AllocationData() {
    this(null, null, null);
  }

  public AllocationData(
      final Allocator allocator,
      final List<Value> retain,
      final List<Value> exclude) {
    this.allocator = allocator;
    this.retain = retain;
    this.exclude = exclude;
  }

  public AllocationData(final AllocationData other) {
    InvariantChecks.checkNotNull(other);

    this.allocator = other.allocator;
    this.retain = AllocatorUtils.copyValues(other.retain);
    this.exclude = AllocatorUtils.copyValues(other.exclude);
  }

  public Allocator getAllocator() {
    return allocator;
  }

  public List<Value> getRetain() {
    return retain;
  }

  public List<Value> getExclude() {
    return exclude;
  }
}
