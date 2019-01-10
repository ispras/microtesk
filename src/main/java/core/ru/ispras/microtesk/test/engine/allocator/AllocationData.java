/*
 * Copyright 2018-2019 ISP RAS (http://www.ispras.ru)
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link AllocationData} holds data used for register allocation.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AllocationData {
  private final Allocator allocator;
  private final List<Value> retain;
  private final List<Value> exclude;
  private final Map<String, Object> readAfterRate;
  private final Map<String, Object> writeAfterRate;

  private final boolean reserved;

  public AllocationData() {
    this(
        null,
        Collections.<Value>emptyList(),
        Collections.<Value>emptyList(),
        Collections.<String, Object>emptyMap(),
        Collections.<String, Object>emptyMap(),
        false
    );
  }

  public AllocationData(
      final Allocator allocator,
      final List<Value> retain,
      final List<Value> exclude,
      final Map<String, Object> readAfterRate,
      final Map<String, Object> writeAfterRate,
      final boolean reserved) {
    // The allocator argument can be null.
    InvariantChecks.checkNotNull(retain);
    InvariantChecks.checkNotNull(exclude);
    InvariantChecks.checkNotNull(readAfterRate);
    InvariantChecks.checkNotNull(writeAfterRate);

    this.allocator = allocator;
    this.retain = retain;
    this.exclude = exclude;
    this.readAfterRate = Collections.unmodifiableMap(readAfterRate);
    this.writeAfterRate = Collections.unmodifiableMap(writeAfterRate);
    this.reserved = reserved;
  }

  public AllocationData(final AllocationData other) {
    InvariantChecks.checkNotNull(other);

    this.allocator = other.allocator;
    this.retain = AllocatorUtils.copyValues(other.retain);
    this.exclude = AllocatorUtils.copyValues(other.exclude);
    this.readAfterRate = other.readAfterRate;
    this.writeAfterRate = other.writeAfterRate;
    this.reserved = other.reserved;
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

  public Map<String, Object> getReadAfterRate() {
    return readAfterRate;
  }

  public Map<String, Object> getWriteAfterRate() {
    return writeAfterRate;
  }

  public boolean isReserved() {
    return reserved;
  }
}
