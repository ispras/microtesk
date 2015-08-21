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

  /** Variables used in the formula. */
  private final Collection<IntegerVariable> variables;
  /** Formula (constraint) to be solved. */
  private final IntegerFormula<IntegerField> formula;
  /** Initializer used to fill the unused fields of the variables. */
  private final IntegerVariableInitializer initializer;

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

  /**
   * Constructs a solver.
   * 
   * @param variables the variables to be included into a solution.
   * @param formula the constraint to be solved.
   * @param initializer the initializer to be used to fill the unused fields. 
   */
  public IntegerFieldFormulaSolver(
    final Collection<IntegerVariable> variables,
    final IntegerFormula<IntegerField> formula,
    final IntegerVariableInitializer initializer) {
    InvariantChecks.checkNotNull(variables);
    InvariantChecks.checkNotNull(formula);

    this.variables = variables;
    this.formula = formula;
    this.initializer = initializer;

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

    final IntegerVariable var = field.getVariable();

    final Collection<IntegerRange> fieldRanges =
        IntegerRange.select(dividedRanges.get(var), getRange(field));

    final List<IntegerVariable> result = new ArrayList<>();
    for (final IntegerRange fieldRange : fieldRanges) {
      result.add(getFieldVar(var, fieldRange));
    }

    return result;
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

    // Gather the variables' ranges used in the formula.
    final Map<IntegerVariable, Collection<IntegerRange>> ranges = new LinkedHashMap<>();

    for (final IntegerClause<IntegerField> clause : formula.getClauses()) {
      gatherRanges(ranges, clause);
    }

    // Construct the disjoint ranges not taking into account the links between the fields.
    // Two fields, F1 and F2, are called linked if the formula contains (F1 == F2) or (F1 != F2).
    final Map<IntegerVariable, List<IntegerRange>> dividedRanges = getDividedRanges(ranges);

    // Perform further splitting taking into account the links between the fields.
    boolean divideRanges;

    do {
      divideRanges = false;

      for (final Map.Entry<IntegerField, Collection<IntegerField>> entry : linkedWith.entrySet()) {
        final IntegerField field = entry.getKey();
        final Collection<IntegerField> links = entry.getValue();

        final Collection<IntegerRange> oldDividedRanges = dividedRanges.get(field.getVariable());
        InvariantChecks.checkFalse(oldDividedRanges.isEmpty());

        for (final IntegerField link : links) {
          InvariantChecks.checkTrue(field.getWidth() == link.getWidth());

          final Collection<IntegerRange> linkDividedRanges =
              IntegerRange.select(dividedRanges.get(link.getVariable()), getRange(link));
          InvariantChecks.checkFalse(linkDividedRanges.isEmpty());

          // Possibly, the ranges should be split.
          if (linkDividedRanges.size() > 1) {
            final int offset = field.getLoIndex() - link.getLoIndex();
            final Collection<IntegerRange> newRanges = new LinkedHashSet<>(oldDividedRanges);

            for (final IntegerRange linkRange : linkDividedRanges) {
              newRanges.add(linkRange.shift(offset));
            }

            if (newRanges.size() > oldDividedRanges.size()) {
              final List<IntegerRange> newDividedRanges = IntegerRange.divide(newRanges);
              InvariantChecks.checkTrue(newDividedRanges.size() >= oldDividedRanges.size());

              // Definitely, the field should be split.
              if (newDividedRanges.size() > oldDividedRanges.size()) {
                dividedRanges.put(field.getVariable(), newDividedRanges);
                divideRanges = true;
              }
            }
          }
        }
      } // for field-links.
    } while(divideRanges);

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

      if (!linkedWith.containsKey(equation.lhs)) {
        linkedWith.put(equation.lhs, new LinkedHashSet<IntegerField>());
      }
    }
    if (equation.rhs != null) {
      fields.add(equation.rhs);

      if (!linkedWith.containsKey(equation.rhs)) {
        linkedWith.put(equation.rhs, new LinkedHashSet<IntegerField>());
      }
    }

    // Add the link between the fields (to be able to split the variables into disjoint ranges).
    if (equation.lhs != null && equation.rhs != null) {
      final Collection<IntegerField> lhsLinkedWith = linkedWith.get(equation.lhs);
      final Collection<IntegerField> rhsLinkedWith = linkedWith.get(equation.rhs);

      lhsLinkedWith.add(equation.rhs);
      lhsLinkedWith.addAll(rhsLinkedWith);
      lhsLinkedWith.remove(equation.lhs);

      rhsLinkedWith.add(equation.lhs);
      rhsLinkedWith.addAll(lhsLinkedWith);
      rhsLinkedWith.remove(equation.rhs);
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

    for (final IntegerClause<IntegerField> oldClause : oldFormula.getClauses()) {
      newFormula.addClauses(getClauses(oldClause));
    }

    return newFormula;
  }

  private Collection<IntegerClause<IntegerVariable>> getClauses(
      final IntegerClause<IntegerField> oldClause) {
    InvariantChecks.checkNotNull(oldClause);

    final List<IntegerEquation<IntegerVariable>> conjunctions = new ArrayList<>();
    final List<IntegerEquation<IntegerVariable>> disjunctions = new ArrayList<>();

    for (final IntegerEquation<IntegerField> oldEquation : oldClause.getEquations()) {
      final IntegerClause<IntegerVariable> clause = getClause(oldEquation);

      final List<IntegerEquation<IntegerVariable>> equations =
          (clause.getType() == IntegerClause.Type.AND ? conjunctions : disjunctions);

      equations.addAll(clause.getEquations());
    }

    final Collection<IntegerClause<IntegerVariable>> newClauses = new ArrayList<>();

    if (oldClause.getType() == IntegerClause.Type.AND
        || conjunctions.isEmpty() || disjunctions.isEmpty()) {
      if (!conjunctions.isEmpty()) {
        newClauses.add(new IntegerClause<>(IntegerClause.Type.AND, conjunctions));
      }
      if (!disjunctions.isEmpty()) {
        newClauses.add(new IntegerClause<>(IntegerClause.Type.OR, disjunctions));
      }
    } else {
      for (final IntegerEquation<IntegerVariable> conjunction : conjunctions) {
        final IntegerClause<IntegerVariable> newClause =
            new IntegerClause<>(IntegerClause.Type.OR, disjunctions);

        newClause.addEquation(conjunction);
        newClauses.add(newClause);
      }
    }

    return newClauses;
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

      BigInteger value = initializer.getValue(variable);

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
