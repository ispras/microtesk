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
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;

public final class Segment extends AbstractStorage {
  private final BitVector rangeStart;
  private final BitVector rangeEnd;

  public Segment(
      String id, Variable addressArg, BitVector rangeStart, BitVector rangeEnd) {

    super(id, addressArg, null, createAttributes(addressArg, rangeStart, rangeEnd));

    this.rangeStart = rangeStart;
    this.rangeEnd = rangeEnd;
  }

  private static Map<String, Attribute> createAttributes(
      Variable addressArg, BitVector rangeStart, BitVector rangeEnd) {

    checkNotNull(addressArg);
    checkNotNull(rangeStart);
    checkNotNull(rangeEnd);

    if (addressArg.getBitSize() != rangeStart.getBitSize() ||
        addressArg.getBitSize() != rangeEnd.getBitSize()) {
      throw new IllegalArgumentException();      
    }

    final Node hitExpr = new NodeOperation(
        StandardOperation.AND,
        new NodeOperation(StandardOperation.BVUGE,
            addressArg.getVariable(), NodeValue.newBitVector(rangeStart)),
        new NodeOperation(StandardOperation.BVULE,
            addressArg.getVariable(), NodeValue.newBitVector(rangeEnd))
        );

    final Attribute hitAttr = new Attribute(HIT_ATTR_NAME,
        DataType.BOOLEAN, Collections.<Stmt>singletonList(new StmtExpr(hitExpr)));

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
    return String.format("segment %s(%s) range = (%s, %s)",
        getId(), getAddressArg(), rangeStart.toHexString(), rangeEnd.toHexString());
  }
}
