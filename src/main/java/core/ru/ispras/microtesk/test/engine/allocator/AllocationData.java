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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link AllocationData} holds data used for register allocation.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AllocationData<T> {
  private final Allocator allocator;

  private final Collection<T> retain;
  private final Collection<T> exclude;

  private final int track;
  private final Map<ResourceOperation, Integer> readAfterRate;
  private final Map<ResourceOperation, Integer> writeAfterRate;

  private final boolean reserved;

  public AllocationData(
      final Allocator allocator,
      final Collection<T> retain,
      final Collection<T> exclude,
      final int track,
      final Map<ResourceOperation, Integer> readAfterRate,
      final Map<ResourceOperation, Integer> writeAfterRate,
      final boolean reserved) {
    InvariantChecks.checkNotNull(retain);
    InvariantChecks.checkNotNull(exclude);
    InvariantChecks.checkNotNull(readAfterRate);
    InvariantChecks.checkNotNull(writeAfterRate);

    this.allocator = allocator;
    this.retain = Collections.<T>unmodifiableCollection(retain);
    this.exclude = Collections.<T>unmodifiableCollection(exclude);
    this.track = track;
    this.readAfterRate = Collections.<ResourceOperation, Integer>unmodifiableMap(readAfterRate);
    this.writeAfterRate = Collections.<ResourceOperation, Integer>unmodifiableMap(writeAfterRate);
    this.reserved = reserved;
  }

  public AllocationData(
      final Allocator allocator,
      final Collection<T> retain,
      final Collection<T> exclude) {
    this(
        allocator,
        retain,
        exclude,
        -1, 
        Collections.<ResourceOperation, Integer>emptyMap(),
        Collections.<ResourceOperation, Integer>emptyMap(),
        false
    );
  }

  public AllocationData(
      final Allocator allocator,
      final Collection<T> retain) {
    this(
        allocator,
        retain,
        Collections.<T>emptySet()
    );
  }

  public AllocationData(final Allocator allocator) {
    this(allocator, Collections.<T>emptySet());
  }

  public AllocationData() {
    this(null, Collections.<T>emptySet());
  }

  public AllocationData(final AllocationData<T> other) {
    InvariantChecks.checkNotNull(other);

    this.allocator = other.allocator;
    this.retain = other.retain;
    this.exclude = other.exclude;
    this.track = other.track;
    this.readAfterRate = other.readAfterRate;
    this.writeAfterRate = other.writeAfterRate;
    this.reserved = other.reserved;
  }

  public Allocator getAllocator() {
    return allocator;
  }

  public Collection<T> getRetain() {
    return retain;
  }

  public Collection<T> getExclude() {
    return exclude;
  }

  public int getTrack() {
    return track;
  }

  public Map<ResourceOperation, Integer> getReadAfterRate() {
    return readAfterRate;
  }

  public Map<ResourceOperation, Integer> getWriteAfterRate() {
    return writeAfterRate;
  }

  public boolean isReserved() {
    return reserved;
  }

  public boolean isSpecified() {
    return allocator != null
        || !retain.isEmpty()
        || !exclude.isEmpty()
        || track != -1
        || !readAfterRate.isEmpty()
        || !writeAfterRate.isEmpty();
  }
}
