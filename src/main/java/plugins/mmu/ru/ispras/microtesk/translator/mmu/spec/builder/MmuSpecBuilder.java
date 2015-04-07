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

package ru.ispras.microtesk.translator.mmu.spec.builder;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.ExprTreeVisitor;
import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.mmu.PolicyId;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.mmu.ir.Address;
import ru.ispras.microtesk.translator.mmu.ir.Buffer;
import ru.ispras.microtesk.translator.mmu.ir.Field;
import ru.ispras.microtesk.translator.mmu.ir.Ir;
import ru.ispras.microtesk.translator.mmu.ir.Memory;
import ru.ispras.microtesk.translator.mmu.ir.Variable;
import ru.ispras.microtesk.translator.mmu.spec.MmuAction;
import ru.ispras.microtesk.translator.mmu.spec.MmuAddress;
import ru.ispras.microtesk.translator.mmu.spec.MmuAssignment;
import ru.ispras.microtesk.translator.mmu.spec.MmuDevice;
import ru.ispras.microtesk.translator.mmu.spec.MmuExpression;
import ru.ispras.microtesk.translator.mmu.spec.MmuGuard;
import ru.ispras.microtesk.translator.mmu.spec.MmuSpecification;
import ru.ispras.microtesk.translator.mmu.spec.MmuTransition;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerField;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;
import ru.ispras.microtesk.translator.mmu.spec.basis.MemoryOperation;

/**
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 */

public class MmuSpecBuilder implements TranslatorHandler<Ir> {
  public static final MmuAction START = new MmuAction("START");

  private MmuSpecification spec; 
  private Map<String, MmuAddress> addresses;

  @Override
  public void processIr(Ir ir) {
    System.out.println(ir);

    this.spec = new MmuSpecification();
    this.addresses = new HashMap<>();

    for (Address address : ir.getAddresses().values()) {
      registerAddress(address);
    }

    for (Buffer buffer : ir.getBuffers().values()) {
      registerDevice(buffer);
    }

    final Map<String, Memory> memories = ir.getMemories();
    if (memories.size() > 1) {
      throw new IllegalStateException("Only one load/store specification is allowed.");
    }

    final Memory memory = memories.values().iterator().next();
    registerControlFlow(memory);

    System.out.println(spec);
  }

  private void registerAddress(Address address) {
    final IntegerVariable variable = new IntegerVariable(address.getId(), address.getBitSize());
    final MmuAddress mmuAddress = new MmuAddress(variable);

    spec.registerAddress(mmuAddress);
    addresses.put(address.getId(), mmuAddress);
  }

  private void registerDevice(Buffer buffer) {
    final MmuAddress address = addresses.get(buffer.getAddress().getId());
    final boolean isReplaceable = PolicyId.NONE != buffer.getPolicy();

    final AddressFormatExtractor addressFormat = new AddressFormatExtractor(
        address, buffer.getAddressArg(), buffer.getIndex(), buffer.getMatch());

    final MmuDevice device = new MmuDevice(
        buffer.getId(),
        buffer.getWays(),
        buffer.getSets(),
        address,
        addressFormat.getTagExpr(),
        addressFormat.getIndexExpr(),
        addressFormat.getOffsetExpr(),
        isReplaceable
        );

    for(Field field : buffer.getEntry().getFields()) {
      device.addField(new IntegerVariable(field.getId(), field.getBitSize()));
    }

    spec.registerDevice(device);
  }

  private void registerControlFlow(Memory memory) {
    final MmuAddress address = addresses.get(memory.getAddress().getId());
    spec.setStartAddress(address);

    final MmuAction ROOT = new MmuAction("ROOT", new MmuAssignment(address.getAddress()));
    spec.registerAction(ROOT);

    spec.registerAction(START);
    spec.setStartAction(START);

    final MmuTransition IF_READ = new MmuTransition(
        ROOT, START, new MmuGuard(MemoryOperation.LOAD));

    final MmuTransition IF_WRITE = new MmuTransition(
        ROOT, START, new MmuGuard(MemoryOperation.STORE));

    spec.registerTransition(IF_READ);
    spec.registerTransition(IF_WRITE);
  }
}

final class AddressFormatExtractor {
  private final MmuAddress address;
  private final Variable addressArg;
  private final FieldTracker addressFieldTracker;

  private final MmuExpression indexExpr;
  private final MmuExpression tagExpr;
  private final MmuExpression offsetExpr;

  class Visitor extends ExprTreeVisitorDefault {
    
  }

  AddressFormatExtractor(MmuAddress address, Variable addressArg, Node index, Node match) {
    this.address = address;
    this.addressArg = addressArg;
    this.addressFieldTracker = new FieldTracker(addressArg.getBitSize());

    this.indexExpr = extractIndexExpr(index);
    this.tagExpr = extractTagExpr(index);
    this.offsetExpr = MmuExpression.ZERO();

    /*
    this.tagExpr = isZero(match) ? MmuExpression.ZERO() : null;

    System.out.println(index);
    System.out.println(isZero(index));

    System.out.println(match);
    System.out.println(isZero(match));
    */
  }

  private MmuExpression exressionForFields(List<Field> fields) {
    final MmuExpression expression = new MmuExpression();

    for (Field field : fields) {
      expression.addLoTerm(new IntegerField(address.getAddress(), field.lo, hi));
    }

    return expression;
    
  }

  List<Field> extractFields(Node expr) {
    final ExprTreeVisitor visitor = new Visitor();
    final ExprTreeWalker walker = new ExprTreeWalker(visitor);
    walker.visit(expr);

    return Collections.emptyList();
  }

  MmuExpression extractTagExpr(Node index) {
    return MmuExpression.ZERO();
  }

  MmuExpression extractIndexExpr(Node index) {
    
    System.out.println("!!! " + index);
    return MmuExpression.ZERO();
  }

  MmuExpression getIndexExpr() {
    return indexExpr;
  }

  MmuExpression getTagExpr() {
    return tagExpr;
  }
  
  MmuExpression getOffsetExpr() {
    return offsetExpr;
  }

  boolean isZero(Node expr) {
    if (expr.getKind() != Node.Kind.VALUE) {
      return false;
    }

    if (!expr.isType(DataTypeId.LOGIC_INTEGER)) {
      return false;
    }

    return ((NodeValue) expr).getInteger().equals(BigInteger.ZERO);
  }
}
