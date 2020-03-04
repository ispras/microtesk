/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.model.sim.EvictPolicyId;
import ru.ispras.microtesk.mmu.model.spec.MmuBuffer;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Buffer extends AbstractStorage {

  private static final String TO_STRING_FMT =
      "%sbuffer %s(%s) ="
          + " {ways=%d, sets=%d, entry=%s, index=%s, match=%s, policy=%s, parent=%s}";
  private final MmuBuffer.Kind kind;
  private final boolean isView;
  private final BigInteger ways;
  private final BigInteger sets;
  private final Node index;
  private final Node match;
  private final EvictPolicyId policy;
  private final Buffer next;

  public Buffer(
      final String id,
      final MmuBuffer.Kind kind,
      final boolean isView,
      final Address address,
      final Var addressArg,
      final Var dataArg,
      final BigInteger ways,
      final BigInteger sets,
      final Node index,
      final Node match,
      final EvictPolicyId policy,
      final Buffer next) {

    super(
        id,
        address,
        addressArg,
        dataArg,
        Collections.<String, Var>emptyMap(),
        createAttributes(addressArg, dataArg)
    );

    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkTrue(ways.compareTo(BigInteger.ZERO) > 0);
    InvariantChecks.checkTrue(sets.compareTo(BigInteger.ZERO) > 0);
    InvariantChecks.checkNotNull(index);
    InvariantChecks.checkNotNull(match);
    InvariantChecks.checkNotNull(policy);

    this.kind = kind;
    this.isView = isView;
    this.ways = ways;
    this.sets = sets;
    this.index = index;
    this.match = match;
    this.policy = policy;
    this.next = next;
  }

  private static Map<String, Attribute> createAttributes(
      final Var addressArg,
      final Var dataArg) {

    InvariantChecks.checkNotNull(addressArg);
    InvariantChecks.checkNotNull(dataArg);

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

  public MmuBuffer.Kind getKind() {
    return kind;
  }

  public boolean isView() {
    return isView;
  }

  public BigInteger getWays() {
    return ways;
  }

  public BigInteger getSets() {
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

  public EvictPolicyId getPolicy() {
    return policy;
  }

  public Buffer getNext() {
    return next;
  }

  @Override
  public String toString() {
    return String.format(
        TO_STRING_FMT,
        kind.getText(),
        getId(),
        getAddressArg(),
        ways,
        sets,
        getEntry(),
        index,
        match,
        policy,
        next != null ? next.getId() : null
        );
  }
}
