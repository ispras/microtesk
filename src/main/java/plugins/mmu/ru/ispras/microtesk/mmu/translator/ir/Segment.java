/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.fortress.util.InvariantChecks;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

public final class Segment extends AbstractStorage {
  private final BigInteger min;
  private final BigInteger max;
  private final Address dataArgAddress;

  public Segment(
      final String id,
      final Address address,
      final Var addressArg,
      final BigInteger min,
      final BigInteger max,
      final Address dataArgAddress,
      final Var dataArg,
      final Map<String, Var> variables,
      final Map<String, Attribute> attrs) {
    super(
        id,
        address,
        addressArg,
        dataArg,
        variables,
        createAttributes(attrs)
        );

    InvariantChecks.checkNotNull(min);
    InvariantChecks.checkNotNull(max);

    this.min = min;
    this.max = max;

    this.dataArgAddress = dataArgAddress;
  }

  private static Map<String, Attribute> createAttributes(final Map<String, Attribute> map) {
    final Attribute hitAttr =
        new Attribute(HIT_ATTR_NAME, DataType.BOOLEAN);

    if (map.isEmpty()) {
      return Collections.singletonMap(hitAttr.getId(), hitAttr);
    }

    map.put(hitAttr.getId(), hitAttr);
    return map;
  }

  public BigInteger getMin() {
    return min;
  }

  public BigInteger getMax() {
    return max;
  }

  public Address getDataArgAddress() {
    return dataArgAddress;
  }

  @Override
  public String toString() {
    final int addressSize = getAddress().getAddressType().getBitSize();
    final int width = addressSize / 4 + addressSize % 4 != 0 ? 1 : 0;

    final String rangeFormat =
        "(0x%0" + width + "X, 0x%0" + width + "X)";

    return String.format(
        "segment %s(%s) range=%s, variables=%s, attributes=%s",
        getId(),
        getAddressArg(),
        String.format(rangeFormat, min, max),
        getVariables(),
        getAttributes()
        );
  }
}
