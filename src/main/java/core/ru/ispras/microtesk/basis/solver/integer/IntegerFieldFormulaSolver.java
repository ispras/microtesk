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

package ru.ispras.microtesk.basis.solver.integer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;

/**
 * {@link IntegerFieldFormulaSolver} implements an integer-field-constraints solver.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class IntegerFieldFormulaSolver implements Solver<Map<IntegerVariable, BigInteger>> {
  private static Map<IntegerVariable, List<IntegerRange>> getDisjointRanges(
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(ranges);

    final Map<IntegerVariable, List<IntegerRange>> disjointRanges = new LinkedHashMap<>();

    for (final Map.Entry<IntegerVariable, Set<IntegerRange>> entry : ranges.entrySet()) {
      final IntegerVariable var = entry.getKey();
      final List<IntegerRange> varRanges = IntegerRange.divide(entry.getValue());

      disjointRanges.put(var, varRanges);
    }

    return disjointRanges;
  }

  /** Formula (constraint) to be solved. */
  private final IntegerFormula<IntegerField> formula;
  /** Variables used in the formula. */
  private final Collection<IntegerVariable> variables;

  /** Maps a variable to the list of ranges. */
  private final Map<IntegerVariable, List<IntegerRange>> disjointRanges;
  /** Maps a variable to the list of fields (variables used in the formula). */
  private final Map<IntegerVariable, List<IntegerVariable>> varToFields;
  /** Maps a field to the range. */
  private final Map<IntegerVariable, IntegerRange> fieldToRange;

  public IntegerFieldFormulaSolver(
    final Collection<IntegerVariable> variables, final IntegerFormula<IntegerField> formula) {
    InvariantChecks.checkNotNull(variables);
    InvariantChecks.checkNotNull(formula);

    this.variables = variables;
    this.formula = formula;

    // Auxiliary data structures.
    this.disjointRanges = getDisjointRanges(getRanges(formula));
    this.varToFields = new LinkedHashMap<>();
    this.fieldToRange = new LinkedHashMap<>();

    for (final Map.Entry<IntegerVariable, List<IntegerRange>> entry : disjointRanges.entrySet()) {
      final IntegerVariable var = entry.getKey();
      final List<IntegerRange> varRanges = entry.getValue();

      final List<IntegerVariable> varFields = new ArrayList<>();
      varToFields.put(var, varFields);

      for (final IntegerRange varRange : varRanges) {
        final IntegerVariable varField = getFieldVar(var, varRange);

        varFields.add(varField);
        fieldToRange.put(varField, varRange);
      }
    }
  }

  @Override
  public SolverResult<Map<IntegerVariable, BigInteger>> solve() {
    final IntegerFormula<IntegerVariable> newFormula = getFormula(formula);

    final IntegerFormulaSolver solver = new IntegerFormulaSolver(fieldToRange.keySet(), newFormula);
    final SolverResult<Map<IntegerVariable, BigInteger>> result = solver.solve();

    if (result.getStatus() != SolverResult.Status.SAT) {
      return result;
    }

    final Map<IntegerVariable, BigInteger> newSolution = result.getResult();
    final Map<IntegerVariable, BigInteger> oldSolution = getSolution(newSolution);

    return new SolverResult<Map<IntegerVariable, BigInteger>>(oldSolution);
  }

  private List<IntegerVariable> getFieldVars(final IntegerField field) {
    InvariantChecks.checkNotNull(field);

    final List<IntegerVariable> fieldVars = new ArrayList<>();

    final IntegerVariable var = field.getVariable();
    final List<IntegerRange> varRanges = disjointRanges.get(var);

    final IntegerRange fieldRange = new IntegerRange(field.getLoIndex(), field.getHiIndex());

    for (final IntegerRange varRange : varRanges) {
      if (fieldRange.contains(varRange)) {
        final IntegerVariable fieldVar = getFieldVar(var, varRange);
        fieldVars.add(fieldVar);
      }
    }

    return fieldVars;
  }

  private IntegerVariable getFieldVar(final IntegerVariable var, final IntegerRange range) {
    InvariantChecks.checkNotNull(var);
    InvariantChecks.checkNotNull(range);

    final String name = String.format("%s$%s", var, range);
    final int width = range.size().intValue();

    return new IntegerVariable(name, width);
  }

  private Map<IntegerVariable, Set<IntegerRange>> getRanges(
      final IntegerFormula<IntegerField> formula) {
    InvariantChecks.checkNotNull(formula);

    final Map<IntegerVariable, Set<IntegerRange>> ranges = new LinkedHashMap<>();

    for (final IntegerClause<IntegerField> clause : formula.getEquationClauses()) {
      gatherRanges(ranges, clause);
    }

    return ranges;
  }

  private void gatherRanges(
      final Map<IntegerVariable, Set<IntegerRange>> ranges,
      final IntegerClause<IntegerField> clause) {
    InvariantChecks.checkNotNull(ranges);
    InvariantChecks.checkNotNull(clause);

    final List<IntegerEquation<IntegerField>> equations = clause.getEquations();
    for (final IntegerEquation<IntegerField> equation : equations) {
      gatherRanges(ranges, equation);
    }
  }

  private void gatherRanges(
      final Map<IntegerVariable, Set<IntegerRange>> ranges,
      final IntegerEquation<IntegerField> equation) {
    InvariantChecks.checkNotNull(ranges);
    InvariantChecks.checkNotNull(equation);

    final List<IntegerField> fields = new ArrayList<>();

    if (equation.lhs != null) {
      fields.add(equation.lhs);
    }
    if (equation.rhs != null) {
      fields.add(equation.rhs);
    }

    for (final IntegerField field : fields) {
      final IntegerVariable var = field.getVariable();

      Set<IntegerRange> varRanges = ranges.get(var);
      if (varRanges == null) {
        ranges.put(var, varRanges = new LinkedHashSet<>());
        varRanges.add(new IntegerRange(0, var.getWidth() - 1));
      }

      varRanges.add(new IntegerRange(field.getLoIndex(), field.getHiIndex()));
    }
  }

  private IntegerFormula<IntegerVariable> getFormula(
      final IntegerFormula<IntegerField> oldFormula) {
    InvariantChecks.checkNotNull(oldFormula);

    final IntegerFormula<IntegerVariable> newFormula = new IntegerFormula<IntegerVariable>();

    for (final IntegerClause<IntegerField> oldClause : oldFormula.getEquationClauses()) {
      newFormula.addEquationClause(getClause(oldClause));
    }

    return newFormula;
  }

  private IntegerClause<IntegerVariable> getClause(
      final IntegerClause<IntegerField> oldClause) {
    InvariantChecks.checkNotNull(oldClause);

    final IntegerClause<IntegerVariable> newClause =
        new IntegerClause<IntegerVariable>(oldClause.getType());

    for (final IntegerEquation<IntegerField> oldEquation : oldClause.getEquations()) {
      final IntegerClause<IntegerVariable> newSubClause = getClause(oldEquation);
      InvariantChecks.checkTrue(
          newSubClause.size() == 1 || newSubClause.getType() == newClause.getType());

      newClause.addEquationClause(newSubClause);
    }

    return newClause;
  }

  private IntegerClause<IntegerVariable> getClause(
      final IntegerEquation<IntegerField> oldEquation) {
    InvariantChecks.checkNotNull(oldEquation);

    return oldEquation.value ?
        getClause(oldEquation.lhs, oldEquation.val, oldEquation.equal) :
        getClause(oldEquation.lhs, oldEquation.rhs, oldEquation.equal);
  }

  private IntegerClause<IntegerVariable> getClause(
      final IntegerField oldLhs,
      final IntegerField oldRhs,
      final boolean oldEqual) {
    InvariantChecks.checkNotNull(oldLhs);
    InvariantChecks.checkNotNull(oldRhs);

    final IntegerClause<IntegerVariable> newClause =
        new IntegerClause<IntegerVariable>(
            oldEqual ? IntegerClause.Type.AND : IntegerClause.Type.OR);

    final List<IntegerVariable> lhsFields = getFieldVars(oldLhs);
    final List<IntegerVariable> rhsFields = getFieldVars(oldRhs);
    InvariantChecks.checkTrue(lhsFields.size() == rhsFields.size());

    for (int i = 0; i < lhsFields.size(); i++) {
      final IntegerVariable newLhs = lhsFields.get(i);
      final IntegerVariable newRhs = rhsFields.get(i);

      newClause.addEquation(newLhs, newRhs, oldEqual);
    }

    return newClause;
  }

  private IntegerClause<IntegerVariable> getClause(
      final IntegerField oldLhs,
      final BigInteger oldRhs,
      final boolean oldEqual) {
    InvariantChecks.checkNotNull(oldLhs);
    InvariantChecks.checkNotNull(oldRhs);

    final IntegerClause<IntegerVariable> newClause =
        new IntegerClause<IntegerVariable>(
            oldEqual ? IntegerClause.Type.AND : IntegerClause.Type.OR);

    final List<IntegerVariable> lhsFields = getFieldVars(oldLhs);

    for (int i = 0; i < lhsFields.size(); i++) {
      final IntegerVariable newLhs = lhsFields.get(i);
      final IntegerRange newRange = fieldToRange.get(newLhs);

      final int lo = newRange.getMin().intValue();
      final int hi = newRange.getMax().intValue();

      final BigInteger newRhs = BitUtils.getField(oldRhs, lo, hi);

      newClause.addEquation(newLhs, newRhs, oldEqual);
    }

    return newClause;
  }

  private Map<IntegerVariable, BigInteger> getSolution(
      final Map<IntegerVariable, BigInteger> newSolution) {
    InvariantChecks.checkNotNull(newSolution);

    final Map<IntegerVariable, BigInteger> oldSolution = new LinkedHashMap<>();

    for (final IntegerVariable variable : variables) {
      final List<IntegerVariable> fields = varToFields.get(variable);

      BigInteger value = Randomizer.get().nextBigIntegerField(variable.getWidth(), false);

      if (fields != null) {
        for (final IntegerVariable field : fields) {
          final IntegerRange fieldRange = fieldToRange.get(field);
          final BigInteger fieldValue = newSolution.get(field);

          final int lo = fieldRange.getMin().intValue();
          final int hi = fieldRange.getMax().intValue();

          value = BitUtils.setField(value, lo, hi, fieldValue);
        }
      }

      oldSolution.put(variable, value);
    }

    return oldSolution;
  }
}
