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

package ru.ispras.microtesk.model.api.memory;

import java.math.BigInteger;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link MemoryRegion} is designed to track allocated memory.
 * This must be done to prevent overlapping data and code allocations.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class MemoryRegion {
  private final BigInteger from;
  private BigInteger to;

  public MemoryRegion(
      final BigInteger from,
      final BigInteger to) {
    InvariantChecks.checkNotNull(from);
    InvariantChecks.checkGreaterOrEq(from, BigInteger.ZERO);

    InvariantChecks.checkNotNull(to);
    InvariantChecks.checkGreaterOrEq(to, from);

    this.from = from;
    this.to = to;
  }

  public BigInteger getFrom() {
    return from;
  }

  public BigInteger getTo() {
    return to;
  }

  public void growTo(final BigInteger value) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(isGreater(value));

    this.to = value;
  }

  public boolean isGreater(final BigInteger address) {
    InvariantChecks.checkNotNull(address);
    return address.compareTo(to) >= 0;
  }

  public boolean isOverlap(final MemoryRegion other) {
    InvariantChecks.checkNotNull(other);
    return !(this.to.compareTo(other.from) < 0 || this.from.compareTo(other.to) > 0);
  }

  @Override
  public String toString() {
    return String.format("[0x%x..0x%x]", from, to);
  }
}
