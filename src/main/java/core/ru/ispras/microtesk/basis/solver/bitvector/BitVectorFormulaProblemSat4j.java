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
import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.FortressUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link BitVectorFormulaProblemSat4j} represents a bit-vector problem.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BitVectorFormulaProblemSat4j extends BitVectorFormulaBuilder {
  /** Using directly ISolver instead of Sat4jFormula.Builder slows down performance. */
  private final Sat4jFormula.Builder builder;

  /** Contains the indices of the variables. */
  private final Map<Variable, Integer> indices;
  private int index;

  /** Contains the used/unused bits of the variables. */
  private final Map<Variable, BitVector> masks;

  public BitVectorFormulaProblemSat4j() {
    this.builder = new Sat4jFormula.Builder();
    this.indices = new LinkedHashMap<>();
    // Variable identifier should be positive.
    this.index = 1;
    this.masks = new LinkedHashMap<>();
  }

  public BitVectorFormulaProblemSat4j(final BitVectorFormulaProblemSat4j r) {
    this.builder = new Sat4jFormula.Builder(r.builder);
    this.indices = new LinkedHashMap<>(r.indices);
    this.index = r.index;
    this.masks = new LinkedHashMap<>(r.masks);
  }

  public Map<Variable, Integer> getIndices() {
    return indices;
  }

  public Map<Variable, BitVector> getMasks() {
    return masks;
  }

  public Sat4jFormula getFormula() {
    return builder.build();
  }

  private final IntSupplier newIndex = new IntSupplier() {
    @Override
    public int getAsInt() {
      return index++;
    }
  };

  @Override
  public void addFormula(final Node formula) {
    Logger.debug("Add formula: %s", formula);

    handleConstants(formula);

    final NodeOperation operation = (NodeOperation) formula;
    final Enum<?> operationId = operation.getOperationId();

    if (operationId == StandardOperation.EQ || operationId == StandardOperation.NOTEQ) {
      handleEq(operation);
    } else if (operationId == StandardOperation.AND) {
      handleAnd(operation);
    } else if (operationId == StandardOperation.OR) {
      handleOr(operation);
    }
  }

  private CnfEncoder.Operand getOperand(final Data data) {
    return new CnfEncoder.Operand(FortressUtils.getInteger(data));
  }

  private CnfEncoder.Operand getOperand(final Node node) {
    // A bit-vector variable or an extract.
    if (FortressUtils.getVariable(node) != null) {
      final int index = getVarIndex(node);
      final int size = FortressUtils.getBitSize(node);
      final int lower = FortressUtils.getLowerBit(node);

      return new CnfEncoder.Operand(index + lower, true, size);
    }

    // A bit-vector or integer value.
    if (node.getKind() == Node.Kind.VALUE) {
      return new CnfEncoder.Operand(FortressUtils.getInteger(node));
    }

    InvariantChecks.checkTrue(false, "Cannot encode the node");
    return null;
  }

  private CnfEncoder.Operand[] getOperands(final Node... nodes) {
    final CnfEncoder.Operand[] operands = new CnfEncoder.Operand[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      operands[i] = getOperand(nodes[i]);
    }

    return operands;
  }

  private void handleConstants(final Node node) {
    final ExprTreeWalker walker = new ExprTreeWalker(
        new ExprTreeVisitorDefault() {
          @Override
          public void onVariable(final NodeVariable nodeVariable) {
            final int preIndex = index;
            final int varIndex = getVarIndex(nodeVariable);

            // If the variable is new.
            if (varIndex >= preIndex) {
              final Variable variable = nodeVariable.getVariable();

              if (variable.hasValue()) {
                setUsedBits(variable);

                // Generate n unit clauses (c[i] ? x[i] : ~x[i]).
                builder.addAllClauses(
                    CnfEncoder.EQ_CONST.encode(
                        new CnfEncoder.Operand[] {
                            getOperand(nodeVariable),
                            getOperand(variable.getData())
                        }, newIndex));
              }
            }
          }
        });

    walker.visit(node);
  }

  private void handleEq(final NodeOperation equation) {
    final Node lhs = FortressUtils.getVariable(equation.getOperand(0)) != null
        ? equation.getOperand(0)
        : equation.getOperand(1);

    final Node rhs = FortressUtils.getVariable(equation.getOperand(0)) != null
        ? equation.getOperand(1)
        : equation.getOperand(0);

    Logger.debug("Handle equation: %s", equation);

    setUsedBits(lhs);
    setUsedBits(rhs);

    if (equation.getOperationId() == StandardOperation.EQ) {
      builder.addAllClauses(CnfEncoder.EQ.encode(getOperands(lhs, rhs), newIndex));
    } else {
      builder.addAllClauses(CnfEncoder.NEQ.encode(getOperands(lhs, rhs), newIndex));
    }
  }

  private void handleAnd(final NodeOperation operation) {
    for (final Node operand : operation.getOperands()) {
      final NodeOperation clause = (NodeOperation) operand;
      final Enum<?> clauseId = clause.getOperationId();

      if (clauseId == StandardOperation.EQ || clauseId == StandardOperation.NOTEQ) {
        handleEq(clause);
      } else if (clauseId == StandardOperation.AND) {
        handleAnd(clause);
      } else if (clauseId == StandardOperation.OR) {
        handleOr(clause);
      }
    }
  }

  private void handleOr(final NodeOperation operation) {
    int ej = index;

    builder.addClause(CnfEncoder.newClause(operation.getOperandCount(), newIndex));

    for (final Node operand : operation.getOperands()) {
      final NodeOperation equation = (NodeOperation) operand;

      InvariantChecks.checkTrue(
          equation.getOperationId() == StandardOperation.EQ
          || equation.getOperationId() == StandardOperation.NOTEQ);

      final Node lhs = FortressUtils.getVariable(equation.getOperand(0)) != null
          ? equation.getOperand(0)
          : equation.getOperand(1);

      final Node rhs = FortressUtils.getVariable(equation.getOperand(0)) != null
          ? equation.getOperand(1)
          : equation.getOperand(0);

      setUsedBits(lhs);
      setUsedBits(rhs);

      if (equation.getOperationId() == StandardOperation.EQ) {
        builder.addAllClauses(CnfEncoder.EQ.encode(getOperands(lhs, rhs), ej, newIndex));
      } else {
        builder.addAllClauses(CnfEncoder.NEQ.encode(getOperands(lhs, rhs), ej, newIndex));
      }

      ej++;
    } // for equation.
  }

  private int getVarIndex(final Node node) {
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

  private BitVector getVarMask(final Variable variable) {
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

    final BitVector mask = getVarMask(variable);
    for (int i = FortressUtils.getLowerBit(node); i <= FortressUtils.getUpperBit(node); i++) {
      mask.setBit(i, true);
    }
  }

  private void setUsedBits(final Variable variable) {
    final BitVector mask = getVarMask(variable);
    mask.setAll();
  }

  @Override
  public BitVectorFormulaProblemSat4j clone() {
    return new BitVectorFormulaProblemSat4j(this);
  }
}
