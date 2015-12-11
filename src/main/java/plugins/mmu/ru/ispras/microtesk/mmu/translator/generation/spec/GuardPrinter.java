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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Constant;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Variable;

final class GuardPrinter {
  private final Ir ir;
  private final String context;

  private final Condition condTrue;
  private final Condition condFalse;

  public GuardPrinter(final Ir ir, final String context, final Node expr) {
    checkNotNull(ir);
    checkNotNull(context);
    checkNotNull(expr);

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

        final Node equality = new NodeOperation(
            StandardOperation.NOTEQ,
            variable,
            NodeValue.newBitVector(BitVector.FALSE)
            );

        equalities.add(equality);
      } else if (isNegatedBooleanVariable(atom)) {
        final NodeOperation op = (NodeOperation) atom;
        final NodeVariable var = (NodeVariable) op.getOperand(0);

        final Node variable = new NodeVariable(var.getName(), DataType.BIT_VECTOR(1));
        variable.setUserData(var.getUserData());

        final Node equality = new NodeOperation(
            StandardOperation.EQ,
            variable,
            NodeValue.newBitVector(BitVector.FALSE)
            );

        equalities.add(equality);
      } else {
        throw new IllegalStateException("Illegal atomic condition: " + atom);
      }
    }

    InvariantChecks.checkFalse(
        equalities.isEmpty() && segments.isEmpty() && buffers.isEmpty());

    if (!equalities.isEmpty() && segments.isEmpty() && buffers.isEmpty()) {
      if (equalities.size() == 1) {
        final String condition = extractEqualityCondition(equalities.get(0));
        return String.format("new MmuGuard(%s)", condition);
      }

      final List<String> conditionAtoms = extractEqualityConditionAtoms(equalities);
      InvariantChecks.checkNotEmpty(conditionAtoms);

      if (conditionAtoms.size() == 1) {
        return String.format("new MmuGuard(%s)", conditionAtoms.get(0));
      }

      final String operation =
          cond.getType() == Condition.Type.AND ? "and" : "or";

      return String.format("new MmuGuard(MmuCondition.%s(%s))",
          operation, Utils.toString(conditionAtoms, ", "));
    }

    if (equalities.isEmpty() && !segments.isEmpty() && buffers.isEmpty()) {
      InvariantChecks.checkTrue(segments.size() == 1, "One segment event is allowed.");

      final List<String> segmentIds = extractSegmentIds(segments.get(0));
      final String segmentIdsText = Utils.toString(segmentIds, ".get(), ") + ".get()";

      return String.format(
          "new MmuGuard(null, Arrays.<MmuSegment>asList(%s))", segmentIdsText);
    }

    if (equalities.isEmpty() && segments.isEmpty() && !buffers.isEmpty()) {
      InvariantChecks.checkTrue(buffers.size() == 1, "One buffer event is allowed.");
      final Pair<String, String> bufferEvent = extractBufferEventInfo(buffers.get(0));

      return String.format(
          "new MmuGuard(%s.get(), %s)", bufferEvent.first, bufferEvent.second);
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

  private List<String> extractSegmentIds(final Node node) {
    InvariantChecks.checkNotNull(node);

    if (node.getKind() == Node.Kind.VARIABLE) {
      final AttributeRef attrRef = (AttributeRef) node.getUserData();
      final Segment segment = (Segment) attrRef.getTarget();
      return Collections.singletonList(segment.getId());
    }

    if (node.getKind() == Node.Kind.OPERATION) {
      final NodeOperation op = (NodeOperation) node;
      InvariantChecks.checkTrue(op.getOperationId() == StandardOperation.NOT);

      final AttributeRef attrRef = (AttributeRef) op.getOperand(0).getUserData();
      final Segment segment = (Segment) attrRef.getTarget();

      final Set<String> allSegmentIds = new HashSet<>(ir.getSegments().keySet());
      allSegmentIds.remove(segment.getId());

      return new ArrayList<>(allSegmentIds);
    }

    throw new IllegalArgumentException("Illegal segment access expression: " + node);
  }

  private Pair<String, String> extractBufferEventInfo(final Node node) {
    InvariantChecks.checkNotNull(node);

    if (node.getKind() == Node.Kind.VARIABLE) {
      final AttributeRef attrRef = (AttributeRef) node.getUserData();
      final Buffer buffer = (Buffer) attrRef.getTarget();
      return new Pair<>(buffer.getId(), "BufferAccessEvent.HIT");
    }

    if (node.getKind() == Node.Kind.OPERATION) {
      final NodeOperation op = (NodeOperation) node;
      InvariantChecks.checkTrue(op.getOperationId() == StandardOperation.NOT);

      final AttributeRef attrRef = (AttributeRef) op.getOperand(0).getUserData();
      final Buffer buffer = (Buffer) attrRef.getTarget();
      return new Pair<>(buffer.getId(), "BufferAccessEvent.MISS");
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

      final String lhsText = toString(equality.getLhs());
      final String rhsText = toString(equality.getRhs());

      final String conditionAtom = String.format(
          "MmuConditionAtom.%s(%s, %s)", equality.getOperationText(), lhsText, rhsText);

      result.add(conditionAtom);
    }

    return result;
  }

  private String extractEqualityCondition(final Node node) {
    final Equality equality = Equality.newEquality(node);

    final String lhsText = toString(equality.getLhs());
    final String rhsText = toString(equality.getRhs());

    return String.format(
        "%s.%s(%s, %s)",
        equality.isStruct() ? "MmuCondition" : "MmuConditionAtom",
        equality.getOperationText(),
        lhsText,
        rhsText
        );
   }

  private String getVariableName(final IntegerVariable variable) {
    final String name = variable.getName();
    final Constant constant = ir.getConstants().get(name);

    if (null != constant) {
      final DataType type = constant.getVariable().getDataType();
      if (variable.getWidth() == type.getSize()) {
        return name + ".get()";
      } else {
        return String.format("%s.get(%d)", name, variable.getWidth());
      }
    }

    return Utils.getVariableName(context, name);
  }

  @SuppressWarnings("unchecked")
  private String toString(final Atom atom) {
    InvariantChecks.checkNotNull(atom);

    final Object object = atom.getObject();
    switch (atom.getKind()) {
      case VALUE:
        return Utils.toString((BigInteger) object);

      case VARIABLE:
        return getVariableName((IntegerVariable) object);

      case FIELD: {
        final IntegerField field = (IntegerField) object;
        final IntegerVariable variable = field.getVariable();
        return String.format("%s.field(%d, %d)",
            getVariableName(variable),
            field.getLoIndex(),
            field.getHiIndex()
            );
      }

      case GROUP:
        return Utils.getVariableName(context, ((Variable) object).getName());

      case CONCAT:
        return Utils.toMmuExpressionText(context, (List<IntegerField>) object);

      default:
        throw new IllegalStateException("Unsupported atom kind: " + atom.getKind());
    }
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
