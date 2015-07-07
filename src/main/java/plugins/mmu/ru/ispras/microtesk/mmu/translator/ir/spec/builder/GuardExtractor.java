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

package ru.ispras.microtesk.mmu.translator.ir.spec.builder;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.microtesk.basis.solver.IntegerField;
import ru.ispras.microtesk.basis.solver.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.BufferAccessEvent;

final class GuardExtractor {
  private final MmuSubsystem specification;
  private final AtomExtractor atomExtractor;

  private final MmuGuard guard;
  private final MmuGuard negatedGuard;

  public GuardExtractor(
      final MmuSubsystem specification,
      final AtomExtractor atomExtractor,
      final Node condition) {
    checkNotNull(specification);
    checkNotNull(atomExtractor);
    checkNotNull(condition);

    this.specification = specification;
    this.atomExtractor = atomExtractor;

    final MmuGuard[] guards = getGuards(condition, false);

    guard = guards[0];
    negatedGuard = guards[1];
  }

  public MmuGuard getGuard() {
    return guard;
  }

  public MmuGuard getNegatedGuard() {
    return negatedGuard;
  }

  private MmuGuard[] getGuards(final Node cond, boolean reversed) {
    if (!cond.isType(DataTypeId.LOGIC_BOOLEAN)) {
      throw new IllegalStateException("Boolean expression is expected: " + cond);
    }

    final MmuGuard[] guards;
    if (cond.getKind() == Node.Kind.VARIABLE && cond.getUserData() instanceof AttributeRef) {
      final AttributeRef attrRef = (AttributeRef) cond.getUserData();
      guards = getEventBasedGuards(attrRef);
    } else if (cond.getKind() == Node.Kind.OPERATION) {
      final NodeOperation expr = (NodeOperation) cond;
      if (expr.getOperationId() == StandardOperation.NOT) {
        guards = getGuards(expr.getOperand(0), true);
      } else {
        guards = getEqualityBasedGuards(expr);
      }
    } else {
      throw new IllegalStateException("Unsupported condition expression format: " + cond);
    }

    if (reversed) {
      return new MmuGuard[] {guards[1], guards[0]};
    }

    return guards;
  }

  private MmuGuard[] getEventBasedGuards(final AttributeRef attrRef) {
    final Attribute attr = attrRef.getAttribute();
    if (!attr.getId().equals(AbstractStorage.HIT_ATTR_NAME)) {
      throw new IllegalStateException("Unsupported attribute call: " + attr.getId());
    }

    final MmuGuard hit;
    final MmuGuard miss;

    final AbstractStorage target = attrRef.getTarget();
    if (target instanceof Segment) {
      final Segment segment = (Segment) target;
      final MmuAddress address = specification.getAddress(segment.getAddress().getId());
      final IntegerVariable addressVar = address.getVariable(); 

      hit = new MmuGuard(MmuCondition.range(addressVar, segment.getMin(), segment.getMax()));
      miss = null; // TODO
    } else {
      final MmuDevice device = specification.getDevice(attrRef.getTarget().getId());
      hit = new MmuGuard(device, BufferAccessEvent.HIT);
      miss = new MmuGuard(device, BufferAccessEvent.MISS);
    }

    return new MmuGuard[] {hit, miss};
  }

  private MmuGuard[] getEqualityBasedGuards(final NodeOperation expr) {
    final Enum<?> operator = expr.getOperationId();
    if (StandardOperation.EQ != operator && StandardOperation.NOTEQ != operator) {
      throw new IllegalStateException("Not an equality based condition: " + expr);
    }

    final Atom lhs = atomExtractor.extract(expr.getOperand(0));
    final Atom rhs = atomExtractor.extract(expr.getOperand(1));

    final BigInteger value;
    final Atom variableAtom;

    if (Atom.Kind.VALUE == lhs.getKind()) {
      value = (BigInteger) lhs.getObject();
      variableAtom = rhs;
    } else if (Atom.Kind.VALUE == rhs.getKind()) {
      value = (BigInteger) rhs.getObject();
      variableAtom = lhs;
    } else {
      throw new IllegalArgumentException(
          "Both sides of an equality expression are constants.");
    }

    final MmuGuard eq;
    final MmuGuard noteq;
    switch (variableAtom.getKind()) {
      case VARIABLE: {
        final IntegerVariable intVar = (IntegerVariable) variableAtom.getObject();
        eq = new MmuGuard(MmuCondition.eq(intVar, value));
        noteq = new MmuGuard(MmuCondition.neq(intVar, value));
        break;
      }

      case FIELD: {
        final IntegerField intField = (IntegerField) variableAtom.getObject();
        final IntegerVariable intVar = intField.getVariable();
        final int lo = intField.getLoIndex();
        final int hi = intField.getHiIndex();
        eq = new MmuGuard(MmuCondition.eq(new IntegerField(intVar, lo, hi), value));
        noteq = new MmuGuard(MmuCondition.neq(new IntegerField(intVar, lo, hi), value));
        break;
      }

      case CONCAT: {
        final MmuExpression mmuExpr = (MmuExpression) variableAtom.getObject();
        eq = new MmuGuard(MmuCondition.eq(mmuExpr));
        noteq = new MmuGuard(MmuCondition.neq(mmuExpr));
        break;
      }

      default: {
        throw new IllegalArgumentException("Variable is expected.");
      }
    }

    return (StandardOperation.EQ == operator) ?
        new MmuGuard[] {eq, noteq} : new MmuGuard[] {noteq, eq};
  }
}
