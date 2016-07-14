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

package ru.ispras.microtesk.test.template;

import java.math.BigInteger;

import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.randomizer.VariateInterval;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.SharedObject;

public final class RandomValue extends SharedObject<RandomValue> implements Value {
  private final Variate<BigInteger> variate;
  private BigInteger value;

  protected RandomValue(final Variate<BigInteger> variate) {
    InvariantChecks.checkNotNull(variate);

    this.variate = variate;
    this.value = null;
  }

  protected RandomValue(final BigInteger min, final BigInteger max) {
    this(new VariateInterval<>(min, max));
  }

  private RandomValue(final RandomValue other) {
    super(other);

    this.variate = other.variate;
    this.value = other.value;
  }

  @Override
  public RandomValue newCopy() {
    return new RandomValue(this);
  }

  @Override
  public Value copy() {
    return newCopy();
  }

  @Override
  public BigInteger getValue() {
    if (null == value) {
      value = variate.value();
    }
    return value;
  }

  @Override
  public String toString() {
    return null != value ? value.toString() : "RandomValue";
  }
}
