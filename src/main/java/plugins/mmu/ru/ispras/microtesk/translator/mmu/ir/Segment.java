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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;

public final class Segment {
  private final String id;
  private final Var addressArg;

  private final BitVector rangeStart;
  private final BitVector rangeEnd;
  
  private final Map<String, Attribute> attributes;

  public Segment(
      String id,
      String addressArgId, Address addressType,
      BitVector rangeStart, BitVector rangeEnd) {

    checkNotNull(id);
    checkNotNull(addressArgId);
    checkNotNull(addressType);
    checkNotNull(rangeStart);
    checkNotNull(rangeEnd);

    if (addressType.getBitSize() != rangeStart.getBitSize() ||
        addressType.getBitSize() != rangeEnd.getBitSize()) {
      throw new IllegalArgumentException();      
    }

    this.id = id;
    this.addressArg = new Var(addressArgId, addressType.getType(), addressType);
    this.rangeStart = rangeStart;
    this.rangeEnd = rangeEnd;
    
    final Node hitExpr = new NodeOperation(
        StandardOperation.AND,
        new NodeOperation(
            StandardOperation.BVUGE,
            addressArg.getVariable(),
            NodeValue.newBitVector(rangeStart)
            ),
        new NodeOperation(
            StandardOperation.BVULE,
            addressArg.getVariable(),
            NodeValue.newBitVector(rangeEnd)
            )
        );

    final Attribute hitAttr = new Attribute("hit", DataType.BOOLEAN,
        Collections.<Stmt>singletonList(new StmtExpr(hitExpr)));

    this.attributes =
        Collections.singletonMap(hitAttr.getId(), hitAttr);
  }

  public String getId() {
    return id;
  }

  public Var getAddressArg() {
    return addressArg;
  }

  public BitVector getRangeStart() {
    return rangeStart;
  }

  public BitVector getRangeEnd() {
    return rangeEnd;
  }
  
  public int getAttributeCount() {
    return attributes.size();
  }

  public Collection<Attribute> getAttributes() {
    return attributes.values();
  }

  public Attribute getAttribute(String name) {
    checkNotNull(name);
    return attributes.get(name);
  }

  @Override
  public String toString() {
    return String.format("segment %s(%s) range = (%s, %s)",
        id, addressArg, rangeStart.toHexString(), rangeEnd.toHexString());
  }
}
