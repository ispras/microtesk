/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.mmu.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;

import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.model.api.mmu.PolicyId;

public final class Buffer {
  private final String name;
  private final Address addressType;
  private final String addressName;
  private final int ways;
  private final int sets;
  private final Entry format;
  private final Node index;
  private final Node match;
  private final PolicyId policy;

  Buffer(
      String name,
      Address addressType,
      String addressName,
      int ways,
      int sets,
      Entry format,
      Node index,
      Node match,
      PolicyId policy) {

    checkNotNull(name);
    checkNotNull(addressType);
    checkNotNull(addressName);
    checkGreaterThanZero(ways);
    checkGreaterThanZero(sets);
    checkNotNull(format);
    checkNotNull(index);
    checkNotNull(match);
    checkNotNull(policy);

    this.name = name;
    this.addressType = addressType;
    this.addressName = addressName;
    this.ways = ways;
    this.sets = sets;
    this.format = format;
    this.index = index;
    this.match = match;
    this.policy = policy;
  }

  public String getName() {
    return name;
  }

  public Address getAddressType() {
    return addressType;
  }

  public String getAddressName() {
    return addressName;
  }

  public int getWays() {
    return ways;
  }

  public int getSets() {
    return sets;
  }

  public Entry getFormat() {
    return format;
  }

  public Node getIndex() {
    return index;
  }

  public Node getMatch() {
    return match;
  }

  public PolicyId getPolicy() {
    return policy;
  }
}
