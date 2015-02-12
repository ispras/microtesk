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

import java.util.Collections;
import java.util.Map;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.model.api.mmu.PolicyId;

public final class Buffer extends AbstractStorage implements TypeProvider {
  private final int ways;
  private final int sets;
  private final Type entry;
  private final Node index;
  private final Node match;
  private final PolicyId policy;

  public Buffer(
      String id,
      Var addressArg,
      int ways,
      int sets,
      Type entry,
      Node index,
      Node match,
      PolicyId policy) {

    super(id, addressArg, null, createAttributes(addressArg));

    checkGreaterThanZero(ways);
    checkGreaterThanZero(sets);
    checkNotNull(entry);
    checkNotNull(index);
    checkNotNull(match);
    checkNotNull(policy);

    this.ways = ways;
    this.sets = sets;
    this.entry = entry;
    this.index = index;
    this.match = match;
    this.policy = policy;
  }

  private static Map<String, Attribute> createAttributes(Var addressArg) {
    checkNotNull(addressArg);

    final Attribute hitAttr = new Attribute(HIT_ATTR_NAME, DataType.BOOLEAN);
    return Collections.singletonMap(hitAttr.getId(), hitAttr);
  }

  @Override
  public Type getType() {
    return getEntry();
  }

  @Override
  public String getTypeAlias() {
    return String.format("%s.%s", getId(), "entry");
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
        "buffer %s(%s) = {ways=%d, sets=%d, entry=%s, index=%s, match=%s, policy=%s}",
        getId(), getAddressArg(), ways, sets, entry, index, match, policy);
  }
}
