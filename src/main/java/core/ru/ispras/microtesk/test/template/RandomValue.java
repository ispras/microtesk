/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import java.math.BigInteger;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;

public final class RandomValue implements Value {
  private final BigInteger min;
  private final BigInteger max;
  private BigInteger value;

  protected RandomValue(final BigInteger min, final BigInteger max) {
    InvariantChecks.checkNotNull(min);
    InvariantChecks.checkNotNull(max);
    InvariantChecks.checkGreaterOrEq(max, min);

    this.min = min;
    this.max = max;
    this.value = null;
  }

  protected RandomValue(final RandomValue other) {
    InvariantChecks.checkNotNull(other);

    this.min = other.min;
    this.max = other.max;
    this.value = other.value;
  }

  public BigInteger getMin() {
    return min;
  }

  public BigInteger getMax() {
    return max;
  }

  @Override
  public BigInteger getValue() {
    if (null == value) {
      value = Randomizer.get().nextBigIntegerRange(min, max);
    }
    return value;
  }

  @Override
  public String toString() {
    return null != value ? value.toString() : String.format("RandomValue[%d..%d]", min, max);
  }
}
