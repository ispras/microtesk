/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.basis.solver.integer;

import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.FortressUtils;

/**
 * {@link NodeOperationFormulaProblemSat4j} represents an integer problem.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFormulaProblemSat4j extends IntegerFormulaBuilder {
  /** Using directly ISolver instead of Sat4jFormula.Builder slows down performance. */
  private final Sat4jFormula.Builder builder;

  /** Contains the indices of the variables. */
  private final Map<Variable, Integer> indices;
  private int index;

  /** Contains the used/unused bits of the variables. */
  private final Map<Variable, BitVector> masks;

  public IntegerFormulaProblemSat4j() {
    this.builder = new Sat4jFormula.Builder();
    this.indices = new LinkedHashMap<>();
    this.index = 1;
    this.masks = new LinkedHashMap<>();
  }

  public IntegerFormulaProblemSat4j(final IntegerFormulaProblemSat4j r) {
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

  @Override
  public void addFormula(final Node formula) {
    final NodeOperation operation = (NodeOperation) formula;
    InvariantChecks.checkTrue(
           operation.getOperationId() == StandardOperation.AND
        || operation.getOperationId() == StandardOperation.OR);

    // Handle constants.
    for (final Node operand : operation.getOperands()) {
      final NodeOperation equation = (NodeOperation) operand;
      InvariantChecks.checkTrue(
          equation.getOperationId() == StandardOperation.EQ
       || equation.getOperationId() == StandardOperation.NOTEQ);

      final Node lhs = equation.getOperand(1);
      final Node rhs = equation.getOperand(2);

      final Node[] fields = new Node[] { lhs, rhs };

      for (final Node field : fields) {
        final int i = index;
        final int x = getVarIndex(field);

        // If the variable is new.
        if (x >= i) {
          final Variable variable = FortressUtils.getVariable(field);

          if (variable.hasValue()) {
            setUsedBits(variable);

            // Generate n clauses (c[i] ? x[i] : ~x[i]).
            builder.addAllClauses(
                Sat4jUtils.encodeVarEqualConst(
                    FortressUtils.makeNodeVariable(variable), x, variable.getData().getInteger()));
          }
        }
      }
    }

    // Handle equations.
    if (operation.getOperationId() == StandardOperation.AND || operation.getOperandCount() == 1) {
      // Handle an AND-clause.
      for (final Node operand : operation.getOperands()) {
        final NodeOperation equation = (NodeOperation) operand;

        final Node lhs = equation.getOperand(1);
        final Node rhs = equation.getOperand(2);

        final int n = FortressUtils.getBitSize(lhs);
        final int x = getVarIndex(lhs);
        final int y = getVarIndex(rhs);

        setUsedBits(lhs);
        setUsedBits(rhs);

        if (equation.getOperationId() == StandardOperation.EQ) {
          if (rhs.getKind() == Node.Kind.VALUE) {
            final NodeValue value = (NodeValue) rhs;

            // Equality x == c.
            builder.addAllClauses(
                Sat4jUtils.encodeVarEqualConst(lhs, x, value.getInteger()));
          } else {
            // Equality x == y.
            builder.addAllClauses(
                Sat4jUtils.encodeVarEqualVar(lhs, x, rhs, y));
          }
        } else {
          if (rhs.getKind() == Node.Kind.VALUE) {
            final NodeValue value = (NodeValue) rhs;

            // Inequality x != c.
            builder.addAllClauses(
                Sat4jUtils.encodeVarNotEqualConst(lhs, x, value.getInteger()));
          } else {
            // Inequality x != y.
            builder.addAllClauses(
                Sat4jUtils.encodeVarNotEqualVar(lhs, x, rhs, y, index));

            index += 2 * n;
          }
        }
      } // for equation.
    } else {
      // Handle an OR-clause.
      int ej = index;

      builder.addClause(Sat4jUtils.createClause(index, operation.getOperandCount()));
      index += operation.getOperandCount();

      for (final Node operand : operation.getOperands()) {
        final NodeOperation equation = (NodeOperation) operand;

        final Node lhs = equation.getOperand(1);
        final Node rhs = equation.getOperand(2);

        final int n = FortressUtils.getBitSize(lhs);
        final int x = getVarIndex(lhs);
        final int y = getVarIndex(rhs);

        setUsedBits(lhs);
        setUsedBits(rhs);

        if (equation.getOperationId() == StandardOperation.EQ) {
          if (rhs.getKind() == Node.Kind.VALUE) {
            final NodeValue value = (NodeValue) rhs;

            // Equality x == c.
            builder.addAllClauses(
                Sat4jUtils.encodeVarEqualConst(ej, lhs, x, value.getInteger()));
          } else {
            // Equality x == y.
            builder.addAllClauses(
                Sat4jUtils.encodeVarEqualVar(ej, lhs, x, rhs, y, index));

            index += 2 * n;
          }
        } else {
          if (rhs.getKind() == Node.Kind.VALUE) {
            final NodeValue value = (NodeValue) rhs;

            // Inequality x != c.
            builder.addAllClauses(
                Sat4jUtils.encodeVarNotEqualConst(ej, lhs, x, value.getInteger()));
          } else {
            // Inequality x != y.
            builder.addAllClauses(
                Sat4jUtils.encodeVarNotEqualVar(ej, lhs, x, rhs, y, index));

            index += 2 * n;
          }
        }

        ej++;
      } // for equation.
    } // if clause type.
  }

  private int getVarIndex(final Node node) {
    final Variable variable = FortressUtils.getVariable(node);

    if (variable == null) {
      return -1;
    }

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
      masks.put(variable, mask = BitVector.newEmpty(variable.getType().getSize()));
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
  public IntegerFormulaProblemSat4j clone() {
    return new IntegerFormulaProblemSat4j(this);
  }
}
