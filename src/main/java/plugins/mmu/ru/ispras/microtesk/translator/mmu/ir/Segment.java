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

import java.util.Collections;
import java.util.Map;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.types.bitvector.BitVector;

public final class Segment extends AbstractStorage {
  private final BitVector rangeStart;
  private final BitVector rangeEnd;

  public Segment(
      final String id,
      final Address address,
      final Variable addressArg,
      final BitVector rangeStart,
      final BitVector rangeEnd) {
    super(id, address, addressArg, null, createAttributes());

    checkNotNull(rangeStart);
    checkNotNull(rangeEnd);

    this.rangeStart = rangeStart;
    this.rangeEnd = rangeEnd;
  }

  private static Map<String, Attribute> createAttributes() {
    final Attribute hitAttr = new Attribute(HIT_ATTR_NAME, DataType.BOOLEAN);
    return Collections.singletonMap(hitAttr.getId(), hitAttr);
  }

  public BitVector getRangeStart() {
    return rangeStart;
  }

  public BitVector getRangeEnd() {
    return rangeEnd;
  }

  @Override
  public String toString() {
    return String.format("segment %s(%s) range = (0x%s, 0x%s)",
        getId(), getAddressArg(), rangeStart.toHexString(), rangeEnd.toHexString());
  }
}
