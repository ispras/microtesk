/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Segment;

final class GuardPrinter {
  private final Ir ir;
  private final String context;

  private final Condition condTrue;
  private final Condition condFalse;

  public GuardPrinter(final Ir ir, final String context, final Node expr) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(expr);

    this.ir = ir;
    this.context = context;

    this.condTrue = Condition.extract(expr);
    this.condFalse = condTrue.not();
  }

  public String getGuard() {
    return printGuard(condTrue);
  }

  public String getNegatedGuard() {
    return printGuard(condFalse);
  }

  private String printGuard(final Condition cond) {
    InvariantChecks.checkNotNull(cond);

    final List<Node> equalities = new ArrayList<>();
    final List<Node> segments = new ArrayList<>();
    final List<Node> buffers = new ArrayList<>();

    for (final Node atom : cond.getAtoms()) {
      if (isEquality(atom)) {
        equalities.add(atom);
      } else if (isSegmentAccess(atom)) {
        segments.add(atom);
      } else if (isBufferEvent(atom)) {
        buffers.add(atom);
      } else if (isBooleanVariable(atom)) {
        final String variableName = ((NodeVariable) atom).getName();

        final Node variable = new NodeVariable(variableName, DataType.BIT_VECTOR(1));
        variable.setUserData(atom.getUserData());

        final Node equality = Nodes.noteq(variable, NodeValue.newBitVector(BitVector.FALSE));
        equalities.add(equality);
      } else if (isNegatedBooleanVariable(atom)) {
        final NodeOperation op = (NodeOperation) atom;
        final NodeVariable var = (NodeVariable) op.getOperand(0);

        final Node variable = new NodeVariable(var.getName(), DataType.BIT_VECTOR(1));
        variable.setUserData(var.getUserData());

        final Node equality = Nodes.eq(variable, NodeValue.newBitVector(BitVector.FALSE));
        equalities.add(equality);
      } else {
        throw new IllegalStateException("Illegal atomic condition: " + atom);
      }
    }

    InvariantChecks.checkFalse(
        equalities.isEmpty() && segments.isEmpty() && buffers.isEmpty());

    if (!equalities.isEmpty() && segments.isEmpty() && buffers.isEmpty()) {
      if (1 == equalities.size()) {
        return String.format("new MmuGuard(%s)", Utils.toString(ir, context, equalities.get(0)));
      }

      final Node node = cond.getType() == Condition.Type.AND ?
          Nodes.and(equalities) : Nodes.or(equalities);

      return String.format("new MmuGuard(%s)", Utils.toString(ir, context, node));
    }

    if (equalities.isEmpty() && !segments.isEmpty() && buffers.isEmpty()) {
      InvariantChecks.checkTrue(segments.size() == 1, "One segment event is allowed.");

      final Pair<String, Boolean> segmentInfo = extractSegmentInfo(segments.get(0));
      return String.format("new MmuGuard(%s.get(), %b)", segmentInfo.first, segmentInfo.second);
    }

    if (equalities.isEmpty() && segments.isEmpty() && !buffers.isEmpty()) {
      InvariantChecks.checkTrue(buffers.size() == 1, "One buffer event is allowed.");
      final Pair<Pair<String, String>, BufferAccessEvent> bufferEvent =
          extractBufferEventInfo(buffers.get(0));

      return String.format(
          "new MmuGuard(%s)",
          ControlFlowBuilder.defaultBufferAccess(
              bufferEvent.first.first,
              bufferEvent.second,
              bufferEvent.first.second));
    }

    throw new UnsupportedOperationException(
        "Guards for complex conditions that include several condition types " +
        "are not supported: " + cond);
  }

  private static boolean isEquality(final Node node) {
    InvariantChecks.checkNotNull(node);

    if (node.getKind() == Node.Kind.OPERATION) {
      final NodeOperation op = (NodeOperation) node;
      return op.getOperationId() == StandardOperation.EQ ||
             op.getOperationId() == StandardOperation.NOTEQ;
    }

    return false;
  }

  private static boolean isSegmentAccess(final Node node) {
    InvariantChecks.checkNotNull(node);

    if (node.getKind() == Node.Kind.VARIABLE &&
        node.getUserData() instanceof AttributeRef) {
      final AttributeRef attrRef = (AttributeRef) node.getUserData();
      return attrRef.getTarget() instanceof Segment;
    }

    if (node.getKind() == Node.Kind.OPERATION) {
      final NodeOperation op = (NodeOperation) node;
      return op.getOperationId() == StandardOperation.NOT &&
             isSegmentAccess(op.getOperand(0));
    }

    return false;
  }

  private static boolean isBufferEvent(final Node node) {
    InvariantChecks.checkNotNull(node);

    if (node.getKind() == Node.Kind.VARIABLE &&
        node.getUserData() instanceof AttributeRef) {
      final AttributeRef attrRef = (AttributeRef) node.getUserData();
      return attrRef.getTarget() instanceof Buffer;
    }

    if (node.getKind() == Node.Kind.OPERATION) {
      final NodeOperation op = (NodeOperation) node;
      return op.getOperationId() == StandardOperation.NOT &&
             isBufferEvent(op.getOperand(0));
    }

    return false;
  }

  private static boolean isBooleanVariable(final Node node) {
    InvariantChecks.checkNotNull(node);
    return node.getKind() == Node.Kind.VARIABLE &&
           node.isType(DataTypeId.LOGIC_BOOLEAN);
  }
  
  private static boolean isNegatedBooleanVariable(final Node node) {
    if (node.getKind() != Node.Kind.OPERATION) {
      return false;
    }

    final NodeOperation op = (NodeOperation) node;
    return op.getOperationId() == StandardOperation.NOT && 
           isBooleanVariable(op.getOperand(0));
  }

  private Pair<String, Boolean> extractSegmentInfo(final Node node) {
    InvariantChecks.checkNotNull(node);

    if (node.getKind() == Node.Kind.VARIABLE) {
      final AttributeRef attrRef = (AttributeRef) node.getUserData();
      final Segment segment = (Segment) attrRef.getTarget();
      return new Pair<>(segment.getId(), true);
    }

    if (node.getKind() == Node.Kind.OPERATION) {
      final NodeOperation op = (NodeOperation) node;
      InvariantChecks.checkTrue(op.getOperationId() == StandardOperation.NOT);

      final AttributeRef attrRef = (AttributeRef) op.getOperand(0).getUserData();
      final Segment segment = (Segment) attrRef.getTarget();
      return new Pair<>(segment.getId(), false);
    }

    throw new IllegalArgumentException("Illegal segment access expression: " + node);
  }

  private Pair<Pair<String, String>, BufferAccessEvent> extractBufferEventInfo(final Node node) {
    InvariantChecks.checkNotNull(node);

    if (node.getKind() == Node.Kind.VARIABLE) {
      final AttributeRef attrRef = (AttributeRef) node.getUserData();
      final String address = Utils.getVariableName(context, attrRef.getAddressArgValue().toString());
      final Buffer buffer = (Buffer) attrRef.getTarget();
      return new Pair<>(new Pair<>(buffer.getId(), address), BufferAccessEvent.HIT);
    }

    if (node.getKind() == Node.Kind.OPERATION) {
      final NodeOperation op = (NodeOperation) node;
      InvariantChecks.checkTrue(op.getOperationId() == StandardOperation.NOT);

      final AttributeRef attrRef = (AttributeRef) op.getOperand(0).getUserData();
      final String address = Utils.getVariableName(context, attrRef.getAddressArgValue().toString());
      final Buffer buffer = (Buffer) attrRef.getTarget();
      return new Pair<>(new Pair<>(buffer.getId(), address), BufferAccessEvent.MISS);
    }

    throw new IllegalArgumentException("Illegal buffer event expression: " + node);
  }

  private List<String> extractEqualityConditionAtoms(final List<Node> nodes) {
    InvariantChecks.checkNotEmpty(nodes);

    final List<String> result = new ArrayList<>();
    for (final Node node : nodes) {
      final Equality equality = Equality.newEquality(node);

      InvariantChecks.checkFalse(
          equality.isStruct(),
          "Comparing multi-field variables is not allowed in compound equalities."
          );

      final String conditionAtom = Utils.toString(ir, context, node);
      result.add(conditionAtom);
    }

    return result;
  }

  private static final class Equality {
    private final Atom lhs;
    private final Atom rhs;
    private final boolean negated;

    public static Equality newEquality(final Node expr) {
      InvariantChecks.checkNotNull(expr);
      InvariantChecks.checkTrue(expr.getKind() == Node.Kind.OPERATION);

      final NodeOperation op = (NodeOperation) expr;
      InvariantChecks.checkTrue(
          op.getOperationId() == StandardOperation.EQ ||
          op.getOperationId() == StandardOperation.NOTEQ);

      final Atom lhs = AtomExtractor.extract(op.getOperand(0));
      final Atom rhs = AtomExtractor.extract(op.getOperand(1));
      final boolean negated = op.getOperationId() == StandardOperation.NOTEQ;

      if (Atom.Kind.VALUE == lhs.getKind() && Atom.Kind.VALUE == rhs.getKind()) {
        throw new IllegalArgumentException(String.format(
            "Both sides of an equality expression are constants: %s = %s",
            op.getOperand(0),
            op.getOperand(1))
            );
      }

      if (Atom.Kind.VALUE == rhs.getKind()) {
        return new Equality(lhs, rhs, negated);
      } else {
        return new Equality(rhs, lhs, negated);
      }
    }

    private Equality(final Atom lhs, final Atom rhs, final boolean negated) {
      InvariantChecks.checkNotNull(lhs);
      InvariantChecks.checkNotNull(rhs);

      this.lhs = lhs;
      this.rhs = rhs;
      this.negated = negated;
    }

    public Atom getLhs() {
      return lhs;
    }

    public Atom getRhs() {
      return rhs;
    }

    public String getOperationText() {
      return negated ? "neq" : "eq";
    }

    public boolean isStruct() {
      return Atom.Kind.GROUP == lhs.getKind() &&
             Atom.Kind.GROUP == rhs.getKind();
    }
  }
}
