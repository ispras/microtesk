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

package ru.ispras.microtesk.mmu.translator.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.mmu.model.api.PolicyId;

public final class Buffer extends AbstractStorage implements TypeProvider {
  private final int ways;
  private final int sets;
  private final Node index;
  private final Node match;
  private final PolicyId policy;
  private final Buffer parent;

  public Buffer(
      String id,
      Address address,
      Variable addressArg,
      Variable dataArg,
      int ways,
      int sets,
      Node index,
      Node match,
      PolicyId policy,
      Buffer parent) {

    super(id, address, addressArg, dataArg, createAttributes(addressArg, dataArg));

    checkGreaterThanZero(ways);
    checkGreaterThanZero(sets);
    checkNotNull(index);
    checkNotNull(match);
    checkNotNull(policy);

    this.ways = ways;
    this.sets = sets;
    this.index = index;
    this.match = match;
    this.policy = policy;
    this.parent = parent;
  }

  private static Map<String, Attribute> createAttributes(Variable addressArg, Variable dataArg) {
    checkNotNull(addressArg);
    checkNotNull(dataArg);

    final Attribute[] attrs = new Attribute[] { 
        new Attribute(HIT_ATTR_NAME, DataType.BOOLEAN),
        new Attribute(READ_ATTR_NAME, dataArg.getDataType()),
        new Attribute(WRITE_ATTR_NAME, dataArg.getDataType())
    };

    final Map<String, Attribute> result = new LinkedHashMap<>();
    for (Attribute attr : attrs) {
      result.put(attr.getId(), attr);
    }

    return Collections.unmodifiableMap(result);
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
    return getDataArg().getType();
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

  public Buffer getParent() {
    return parent;
  }

  @Override
  public String toString() {
    return String.format(
        "buffer %s(%s) = {ways=%d, sets=%d, entry=%s, index=%s, match=%s, policy=%s}",
        getId(), getAddressArg(), ways, sets, getEntry(), index, match, policy);
  }
}
