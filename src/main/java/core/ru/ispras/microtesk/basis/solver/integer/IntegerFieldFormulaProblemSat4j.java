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

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerFormulaProblem} represents an integer problem.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFieldFormulaProblemSat4j extends IntegerFieldFormulaProblem {
  /** Stores a SAT solver instance, which also represents a constraint to be solved. */
  private final ISolver solver;

  /** Contains variables' identifiers (indices). */
  private final Map<IntegerVariable, Integer> indices;
  private int index = 1;

  private boolean isInitialized = true;
  private boolean isContradiction = false;

  public IntegerFieldFormulaProblemSat4j() {
    this.solver = Sat4jUtils.getSolver();
    this.indices = new LinkedHashMap<>();
  }

  public IntegerFieldFormulaProblemSat4j(final IntegerFieldFormulaProblemSat4j r) {
    super(r);

    // Unfortunately, a SAT4j solver cannot be cloned.
    this.solver = Sat4jUtils.getSolver();
    this.isInitialized = false;

    this.indices = new LinkedHashMap<>();
    this.index = 1;

    this.isContradiction = r.isContradiction;
  }

  public Map<IntegerVariable, Integer> getIndices() {
    return indices;
  }

  public ISolver getSolver() {
    return solver;
  }

  public boolean isContradiction() {
    return isContradiction;
  }

  @Override
  public void addClause(final IntegerClause<IntegerField> newClause) {
    InvariantChecks.checkNotNull(newClause);

    if (isContradiction) {
      return;
    }

    if (!isInitialized) {
      for (final IntegerClause<IntegerField> oldClause : builder.getClauses()) {
        addClauseToSolver(oldClause);
      }

      isInitialized = true;
    }

    builder.addClause(newClause);
    addClauseToSolver(newClause);
  }

  private void addClauseToSolver(final IntegerClause<IntegerField> clause) {
    // Perform bit blasting.
    try {
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
              // Generate n clauses (c[i] ? x[i] : ~x[i]).
              solver.addAllClauses(
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

          if (equation.equal) {
            if (equation.val != null) {
              // Equality x == c.
              solver.addAllClauses(
                  Sat4jUtils.encodeVarEqualConst(equation.lhs, x, equation.val));
            } else {
              // Equality x == y.
              solver.addAllClauses(
                  Sat4jUtils.encodeVarEqualVar(equation.lhs, x, equation.rhs, y));
            }
          } else {
            if (equation.val != null) {
              // Inequality x != c.
              solver.addAllClauses(
                  Sat4jUtils.encodeVarNotEqualConst(equation.lhs, x, equation.val));
            } else {
              // Inequality x != y.
              solver.addAllClauses(
                  Sat4jUtils.encodeVarNotEqualVar(equation.lhs, x, equation.rhs, y, index));

              index += 2 * n;
            }
          }
        } // for equation.
      } else {
        // Handle an OR-clause.
        int ej = index;

        solver.addClause(Sat4jUtils.createClause(index, clause.size()));
        index += clause.size();

        for (final IntegerEquation<IntegerField> equation : clause.getEquations()) {
          final int n = equation.lhs.getWidth();
          final int x = getVarIndex(equation.lhs);
          final int y = getVarIndex(equation.rhs);

          if (equation.equal) {
            if (equation.val != null) {
              // Equality x == c.
              solver.addAllClauses(
                  Sat4jUtils.encodeVarEqualConst(ej, equation.lhs, x, equation.val));
            } else {
              // Equality x == y.
              solver.addAllClauses(
                  Sat4jUtils.encodeVarEqualVar(ej, equation.lhs, x, equation.rhs, y, index));

              index += 2 * n;
            }
          } else {
            if (equation.val != null) {
              // Inequality x != c.
              solver.addAllClauses(
                  Sat4jUtils.encodeVarNotEqualConst(ej, equation.lhs, x, equation.val));
            } else {
              // Inequality x != y.
              solver.addAllClauses(
                  Sat4jUtils.encodeVarNotEqualVar(ej, equation.lhs, x, equation.rhs, y, index));

              index += 2 * n;
            }
          }

          ej++;
        } // for equation.
      } // if clause type.
    } catch (final ContradictionException e) {
      isContradiction = true;
    }
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
}
