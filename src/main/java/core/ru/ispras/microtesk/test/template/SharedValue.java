/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.SharedObject;

import java.math.BigInteger;

/**
 * The {@link SharedValue} class describes a modifiable value that can be shared among two owners.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class SharedValue extends SharedObject<SharedValue> implements Value {
  private BigInteger value;

  public SharedValue() {
    this.value = null;
  }

  public SharedValue(final SharedValue other) {
    super(other);
    this.value = other.value;
  }

  @Override
  public Value copy() {
    return newCopy();
  }

  @Override
  public SharedValue newCopy() {
    return new SharedValue(this);
  }

  @Override
  public BigInteger getValue() {
    InvariantChecks.checkNotNull(value, "Value is not set.");
    return value;
  }

  public boolean hasValue() {
    return null != value;
  }

  public void setValue(final BigInteger value) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(null == this.value, "Value is already set.");
    this.value = value;
  }

  @Override
  public String toString() {
    return null != value ? getValue().toString() : getClass().getSimpleName();
  }
}
