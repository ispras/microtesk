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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;

/**
 * {@link IntegerFieldFormulaSolver} implements an integer-field-constraints solver.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerFieldFormulaSolver implements Solver<Map<IntegerVariable, BigInteger>> {
  private static IntegerRange getRange(final IntegerField field) {
    InvariantChecks.checkNotNull(field);
    return new IntegerRange(field.getLoIndex(), field.getHiIndex());
  }

  private static IntegerRange getRange(final IntegerVariable variable) {
    InvariantChecks.checkNotNull(variable);
    return new IntegerRange(0, variable.getWidth() - 1);
  }

  private static Map<IntegerVariable, List<IntegerRange>> getDividedRanges(
      final Map<IntegerVariable, Collection<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(ranges);

    final Map<IntegerVariable, List<IntegerRange>> disjointRanges = new LinkedHashMap<>();

    for (final Map.Entry<IntegerVariable, Collection<IntegerRange>> entry : ranges.entrySet()) {
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

  /** Maps an original variable to the list of ranges. */
  private final Map<IntegerVariable, List<IntegerRange>> dividedRanges;

  /** Maps a variable field into the set of fields it is linked with. */
  private final Map<IntegerField, Collection<IntegerField>> linkedWith;

  /** Maps an original variable to the list of fields (variables used in the formula). */
  private final Map<IntegerVariable, Collection<IntegerVariable>> varToFields;
  /** Maps a field to the range. */
  private final Map<IntegerVariable, IntegerRange> fieldToRange;

  /** Caches field variables. */
  private final Map<String, IntegerVariable> fieldCache;

  public IntegerFieldFormulaSolver(
    final Collection<IntegerVariable> variables, final IntegerFormula<IntegerField> formula) {
    InvariantChecks.checkNotNull(variables);
    InvariantChecks.checkNotNull(formula);

    this.variables = variables;
    this.formula = formula;

    // Initialize auxiliary data structures.
    this.linkedWith = new LinkedHashMap<>();
    this.dividedRanges = getDividedRanges(formula);

    this.varToFields = new LinkedHashMap<>();
    this.fieldToRange = new LinkedHashMap<>();
    this.fieldCache = new HashMap<>();

    for (final Map.Entry<IntegerVariable, List<IntegerRange>> entry : dividedRanges.entrySet()) {
      final IntegerVariable var = entry.getKey();
      final Collection<IntegerRange> varRanges = entry.getValue();

      final Collection<IntegerVariable> varFields = new ArrayList<>();
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
    final Collection<IntegerRange> varRanges = dividedRanges.get(var);

    final IntegerRange fieldRange = getRange(field);

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

    IntegerVariable fieldVar = fieldCache.get(name);
    if (fieldVar == null) {
      fieldCache.put(name, fieldVar = new IntegerVariable(name, width));
    }

    return fieldVar;
  }

  private Map<IntegerVariable, List<IntegerRange>> getDividedRanges(
      final IntegerFormula<IntegerField> formula) {
    InvariantChecks.checkNotNull(formula);

    // Gather the ranges used in the formula.
    final Map<IntegerVariable, Collection<IntegerRange>> ranges = new LinkedHashMap<>();

    for (final IntegerClause<IntegerField> clause : formula.getEquationClauses()) {
      gatherRanges(ranges, clause);
    }

    // Construct the disjoint ranges not taking into account the links between the fields.
    // Two fields, F1 and F2, are called linked if the formula contains (F1 == F2).
    final Map<IntegerVariable, List<IntegerRange>> dividedRanges = getDividedRanges(ranges);

    // Perform further splitting taking into account the links between the fields.
    final Map<IntegerField, Collection<IntegerRange>> fieldRanges = new LinkedHashMap<>();

    for (final IntegerField field : linkedWith.keySet()) {
      final Collection<IntegerRange> all = dividedRanges.get(field.getVariable());
      final Collection<IntegerRange> selected = IntegerRange.select(all, getRange(field));

      fieldRanges.put(field, selected);
    }

    boolean divideRanges;

    do {
      divideRanges = false;

      for (final Map.Entry<IntegerField, Collection<IntegerField>> entry : linkedWith.entrySet()) {
        final IntegerField field = entry.getKey();
        final Collection<IntegerField> links = entry.getValue();

        final Collection<IntegerRange> oldDividedRanges = fieldRanges.get(field);
        InvariantChecks.checkNotNull(oldDividedRanges);

        for (final IntegerField link : links) {
          InvariantChecks.checkTrue(field.getWidth() == link.getWidth());

          final Collection<IntegerRange> linkRanges = fieldRanges.get(link);
          InvariantChecks.checkFalse(linkRanges.isEmpty());

          // Possibly, the field should be split.
          if (linkRanges.size() > 1) {
            final int offset = field.getLoIndex() - link.getLoIndex();
            final Collection<IntegerRange> newRanges = new LinkedHashSet<>(oldDividedRanges);

            for (final IntegerRange linkRange : linkRanges) {
              newRanges.add(linkRange.shift(offset));
            }

            final Collection<IntegerRange> newDividedRanges = IntegerRange.divide(newRanges);
            InvariantChecks.checkTrue(newDividedRanges.size() >= oldDividedRanges.size());

            // Definitely, the field should be split.
            if (newDividedRanges.size() > oldDividedRanges.size()) {
              fieldRanges.put(field, newDividedRanges);
              divideRanges = true;
            }
          }
        }
      } // for field-links.
    } while(divideRanges);

    // Update the variable ranges.
    for (final Map.Entry<IntegerField, Collection<IntegerRange>> entry : fieldRanges.entrySet()) {
      final IntegerField field = entry.getKey();
      final Collection<IntegerRange> newFieldRanges = entry.getValue();
      final List<IntegerRange> oldVarRanges = dividedRanges.get(field.getVariable());

      final Collection<IntegerRange> oldFieldRanges =
          IntegerRange.select(oldVarRanges, getRange(field));
      InvariantChecks.checkFalse(oldFieldRanges.isEmpty());

      final int index = oldVarRanges.indexOf(oldFieldRanges.iterator().next());

      oldVarRanges.removeAll(oldFieldRanges);
      oldVarRanges.addAll(index, newFieldRanges);
    }

    return dividedRanges;
  }

  private void gatherRanges(
      final Map<IntegerVariable, Collection<IntegerRange>> ranges,
      final IntegerClause<IntegerField> clause) {
    InvariantChecks.checkNotNull(ranges);
    InvariantChecks.checkNotNull(clause);

    final List<IntegerEquation<IntegerField>> equations = clause.getEquations();
    for (final IntegerEquation<IntegerField> equation : equations) {
      gatherRanges(ranges, equation);
    }
  }

  private void gatherRanges(
      final Map<IntegerVariable, Collection<IntegerRange>> ranges,
      final IntegerEquation<IntegerField> equation) {
    InvariantChecks.checkNotNull(ranges);
    InvariantChecks.checkNotNull(equation);

    final Collection<IntegerField> fields = new ArrayList<>();

    if (equation.lhs != null) {
      fields.add(equation.lhs);
    }
    if (equation.rhs != null) {
      fields.add(equation.rhs);
    }

    // Add the link between the fields (to be able to split the variables into disjoint ranges).
    if (equation.lhs != null && equation.rhs != null) {
      Collection<IntegerField> lhsLinkedWith = linkedWith.get(equation.lhs);
      if (lhsLinkedWith == null) {
        linkedWith.put(equation.lhs, lhsLinkedWith = new LinkedHashSet<>());
      }
      Collection<IntegerField> rhsLinkedWith = linkedWith.get(equation.rhs);
      if (rhsLinkedWith == null) {
        linkedWith.put(equation.rhs, rhsLinkedWith = new LinkedHashSet<>());
      }

      lhsLinkedWith.add(equation.rhs);
      rhsLinkedWith.add(equation.lhs);
    }

    for (final IntegerField field : fields) {
      final IntegerVariable var = field.getVariable();

      Collection<IntegerRange> varRanges = ranges.get(var);
      if (varRanges == null) {
        ranges.put(var, varRanges = new LinkedHashSet<>());
        varRanges.add(getRange(var));
      }

      varRanges.add(getRange(field));
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
    InvariantChecks.checkNotNull(oldLhs, "LHS is null");
    InvariantChecks.checkNotNull(oldRhs, "RHS is null");

    final IntegerClause<IntegerVariable> newClause =
        new IntegerClause<IntegerVariable>(
            oldEqual ? IntegerClause.Type.AND : IntegerClause.Type.OR);

    final List<IntegerVariable> lhsFields = getFieldVars(oldLhs);
    final List<IntegerVariable> rhsFields = getFieldVars(oldRhs);

    InvariantChecks.checkTrue(lhsFields.size() == rhsFields.size(),
        String.format("Fields %s and %s have different number of ranges: %s and %s",
            oldLhs, oldRhs, lhsFields, rhsFields));

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
    InvariantChecks.checkNotNull(oldLhs, "LHS is null");
    InvariantChecks.checkNotNull(oldRhs, "RHS is null");

    final IntegerClause<IntegerVariable> newClause =
        new IntegerClause<IntegerVariable>(
            oldEqual ? IntegerClause.Type.AND : IntegerClause.Type.OR);

    final List<IntegerVariable> lhsFields = getFieldVars(oldLhs);

    for (int i = 0; i < lhsFields.size(); i++) {
      final IntegerVariable newLhs = lhsFields.get(i);
      final IntegerRange newRange = fieldToRange.get(newLhs);

      final int lo = newRange.getMin().intValue() - oldLhs.getLoIndex();
      final int hi = newRange.getMax().intValue() - oldLhs.getLoIndex();;

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
      final Collection<IntegerVariable> fields = varToFields.get(variable);

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
