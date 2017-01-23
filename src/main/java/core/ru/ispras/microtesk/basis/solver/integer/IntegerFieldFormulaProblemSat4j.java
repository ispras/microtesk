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

import ru.ispras.fortress.data.types.bitvector.BitVector;

/**
 * {@link IntegerFormulaProblem} represents an integer problem.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFieldFormulaProblemSat4j extends IntegerFormulaBuilder<IntegerField> {
  /** Using directly ISolver instead of Sat4jFormula.Builder slows down performance. */
  private final Sat4jFormula.Builder builder;

  /** Contains the indices of the variables. */
  private final Map<IntegerVariable, Integer> indices;
  private int index;

  /** Contains the used/unused bits of the variables. */
  private final Map<IntegerVariable, BitVector> masks;

  public IntegerFieldFormulaProblemSat4j() {
    this.builder = new Sat4jFormula.Builder();
    this.indices = new LinkedHashMap<>();
    this.index = 1;
    this.masks = new LinkedHashMap<>();
  }

  public IntegerFieldFormulaProblemSat4j(final IntegerFieldFormulaProblemSat4j r) {
    this.builder = new Sat4jFormula.Builder(r.builder);
    this.indices = new LinkedHashMap<>(r.indices);
    this.index = r.index;
    this.masks = new LinkedHashMap<>(r.masks);
  }

  public Map<IntegerVariable, Integer> getIndices() {
    return indices;
  }

  public Map<IntegerVariable, BitVector> getMasks() {
    return masks;
  }

  public Sat4jFormula getFormula() {
    return builder.build();
  }

  @Override
  public void addClause(final IntegerClause<IntegerField> clause) {
    // Handle constants.
    for (final IntegerEquation<IntegerField> equation : clause.getEquations()) {
      final IntegerField[] fields = new IntegerField[] { equation.lhs, equation.rhs };

      for (final IntegerField field : fields) {
        final int i = index;
        final int x = getVarIndex(field);

        // If the variable is new.
        if (x >= i) {
          final IntegerVariable variable = field.getVariable();

          if (variable.isDefined()) {
            setUsedBits(variable);

            // Generate n clauses (c[i] ? x[i] : ~x[i]).
            builder.addAllClauses(
                Sat4jUtils.encodeVarEqualConst(variable, x, variable.getValue()));
          }
        }
      }
    }

    // Handle equations.
    if (clause.getType() == IntegerClause.Type.AND || clause.size() == 1) {
      // Handle an AND-clause.
      for (final IntegerEquation<IntegerField> equation : clause.getEquations()) {
        final int n = equation.lhs.getWidth();
        final int x = getVarIndex(equation.lhs);
        final int y = getVarIndex(equation.rhs);

        setUsedBits(equation.lhs);
        setUsedBits(equation.rhs);

        if (equation.equal) {
          if (equation.val != null) {
            // Equality x == c.
            builder.addAllClauses(
                Sat4jUtils.encodeVarEqualConst(equation.lhs, x, equation.val));
          } else {
            // Equality x == y.
            builder.addAllClauses(
                Sat4jUtils.encodeVarEqualVar(equation.lhs, x, equation.rhs, y));
          }
        } else {
          if (equation.val != null) {
            // Inequality x != c.
            builder.addAllClauses(
                Sat4jUtils.encodeVarNotEqualConst(equation.lhs, x, equation.val));
          } else {
            // Inequality x != y.
            builder.addAllClauses(
                Sat4jUtils.encodeVarNotEqualVar(equation.lhs, x, equation.rhs, y, index));

            index += 2 * n;
          }
        }
      } // for equation.
    } else {
      // Handle an OR-clause.
      int ej = index;

      builder.addClause(Sat4jUtils.createClause(index, clause.size()));
      index += clause.size();

      for (final IntegerEquation<IntegerField> equation : clause.getEquations()) {
        final int n = equation.lhs.getWidth();
        final int x = getVarIndex(equation.lhs);
        final int y = getVarIndex(equation.rhs);

        setUsedBits(equation.lhs);
        setUsedBits(equation.rhs);

        if (equation.equal) {
          if (equation.val != null) {
            // Equality x == c.
            builder.addAllClauses(
                Sat4jUtils.encodeVarEqualConst(ej, equation.lhs, x, equation.val));
          } else {
            // Equality x == y.
            builder.addAllClauses(
                Sat4jUtils.encodeVarEqualVar(ej, equation.lhs, x, equation.rhs, y, index));

            index += 2 * n;
          }
        } else {
          if (equation.val != null) {
            // Inequality x != c.
            builder.addAllClauses(
                Sat4jUtils.encodeVarNotEqualConst(ej, equation.lhs, x, equation.val));
          } else {
            // Inequality x != y.
            builder.addAllClauses(
                Sat4jUtils.encodeVarNotEqualVar(ej, equation.lhs, x, equation.rhs, y, index));

            index += 2 * n;
          }
        }

        ej++;
      } // for equation.
    } // if clause type.
  }

  private int getVarIndex(final IntegerField field) {
    return field != null ? getVarIndex(field.getVariable()) : -1;
  }

  private int getVarIndex(final IntegerVariable variable) {
    final Integer oldIndex = indices.get(variable);

    if (oldIndex != null) {
      return oldIndex;
    }

    final int newIndex = index;

    indices.put(variable, newIndex);
    index += variable.getWidth();

    return newIndex;
  }

  private BitVector getVarMask(final IntegerVariable variable) {
    BitVector mask = masks.get(variable);

    if (mask == null) {
      masks.put(variable, mask = BitVector.newEmpty(variable.getWidth()));
    }

    return mask;
  }

  private void setUsedBits(final IntegerField field) {
    if (field != null) { 
      final BitVector mask = getVarMask(field.getVariable());

      for (int i = field.getLoIndex(); i <= field.getHiIndex(); i++) {
        mask.setBit(i, true);
      }
    }
  }

  public void setUsedBits(final IntegerVariable variable) {
    final BitVector mask = getVarMask(variable);
    mask.setAll();
  }

  @Override
  public IntegerFieldFormulaProblemSat4j clone() {
    return new IntegerFieldFormulaProblemSat4j(this);
  }
}
