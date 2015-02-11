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

public final class Buffer implements TypeProvider {
  private final String id;

  private final String addressArgId;
  private final Address addressArgType;

  private final int ways;
  private final int sets;
  private final Type entry;

  private final Node index;
  private final Node match;
  
  private final PolicyId policy;

  public Buffer(
      String id,
      String addressArgId,
      Address addressArgType,
      int ways,
      int sets,
      Type entry,
      Node index,
      Node match,
      PolicyId policy) {

    checkNotNull(id);
    checkNotNull(addressArgId);
    checkNotNull(addressArgType);
    checkGreaterThanZero(ways);
    checkGreaterThanZero(sets);
    checkNotNull(entry);
    checkNotNull(index);
    checkNotNull(match);
    checkNotNull(policy);

    this.id = id;
    this.addressArgId = addressArgId;
    this.addressArgType = addressArgType;
    this.ways = ways;
    this.sets = sets;
    this.entry = entry;
    this.index = index;
    this.match = match;
    this.policy = policy;
  }

  public String getId() {
    return id;
  }

  @Override
  public Type getType() {
    return getEntry();
  }

  @Override
  public String getTypeAlias() {
    return String.format("%s.%s", getId(), "entry");
  }

  public String getAddressArgId() {
    return addressArgId;
  }

  public Address getAddressArgType() {
    return addressArgType;
  }

  public int getWays() {
    return ways;
  }

  public int getSets() {
    return sets;
  }

  public Type getEntry() {
    return entry;
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

  @Override
  public String toString() {
    return String.format(
        "buffer %s(%s: %s(%d)) = {ways=%d, sets=%d, entry=%s, index=%s, match=%s, policy=%s}",
        id, addressArgId, addressArgType.getId(), addressArgType.getBitSize(),
        ways, sets, entry, index, match, policy
        );
  }
}
