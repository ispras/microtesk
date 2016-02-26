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
  private final BigInteger base;
  private BigInteger from;
  private BigInteger to;

  public MemoryRegion(
      final BigInteger base,
      final BigInteger from,
      final BigInteger to) {
    InvariantChecks.checkNotNull(base);
    InvariantChecks.checkGreaterOrEq(base, BigInteger.ZERO);

    InvariantChecks.checkNotNull(from);
    InvariantChecks.checkGreaterOrEq(from, base);

    InvariantChecks.checkNotNull(to);
    InvariantChecks.checkGreaterOrEq(to, base);

    InvariantChecks.checkGreaterOrEq(to, from);

    this.base = base;
    this.from = from;
    this.to = to;
  }

  public BigInteger getBase() {
    return base;
  }

  public BigInteger getFrom() {
    return from;
  }

  public void setFrom(final BigInteger value) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkGreaterOrEq(value, base);
    InvariantChecks.checkGreaterOrEq(from, value);

    this.from = value;
  }

  public BigInteger getTo() {
    return to;
  }

  public void setTo(final BigInteger value) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkGreaterOrEq(value, to);

    this.to = value;
  }

  public boolean isMatch(final BigInteger value) {
    InvariantChecks.checkNotNull(value);
    return from.compareTo(value) <= 0 && value.compareTo(to) <= 0;
  }

  public boolean isOverlap(final MemoryRegion other) {
    InvariantChecks.checkNotNull(other);
    return !(this.to.compareTo(other.from) < 0 || this.from.compareTo(other.to) > 0);
  }

  @Override
  public String toString() {
    return String.format("Region: [0x%x..0x%x], Base: 0x%x", from, to, base);
  }
}
