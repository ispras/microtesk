/*
 * Copyright 2017-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.basis.solver.bitvector;

import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.AND;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVADD;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVAND;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVCONCAT;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVNEG;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVOR;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVSGE;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVSGT;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVSLE;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVSLT;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVUGE;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVUGT;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVULE;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVULT;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.EQ;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.EQ_CONSTANT;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.FALSE;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.IMPL;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.ITE;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.NOTEQ;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.NOTEQ_CONSTANT;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.OR;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.XOR;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;
import org.sat4j.specs.IProblem;
import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.Coder;
import ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.Operand;
import ru.ispras.microtesk.utils.FortressUtils;

/**
 * {@link CoderSat4j} implements a SAT4J constraint/solution encoder/decoder.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class CoderSat4j implements Coder<Map<Variable, BitVector>> {
  /** Using directly ISolver instead of Sat4jFormula.Builder slows down performance. */
  private final FormulaSat4j.Builder builder;

  /** Maps variables to their lower bit indices. */
  private final Map<Variable, Integer> indices;
  /** Maps variables to their bit-usage masks. */
  private final Map<Variable, BitVector> masks;

  /** Index of a boolean variable to be allocated next. */
  private int index;

  /** Returns the index of a new boolean variable. */
  private final IntSupplier newIndex = new IntSupplier() {
    @Override
    public int getAsInt() {
      return index++;
    }
  };

  /** Used to fill unused variable fields. */
  private final Initializer initializer;

  public CoderSat4j(final Initializer initializer) {
    InvariantChecks.checkNotNull(initializer);

    this.builder = new FormulaSat4j.Builder();
    this.indices = new LinkedHashMap<>();
    this.masks = new LinkedHashMap<>();
    // In SAT4J, a boolean variable index should be positive:
    // x[i] and ~x[i] are mapped to +i and -i respectively.
    this.index = 1;
    this.initializer = initializer;
  }

  public CoderSat4j() {
    this(Initializer.RANDOM);
  }

  public CoderSat4j(final CoderSat4j other) {
    InvariantChecks.checkNotNull(other);

    this.builder = new FormulaSat4j.Builder(other.builder);
    this.indices = new LinkedHashMap<>(other.indices);
    this.masks = new LinkedHashMap<>(other.masks);
    this.index = other.index;
    this.initializer = other.initializer;
  }

  @Override
  public void addNode(final Node node) {
    encodeConstants(node);
    encode(node, 0, false);
  }

  @Override
  public FormulaSat4j encode() {
    return builder.build();
  }

  @Override
  public Map<Variable, BitVector> decode(final Object encoded) {
    InvariantChecks.checkTrue(encoded instanceof IProblem);

    final IProblem problem = (IProblem) encoded;
    final Map<Variable, BitVector> decoded = new LinkedHashMap<>();

    for (final Map.Entry<Variable, Integer> entry : indices.entrySet()) {
      final Variable variable = entry.getKey();
      final int index = entry.getValue();
      final BitVector mask = masks.get(variable);

      // Initialize the variable (e.g. with a random value).
      final BitVector value = initializer.getValue(variable.getType().getSize());

      // Reassign the bits used in the constraint.
      for (int i = 0; i < variable.getType().getSize(); i++) {
        if (mask.getBit(i)) {
          value.setBit(i, problem.model(index + i));
        }
      }

      decoded.put(variable, value);
    }

    return decoded;
  }

  @Override
  public CoderSat4j clone() {
    return new CoderSat4j(this);
  }

  private void encodeConstants(final Node node) {
    for (final NodeVariable lhs : ExprUtils.getVariables(node)) {
      final int preIndex = index;
      final int varIndex = getVariableIndex(lhs);

      // If the variable has not been encoded yet.
      if (varIndex >= preIndex && lhs.getVariable().hasValue()) {
        setUsedBits(lhs.getVariable());

        final BigInteger value = FortressUtils.getInteger(lhs.getVariable().getData());
        final NodeValue rhs = NodeValue.newInteger(value);

        builder.addAll(EQ_CONSTANT.encode(encodeOperands(lhs, rhs), newIndex));
      }
    }
  }

  private void encode(final Node node, int flag, final boolean negation) {
    if (!ExprUtils.isCondition(node)) {
      encodeTermAsBool(node, flag, negation);
    } else if (ExprUtils.isAtomicCondition(node)) {
      encodePredicate((NodeOperation) node, flag, negation);
    } else {
      encodeConnective((NodeOperation) node, flag, negation);
    }
  }

  private void encodeTermAsBool(final Node node, final int flag, final boolean negation) {
    final Operand operand = encodeTerm(node);

    if (operand.isValue()) {
      final Operand operands[] = new Operand[] {};
      FALSE.encode(operands, flag, newIndex, (operand.value.intValue() != 0) ^ negation);
    } else {
      final Operand operands[] = new Operand[] { operand, new Operand(BigInteger.valueOf(0)) };
      NOTEQ_CONSTANT.encode(operands, flag, newIndex, negation);
    }
  }

  private void encodePredicate(final NodeOperation node, final int flag, final boolean negation) {
    final Operand[] operands = encodeOperands(node.getOperands());

    if (ExprUtils.isOperation(node, StandardOperation.EQ)) {
      builder.addAll(EQ.encode(operands, flag, newIndex, negation));
    } else if (ExprUtils.isOperation(node, StandardOperation.NOTEQ)) {
      builder.addAll(NOTEQ.encode(operands, flag, newIndex, negation));
    } else if (ExprUtils.isOperation(node, StandardOperation.BVULE, StandardOperation.LESSEQ)) {
      builder.addAll(BVULE.encode(operands, flag, newIndex, negation));
    } else if (ExprUtils.isOperation(node, StandardOperation.BVULT, StandardOperation.LESS)) {
      builder.addAll(BVULT.encode(operands, flag, newIndex, negation));
    } else if (ExprUtils.isOperation(node, StandardOperation.BVUGE, StandardOperation.GREATEREQ)) {
      builder.addAll(BVUGE.encode(operands, flag, newIndex, negation));
    } else if (ExprUtils.isOperation(node, StandardOperation.BVUGT, StandardOperation.GREATER)) {
      builder.addAll(BVUGT.encode(operands, flag, newIndex, negation));
    } else if (ExprUtils.isOperation(node, StandardOperation.BVSLE)) {
      builder.addAll(BVSLE.encode(operands, flag, newIndex, negation));
    } else if (ExprUtils.isOperation(node, StandardOperation.BVSLT)) {
      builder.addAll(BVSLT.encode(operands, flag, newIndex, negation));
    } else if (ExprUtils.isOperation(node, StandardOperation.BVSGE)) {
      builder.addAll(BVSGE.encode(operands, flag, newIndex, negation));
    } else if (ExprUtils.isOperation(node, StandardOperation.BVSGT)) {
      builder.addAll(BVSGT.encode(operands, flag, newIndex, negation));
    } else {
      reportUnknownNode(node);
    }
  }

  private void encodeConnective(final NodeOperation node, final int flag, final boolean negation) {
    if (ExprUtils.isOperation(node, StandardOperation.NOT)) {
      encode(node.getOperand(0), flag, !negation);
    } else if (ExprUtils.isOperation(node, StandardOperation.AND)) {
      encodeConjunction(node, flag, negation, false);
    } else if (ExprUtils.isOperation(node, StandardOperation.OR)) {
      encodeDisjunction(node, flag, negation, false);
    } else if (ExprUtils.isOperation(node, StandardOperation.XOR)) {
      encodeExclusiveDisjunction(node, flag, negation);
    } else if (ExprUtils.isOperation(node, StandardOperation.IMPL)) {
      encodeImplication(node, flag, negation);
    } else if (ExprUtils.isOperation(node, StandardOperation.ITE)) {
      encodeIfThenElse(node, flag, negation);
    } else {
      reportUnknownNode(node);
    }
  }

  private Operand encodeTerm(final Node node) {
    InvariantChecks.checkFalse(ExprUtils.isCondition(node));

    // Node is a constant.
    if (ExprUtils.isValue(node)) {
      return new Operand(FortressUtils.getInteger(node));
    }

    // Node is a variable or an extract, i.e. x[h:l].
    if (FortressUtils.getVariable(node) != null) {
      final int index = getVariableIndex(node);
      final int size  = FortressUtils.getBitSize(node);
      final int lower = FortressUtils.getLowerBit(node);

      setUsedBits(node);

      return new Operand(index + lower, true, size);
    }

    // Node is an operation, i.e. x + y.
    final NodeOperation operation = (NodeOperation) node;
    final Operand[] operands = encodeOperands(1, operation.getOperands());

    // Introduce a new variable, i.e. u = x + y.
    final int size  = FortressUtils.getBitSize(node);
    final int index = getVariableIndex(size);
    operands[0] = new Operand(index, true, size);

    if (ExprUtils.isOperation(node, StandardOperation.BVNEG, StandardOperation.BVNOT)) {
      BVNEG.encode(operands, newIndex);
    } else if (ExprUtils.isOperation(node, StandardOperation.BVCONCAT)) {
      BVCONCAT.encode(operands, newIndex);
    } else if (ExprUtils.isOperation(node, StandardOperation.BVAND)) {
      BVAND.encode(operands, newIndex);
    } else if (ExprUtils.isOperation(node, StandardOperation.BVOR)) {
      BVOR.encode(operands, newIndex);
    } else if (ExprUtils.isOperation(node, StandardOperation.BVXOR)) {
      // FIXME:
    } else if (ExprUtils.isOperation(node, StandardOperation.BVNAND)) {
      // FIXME:
    } else if (ExprUtils.isOperation(node, StandardOperation.BVNOR)) {
      // FIXME:
    } else if (ExprUtils.isOperation(node, StandardOperation.BVLSHL)) {
      // FIXME:
    } else if (ExprUtils.isOperation(node, StandardOperation.BVLSHL)) {
      // FIXME:
    } else if (ExprUtils.isOperation(node, StandardOperation.BVASHL)) {
      // FIXME:
    } else if (ExprUtils.isOperation(node, StandardOperation.BVASHR)) {
      // FIXME:
    } else if (ExprUtils.isOperation(node, StandardOperation.BVROL)) {
      // FIXME:
    } else if (ExprUtils.isOperation(node, StandardOperation.BVROR)) {
      // FIXME:
    } else if (ExprUtils.isOperation(node, StandardOperation.BVADD, StandardOperation.ADD)) {
      BVADD.encode(operands, newIndex);
    } else {
      reportUnknownNode(node);
    }
    return null;
  }

  private void encodeConjunction(
      final NodeOperation node,
      final int flag,
      final boolean externalNegation,
      final boolean internalNegation) {
    // De Morgan's law: ~(x & y) == (~x | ~y).
    if (externalNegation) {
      encodeDisjunction(node, flag, false, true);
      return;
    }

    int flagIndex = flag != 0 ? index : 0;
    int flagDelta = flag != 0 ? 1 : 0;

    // f <=> (f[0] & ... & f[n-1]).
    if (flag != 0) {
      builder.addAll(AND.encode(newBoolOperands(node.getOperandCount()), flag, newIndex));
    }

    // f[i] <=> encode(operand[i])
    for (final Node operand : node.getOperands()) {
      // Another option: operand, -flagIndex, !internalNegation.
      encode(operand, flagIndex, internalNegation);
      flagIndex += flagDelta;
    }
  }

  private void encodeDisjunction(
      final NodeOperation node,
      final int flag,
      final boolean externalNegation,
      final boolean internalNegation) {
    // De Morgan's law: ~(x | y) == (~x & ~y).
    if (externalNegation) {
      encodeConjunction(node, flag, false, true);
      return;
    }

    int flagIndex = index;

    // f <=> (f[0] | ... | f[n-1]).
    builder.addAll(OR.encode(newBoolOperands(node.getOperandCount()), flag, newIndex));

    // f[i] <=> encode(operand[i])
    for (final Node operand : node.getOperands()) {
      encode(operand, flagIndex, internalNegation);
      flagIndex++;
    }
  }

  private void encodeExclusiveDisjunction(
      final NodeOperation node,
      final int flag,
      final boolean negation) {
    int flagIndex = index;

    // f <=> (f[0] ^ ... ^ f[n-1]).
    builder.addAll(XOR.encode(newBoolOperands(node.getOperandCount()), flag, newIndex, negation));

    // f[i] <=> encode(operand[i])
    for (final Node operand : node.getOperands()) {
      encode(operand, flagIndex, false);
      flagIndex++;
    }
  }

  private void encodeImplication(
      final NodeOperation node,
      final int flag,
      final boolean negation) {
    int flagIndex = index;

    // f <=> (f[0] -> f[1]).
    InvariantChecks.checkTrue(node.getOperandCount() == 2);
    builder.addAll(IMPL.encode(newBoolOperands(2), flag, newIndex, negation));

    // f[i] <=> encode(operand[i])
    for (final Node operand : node.getOperands()) {
      encode(operand, flagIndex, false);
      flagIndex++;
    }
  }

  private void encodeIfThenElse(
      final NodeOperation node,
      final int flag,
      final boolean negation) {
    int flagIndex = index;

    // f <=> (f[0] ? f[1] : f[2]).
    InvariantChecks.checkTrue(node.getOperandCount() == 3);
    builder.addAll(ITE.encode(newBoolOperands(3), flag, newIndex, negation));

    // f[i] <=> encode(operand[i])
    for (final Node operand : node.getOperands()) {
      encode(operand, flagIndex, false);
      flagIndex++;
    }
  }

  private Operand[] encodeOperands(final int extra, final List<Node> nodes) {
    final Operand[] operands = new Operand[nodes.size() + extra];
    IntStream.range(0, nodes.size()).forEach(i -> operands[i + extra] = encodeTerm(nodes.get(i)));
    return operands;
  }

  private Operand[] encodeOperands(final List<Node> nodes) {
    return encodeOperands(0, nodes);
  }

  private Operand[] encodeOperands(final Node... nodes) {
    return encodeOperands(0, Arrays.asList(nodes));
  }

  private Operand[] newBoolOperands(final int count) {
    return IntStream.range(0, count).mapToObj(
        i -> new Operand(newIndex.getAsInt(), 1)).toArray(Operand[]::new);
  }

  private int getVariableIndex(final Node node) {
    final Variable variable = FortressUtils.getVariable(node);
    InvariantChecks.checkNotNull(variable);

    final Integer oldIndex = indices.get(variable);

    if (oldIndex != null) {
      return oldIndex;
    }

    final int newIndex = getVariableIndex(variable.getType().getSize());
    indices.put(variable, newIndex);

    return newIndex;
  }

  private int getVariableIndex(final int size) {
    final int newIndex = index;
    index += size;
    return newIndex;
  }

  private BitVector getVariableMask(final Variable variable) {
    BitVector mask = masks.get(variable);

    if (mask == null) {
      masks.put(variable, mask = BitVector.newEmpty(FortressUtils.getBitSize(variable)));
    }

    return mask;
  }

  private void setUsedBits(final Node node) {
    final Variable variable = FortressUtils.getVariable(node);
    if (variable == null) {
      return;
    }

    final BitVector mask = getVariableMask(variable);
    for (int i = FortressUtils.getLowerBit(node); i <= FortressUtils.getUpperBit(node); i++) {
      mask.setBit(i, true);
    }
  }

  private void setUsedBits(final Variable variable) {
    final BitVector mask = getVariableMask(variable);
    mask.setAll();
  }

  private void reportUnknownNode(final Node node) {
    Logger.error("Encoding failed: unknown node %s", node);
    InvariantChecks.checkTrue(false);
  }
}
