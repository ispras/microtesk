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

import java.util.function.IntSupplier;
import org.sat4j.specs.IProblem;
import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.utils.FortressUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link CoderSat4J} implements a SAT4J constraint/solution encoder/decoder.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class CoderSat4J implements Coder<Sat4jFormula, IProblem> {
  /** Using directly ISolver instead of Sat4jFormula.Builder slows down performance. */
  private final Sat4jFormula.Builder builder;

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
  private final VariableInitializer initializer;

  public CoderSat4J(final VariableInitializer initializer) {
    InvariantChecks.checkNotNull(initializer);

    this.builder = new Sat4jFormula.Builder();
    this.indices = new LinkedHashMap<>();
    this.masks = new LinkedHashMap<>();
    // In SAT4J, a variable index should be positive:
    // x and ~x are mapped to +index and -index respectively.
    this.index = 1;
    this.initializer = initializer;
  }

  public CoderSat4J(final CoderSat4J other) {
    InvariantChecks.checkNotNull(other);

    this.builder = new Sat4jFormula.Builder(other.builder);
    this.indices = new LinkedHashMap<>(other.indices);
    this.masks = new LinkedHashMap<>(other.masks);
    this.index = other.index;
    this.initializer = other.initializer;
  }

  @Override
  public void addNode(final Node node) {
    encodeConstants(node);
    encodeOperation((NodeOperation) node);
  }

  @Override
  public Sat4jFormula encode() {
    return builder.build();
  }

  @Override
  public Map<Variable, BitVector> decode(final IProblem encoded) {
    final Map<Variable, BitVector> decoded = new LinkedHashMap<>();

    // Decode the solution.
    for (final Map.Entry<Variable, Integer> entry : indices.entrySet()) {
      final Variable variable = entry.getKey();
      final int baseIndex = entry.getValue();

      final BitVector value = BitVector.newEmpty(variable.getType().getSize());
      for (int i = 0; i < variable.getType().getSize(); i++) {
        value.setBit(i, encoded.model(baseIndex + i));
      }

      decoded.put(variable, value);
    }

    // Initialize unused fields of the variables.
    for (final Map.Entry<Variable, BitVector> entry : decoded.entrySet()) {
      final Variable variable = entry.getKey();
      final BitVector mask = masks.get(variable);

      BitVector value = entry.getValue();

      int lowUnusedFieldIndex = -1;

      for (int i = 0; i < mask.getBitSize(); i++) {
        if (!mask.getBit(i)) {
          if (lowUnusedFieldIndex == -1) {
            lowUnusedFieldIndex = i;
          }
        } else {
          if (lowUnusedFieldIndex != -1) {
            final BitVector fieldValue = initializer.getValue(i - lowUnusedFieldIndex);

            value.field(lowUnusedFieldIndex, i - 1).assign(fieldValue);
            lowUnusedFieldIndex = -1;
          }
        }
      }

      if (lowUnusedFieldIndex != -1) {
        final BitVector fieldValue = initializer.getValue(mask.getBitSize() - lowUnusedFieldIndex);
        value.field(lowUnusedFieldIndex, mask.getBitSize() - 1).assign(fieldValue);
      }

      decoded.put(variable, value);
    }

    return decoded;
  }

  @Override
  public CoderSat4J clone() {
    return new CoderSat4J(this);
  }

  private BitBlaster.Operand getOperand(final Data data) {
    return new BitBlaster.Operand(FortressUtils.getInteger(data));
  }

  private BitBlaster.Operand getOperand(final Node node) {
    // A bit-vector variable or an extract.
    if (FortressUtils.getVariable(node) != null) {
      final int index = getVariableIndex(node);
      final int size = FortressUtils.getBitSize(node);
      final int lower = FortressUtils.getLowerBit(node);

      return new BitBlaster.Operand(index + lower, true, size);
    }

    // A bit-vector or integer value.
    if (node.getKind() == Node.Kind.VALUE) {
      return new BitBlaster.Operand(FortressUtils.getInteger(node));
    }

    InvariantChecks.checkTrue(false, "Cannot encode the node");
    return null;
  }

  private BitBlaster.Operand[] getOperands(final Node... nodes) {
    final BitBlaster.Operand[] operands = new BitBlaster.Operand[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      operands[i] = getOperand(nodes[i]);
    }

    return operands;
  }

  private void encodeConstants(final Node node) {
    final ExprTreeWalker walker = new ExprTreeWalker(
        new ExprTreeVisitorDefault() {
          @Override
          public void onVariable(final NodeVariable nodeVariable) {
            final int preIndex = index;
            final int varIndex = getVariableIndex(nodeVariable);

            // If the variable is new.
            if (varIndex >= preIndex) {
              final Variable variable = nodeVariable.getVariable();

              if (variable.hasValue()) {
                setUsedBits(variable);

                // Generate n unit clauses (c[i] ? x[i] : ~x[i]).
                builder.addAllClauses(
                    BitBlaster.EQ_CONST.encode(
                        new BitBlaster.Operand[] {
                            getOperand(nodeVariable),
                            getOperand(variable.getData())
                        }, newIndex));
              }
            }
          }
        });

    walker.visit(node);
  }

  private void encodeOperation(final NodeOperation node) {
    switch ((StandardOperation) node.getOperationId()) {
      case EQ:
      case NOTEQ:
        encodeRelation(node);
        break;
      case AND:
        encodeConjunction(node);
        break;
      case OR:
        encodeDisjunction(node);
        break;
      default:
        InvariantChecks.checkTrue(false, "Cannot encode the node");
    }
  }

  private void encodeRelation(final NodeOperation node, final int flagIndex) {
    InvariantChecks.checkTrue(
        ExprUtils.isOperation(node, StandardOperation.EQ, StandardOperation.NOTEQ));

    final Node lhs = node.getOperand(0);
    final Node rhs = node.getOperand(1);

    setUsedBits(lhs);
    setUsedBits(rhs);

    if (node.getOperationId() == StandardOperation.EQ) {
      builder.addAllClauses(BitBlaster.EQ.encode(getOperands(lhs, rhs), flagIndex, newIndex));
    } else {
      builder.addAllClauses(BitBlaster.NOTEQ.encode(getOperands(lhs, rhs), flagIndex, newIndex));
    }
  }

  private void encodeRelation(final NodeOperation node) {
    encodeRelation(node, 0);
  }

  private void encodeConjunction(final NodeOperation node) {
   InvariantChecks.checkTrue(ExprUtils.isOperation(node, StandardOperation.AND));

    for (final Node operand : node.getOperands()) {
      InvariantChecks.checkTrue(ExprUtils.isOperation(operand));
      encodeOperation((NodeOperation) operand);
    }
  }

  private void encodeDisjunction(final NodeOperation node) {
    InvariantChecks.checkTrue(ExprUtils.isOperation(node, StandardOperation.OR));

    int flagIndex = index;
    builder.addClause(BitBlaster.newClause(node.getOperandCount(), newIndex));

    for (final Node operand : node.getOperands()) {
      encodeRelation((NodeOperation) operand, flagIndex);
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
}
