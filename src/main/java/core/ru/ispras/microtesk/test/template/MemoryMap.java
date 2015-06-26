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

import static ru.ispras.fortress.util.InvariantChecks.checkGreaterOrEq;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public final class MemoryMap {
  private final Map<String, BigInteger> labels;

  MemoryMap() {
    this.labels = new HashMap<>(); 
  }

  public boolean isDefined(final String label) {
    return labels.containsKey(label);
  }

  public void addLabel(final String label, final BigInteger address) {
    checkNotNull(label);
    checkGreaterOrEq(address, BigInteger.ZERO);

    labels.put(label, address);
  }

  public BigInteger resolve(final String label) {
    checkNotNull(label);

    if (!labels.containsKey(label)) {
      throw new IllegalArgumentException(String.format("The %s label is not defined.", label));
    }

    return labels.get(label);
  }

  public BigInteger resolveWithDefault(final String label, final BigInteger defaultValue) {
    checkNotNull(label);

    if (!labels.containsKey(label)) {
      return defaultValue;
    }

    return labels.get(label);
  }
}
