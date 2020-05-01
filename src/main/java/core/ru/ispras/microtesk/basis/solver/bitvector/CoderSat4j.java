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

import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVUGE;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVUGT;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVULE;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.BVULT;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.EQ;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.EQ_CONST;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.FALSE;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.NOTEQ;
import static ru.ispras.microtesk.basis.solver.bitvector.BitBlaster.NOTEQ_CONST;

import java.math.BigInteger;
import java.util.LinkedHashMap;
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

        builder.addAll(EQ_CONST.encode(getOperands(lhs, rhs), newIndex));
      }
    }
  }

  private void encode(final Node node, int flag, final boolean negation) {
    if (ExprUtils.isValue(node)) {
      encodeValue((NodeValue) node, flag, negation);
    } else if (ExprUtils.isVariable(node)) {
      encodeVariable((NodeVariable) node, flag, negation);
    } else if (ExprUtils.isAtomicCondition(node)) {
      encodePredicate((NodeOperation) node, flag, negation);
    } else if (ExprUtils.isCondition(node)) {
      encodeFormula((NodeOperation) node, flag, negation);
    } else {
      encodeTerm((NodeOperation) node, flag, negation);
    }
  }

  private void encodeValue(final NodeValue node, final int flag, final boolean negation) {
    FALSE.encode(getOperands(), flag, newIndex, FortressUtils.getBoolean(node) ^ negation);
  }

  private void encodeVariable(final NodeVariable node, final int flag, final boolean negation) {
    NOTEQ_CONST.encode(getOperands(node, NodeValue.newInteger(0)), flag, newIndex, negation);
  }

  private void encodePredicate(final NodeOperation node, final int flag, final boolean negation) {
    final Node lhs = node.getOperand(0);
    final Node rhs = node.getOperand(1);

    setUsedBits(lhs);
    setUsedBits(rhs);

    final Operand[] operands = getOperands(lhs, rhs);

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
    } else {
      reportUnknownNode(node);
    }
  }

  private void encodeFormula(final NodeOperation node, final int flag, final boolean negation) {
    if (ExprUtils.isOperation(node, StandardOperation.NOT)) {
      encode(node.getOperand(0), flag, !negation);
    } else if (ExprUtils.isOperation(node, StandardOperation.AND)) {
      encodeConjunction(node, flag, negation, false);
    } else if (ExprUtils.isOperation(node, StandardOperation.OR)) {
      encodeDisjunction(node, flag, negation, false);
    } else {
      reportUnknownNode(node);
    }
  }

  private void encodeTerm(final NodeOperation node, final int flag, final boolean negation) {
    reportUnknownNode(node);
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

    int flagIndex = index;

    // f <=> (f[0] & ... & f[n-1]).
    if (flag != 0) {
      final Operand[] operands = IntStream.range(0, node.getOperandCount()).mapToObj(
          i -> new Operand(newIndex.getAsInt(), 1)).toArray(Operand[]::new);
      builder.addAll(BitBlaster.AND.encode(operands, flag, newIndex));
    }

    // f[i] <=> encode(operand[i])
    for (final Node operand : node.getOperands()) {
      // Another option: operand, -flagIndex, !internalNegation.
      encode(operand, (flag != 0 ? flagIndex : 0), internalNegation);
      flagIndex++;
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
    final Operand[] operands = IntStream.range(0, node.getOperandCount()).mapToObj(
        i -> new Operand(newIndex.getAsInt(), 1)).toArray(Operand[]::new);
    builder.addAll(BitBlaster.OR.encode(operands, flag, newIndex));

    // f[i] <=> encode(operand[i])
    for (final Node operand : node.getOperands()) {
      encode(operand, flagIndex, internalNegation);
      flagIndex++;
    }
  }

  private int getVariableIndex(final Node node) {
    final Variable variable = FortressUtils.getVariable(node);
    InvariantChecks.checkNotNull(variable);

    final Integer oldIndex = indices.get(variable);

    if (oldIndex != null) {
      return oldIndex;
    }

    final int newIndex = index;

    indices.put(variable, newIndex);
    index += variable.getType().getSize();

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

  private Operand getOperand(final Node node) {
    // A bit-vector variable or an extract.
    if (FortressUtils.getVariable(node) != null) {
      final int index = getVariableIndex(node);
      final int size = FortressUtils.getBitSize(node);
      final int lower = FortressUtils.getLowerBit(node);

      return new Operand(index + lower, true, size);
    }

    // A bit-vector or integer value.
    if (node.getKind() == Node.Kind.VALUE) {
      return new Operand(FortressUtils.getInteger(node));
    }

    reportUnknownNode(node);
    return null;
  }

  private Operand[] getOperands(final Node... nodes) {
    final Operand[] operands = new Operand[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      operands[i] = getOperand(nodes[i]);
    }

    return operands;
  }

  private void reportUnknownNode(final Node node) {
    Logger.error("Encoding failed: unknown node %s", node);
    InvariantChecks.checkTrue(false);
  }
}
