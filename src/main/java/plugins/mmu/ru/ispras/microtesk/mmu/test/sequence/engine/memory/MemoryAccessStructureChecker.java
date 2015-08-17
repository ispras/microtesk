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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.basis.solver.integer.IntegerClause;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormula;
import ru.ispras.microtesk.basis.solver.integer.IntegerFormulaSolver;
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAssignment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.utils.function.Predicate;

/**
 * {@link MemoryAccessStructureChecker} checks consistency of a {@link MemoryAccessStructure}.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MemoryAccessStructureChecker implements Predicate<MemoryAccessStructure> {
  /** Maps a variable copy name to the list of field names. */
  private final Map<String, List<String>> varCopyToFields = new LinkedHashMap<>();
  /** Maps a variable copy name to the list of ranges. */
  private final Map<String, List<IntegerRange>> varCopyToRanges = new LinkedHashMap<>();
  /** Maps a field name to the range. */
  private final Map<String, IntegerRange> fieldToRange = new LinkedHashMap<>();
  /** Maps a field name to the integer variable used in the formula. */
  private final Map<String, IntegerVariable> fieldToFormulaVar = new LinkedHashMap<>();

  /** Custom filter. */
  private final Predicate<MemoryAccessStructure> filter;

  /**
   * Constructs a checker.
   * 
   * @param filter the memory access filter.
   */
  public MemoryAccessStructureChecker(
      final Predicate<MemoryAccessStructure> filter) {
    InvariantChecks.checkNotNull(filter);
    this.filter = filter;
  }

  /**
   * Check consistency of the memory access structure.
   *
   * @return {@code true} if the memory access structure is consistent; {@code false} otherwise.
   */
  @Override
  public boolean test(final MemoryAccessStructure structure) {
    InvariantChecks.checkNotNull(structure);
    return solve(structure).getStatus() == SolverResult.Status.SAT;
  }

  private SolverResult<Map<IntegerVariable, BigInteger>> solve(final MemoryAccessStructure structure) {
    // TODO:
    varCopyToFields.clear();
    varCopyToRanges.clear();
    fieldToRange.clear();
    fieldToFormulaVar.clear();

    if (!filter.test(structure)) {
      return new SolverResult<>("UNSAT: Custom filter");
    }

    final MemoryAccessVariableStore store = new MemoryAccessVariableStore(structure);
    final Map<IntegerVariable, List<IntegerRange>> variables = store.getDisjointRanges();

    final List<MemoryAccess> accesses = structure.getAccesses();

    for (final Map.Entry<IntegerVariable, List<IntegerRange>> entry : variables.entrySet()) {
      final IntegerVariable variable = entry.getKey();
      final List<IntegerRange> ranges = entry.getValue();

      final List<String> listOfVariableCopies = new ArrayList<>();

      for (int i = 0; i < accesses.size(); i++) {
        final String variableCopyName = createVariableName(variable, i);

        listOfVariableCopies.add(variableCopyName);
        varCopyToRanges.put(variableCopyName, ranges);
      }

      final List<List<String>> variableCopyToRangeNames = new ArrayList<>();

      for (int i = 0; i < accesses.size(); i++) {
        variableCopyToRangeNames.add(new ArrayList<String>());
      }

      for (final IntegerRange range : ranges) {
        final List<String> listOfRangeCopies = new ArrayList<>();

        for (int i = 0; i < accesses.size(); i++) {
          final String rangeCopyName = createVariableName(variable, i, range);

          listOfRangeCopies.add(rangeCopyName);
          variableCopyToRangeNames.get(i).add(rangeCopyName);
        }

        for (int i = 0; i < accesses.size(); i++) {
          final IntegerVariable rangeCopyVariable =
              new IntegerVariable(listOfRangeCopies.get(i), range.size().intValue());

          fieldToFormulaVar.put(listOfRangeCopies.get(i), rangeCopyVariable);
          fieldToRange.put(listOfRangeCopies.get(i), range);
        }
      }

      for (int i = 0; i < accesses.size(); i++) {
        varCopyToFields.put(listOfVariableCopies.get(i), variableCopyToRangeNames.get(i));
      }
    }

    final IntegerFormula<IntegerVariable> formula = new IntegerFormula<IntegerVariable>();
    final Set<IntegerVariable> formulaVariables = new LinkedHashSet<>();

    for (final Map.Entry<String, IntegerVariable> variable : fieldToFormulaVar.entrySet()) {
      formulaVariables.add(variable.getValue());
    }

    for (int i = 0; i < accesses.size(); i++) {
      final MemoryAccess access = accesses.get(i);
      final MemoryAccessPath path = access.getPath();

      for (final MmuTransition transition : path.getTransitions()) {
        if (!process(formula, i, transition)) {
          return new SolverResult<>("UNSAT: Transition");
        }
      }
    }

    for (int i = 0; i < accesses.size() - 1; i++) {
      final MemoryAccess access1 = accesses.get(i);

      for (int j = i + 1; j < accesses.size(); j++) {
        final MemoryAccess access2 = accesses.get(j);
        final MemoryDependency dependency = structure.getDependency(i, j);

        if (dependency != null) {
          if (!process(formula, formulaVariables, i, j, access1, access2, dependency)) {
            return new SolverResult<>("UNSAT: Access pair");
          }
        }
      }
    }

    final IntegerFormulaSolver solver = new IntegerFormulaSolver(formulaVariables, formula);
    final SolverResult<Map<IntegerVariable, BigInteger>> result = solver.solve();

    // TODO: the result should be transformed.
    return result;
  }

  /**
   * Returns the list of the variables inside the range.
   * 
   * @param i number of execution.
   * @param term contain the ranges.
   * @return list of the variables.
   */
  private List<IntegerVariable> getVariable(final int i, final IntegerField term) {
    final IntegerVariable mmuVariable = term.getVariable();

    final String variableName = createVariableName(mmuVariable, i);
    final List<IntegerRange> ranges = varCopyToRanges.get(variableName);

    final IntegerRange variableRange = new IntegerRange(term.getLoIndex(), term.getHiIndex());

    final List<IntegerVariable> variables = new ArrayList<>();
    for (final IntegerRange range : ranges) {
      if (variableRange.contains(range)) {

        final String key = createVariableName(mmuVariable, i, range);
        final IntegerVariable var = fieldToFormulaVar.get(key);

        variables.add(var);
      }
    }

    return variables;
  }

  /**
   * Gathers the variable name for this range.
   * 
   * @param mmuVariable base variable.
   * @param i the index of execution.
   * @param range the range of variable.
   * @return variable name.
   */
  private static String createVariableName(final IntegerVariable mmuVariable, final int i,
      final IntegerRange range) {
    final String executionVariable = createVariableName(mmuVariable, i);
    return String.format("%s$%s", executionVariable, range);
  }

  /**
   * Gathers the variable name for this execution.
   * 
   * @param mmuVariable base variable.
   * @param i the index of execution.
   * @return variable name.
   */
  private static String createVariableName(final IntegerVariable mmuVariable, final int i) {
    return String.format("%s$%d", mmuVariable.getName(), i);
  }

  /**
   * Returns range of BigInteger.
   * 
   * @param constant the BigInteger value
   * @param lo the lo index.
   * @param hi the hi index.
   * @return range of BigInteger.
   */
  private static BigInteger getRangeConstant(
      final BigInteger constant, final int lo, final int hi) {
    // Base for the mask
    final BigInteger mask = BigInteger.valueOf(2);
    // Create mask: 2^(term size) - 1
    final int termSize = hi - lo + 1;
    BigInteger val = mask.pow(termSize).subtract(BigInteger.ONE);
    // (val >> lo index) & mask
    val = val.and(constant.shiftRight(lo));

    return val;
  }

  private boolean process(
      final IntegerFormula<IntegerVariable> formula, final int i, final MmuConditionAtom atom) {
    InvariantChecks.checkNotNull(formula);
    InvariantChecks.checkNotNull(atom);

    // The code is not applicable to non-constant constraints.
    InvariantChecks.checkTrue(atom.getType() == MmuConditionAtom.Type.EQUAL_CONST);

    final MmuExpression expression = atom.getExpression();

    if (expression == null) {
      return true;
    }

    final IntegerClause<IntegerVariable> clause =
        new IntegerClause<IntegerVariable>(
            !atom.isNegated() ? IntegerClause.Type.AND : IntegerClause.Type.OR);

    final BigInteger constant = atom.getConstant();
    final List<IntegerField> terms = expression.getTerms();

    for (final IntegerField term : terms) {
      final List<IntegerVariable> variables = getVariable(i, term);

      for (final IntegerVariable variable : variables) {
        final IntegerRange range = fieldToRange.get(variable.getName());

        final int lo = range.getMin().intValue();
        final int hi = range.getMax().intValue();

        final BigInteger value = getRangeConstant(constant, lo, hi);

        clause.addEquation(variable, value, !atom.isNegated());
      }
    }

    formula.addEquationClause(clause);

    return true;
  }

  private boolean process(
      final IntegerFormula<IntegerVariable> formula,
      final int i,
      final Map<IntegerField, MmuAssignment> assignments) {
    InvariantChecks.checkNotNull(formula);
    InvariantChecks.checkNotNull(assignments);

    for (final Map.Entry<IntegerField, MmuAssignment> assignmentSet : assignments.entrySet()) {
      final IntegerField field = assignmentSet.getKey();
      final MmuAssignment assignment = assignmentSet.getValue();
      final String name = createVariableName(field.getVariable(), i);

      final MmuExpression expression = assignment != null ? assignment.getRhs() : null;

      if (expression == null) {
        continue;
      }

      final List<IntegerField> termList = expression.getTerms();

      int termsSize = 0;
      // Get the shift value
      for (final IntegerField term : termList) {
        termsSize += term.getWidth();
      }

      // Get variable shift
      int variableShift = 0;
      int zeroShift = field.getLoIndex();
      final int variableWidth = field.getWidth();

      if (termsSize < variableWidth) {
        variableShift = variableWidth - termsSize;

        final IntegerRange seachRange = new IntegerRange(
            zeroShift + variableWidth - variableShift, zeroShift + variableWidth - 1);

        final String baseVarName = createVariableName(field.getVariable(), i);

        final List<String> a = varCopyToFields.get(baseVarName);
        InvariantChecks.checkNotNull(a);

        for (final String b : a) {
          final IntegerRange c = fieldToRange.get(b);
          if (seachRange.contains(c)) {
            final String varName = createVariableName(field.getVariable(), i, c);
            final IntegerVariable var = fieldToFormulaVar.get(varName);

            if (var == null) {
              throw new IllegalArgumentException("MmuVariable '" + varName
                  + "' was null inside method 'process'.");
            }

            // Create constant for solver
            final BigInteger val = getRangeConstant(BigInteger.ZERO, 0, c.size().intValue() - 1);
            formula.addEquation(var, val, true);
          }
        }
      }

      final List<IntegerRange> rangesList = varCopyToRanges.get(name);
      InvariantChecks.checkNotNull(rangesList);

      int index = 0;
      for (final IntegerField term : termList) {
        List<IntegerVariable> termVariables = getVariable(i, term);

        for (final IntegerVariable termVariable : termVariables) {

          if (index >= rangesList.size()) {
            throw new IllegalStateException("Error: Ranges size not equal.");
          }

          final IntegerRange varRange = rangesList.get(index);
          final IntegerRange var2Range = fieldToRange.get(termVariable.getName());
          InvariantChecks.checkNotNull(var2Range);

          if (!varRange.size().equals(var2Range.size())) {
            throw new IllegalStateException("Error: Ranges size not equal: " + varRange + " =/= "
                + var2Range + ". Variable:" + field.getVariable());
          }

          final IntegerVariable var =
              fieldToFormulaVar.get(createVariableName(field.getVariable(), i, varRange));
          formula.addEquation(var, termVariable, true);

          index++;
        }
      }
    }

    return true;
  }

  private boolean process(
      final IntegerFormula<IntegerVariable> formula, final int i, final MmuCondition condition) {
    InvariantChecks.checkNotNull(formula);
    InvariantChecks.checkNotNull(condition);

    // The code is not applicable to OR-connected constraints.
    InvariantChecks.checkTrue(condition.getType() == MmuCondition.Type.AND);

    final List<MmuConditionAtom> atoms = condition.getAtoms();

    for (final MmuConditionAtom atom : atoms) {
      if (!process(formula, i, atom)) {
        return false;
      }
    }

    return true;
  }

  private boolean process(
      final IntegerFormula<IntegerVariable> formula, final int i, final MmuTransition transition) {
    InvariantChecks.checkNotNull(formula);
    InvariantChecks.checkNotNull(transition);

    final MmuGuard guard = transition.getGuard();

    if (guard != null) {
      final MmuCondition condition = guard.getCondition();

      if (condition != null) {
        if (!process(formula, i, condition)) {
          return false;
        }
      }
    }

    final MmuAction source = transition.getSource();
    final Map<IntegerField, MmuAssignment> assignments = source.getAction();

    if (assignments != null) {
      if (!process(formula, i, assignments)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Adds the constraints for the given dependency between two memory accesses.
   * 
   * @param formula the formula to be constructed.
   * @param i the index of the primary memory access.
   * @param j the index of the secondary memory access.
   * @param access1 the primary memory access.
   * @param access2 the secondary memory access.
   * @param dependency the dependency between the memory accesses.
   * @return {@code true} if the constraints may be SAT; {@code false} otherwise.
   */
  private boolean process(
      final IntegerFormula<IntegerVariable> formula,
      final Set<IntegerVariable> formulaVariables,
      final int i,
      final int j,
      final MemoryAccess access1,
      final MemoryAccess access2,
      final MemoryDependency dependency) {
    InvariantChecks.checkNotNull(formula);
    InvariantChecks.checkNotNull(formulaVariables);
    InvariantChecks.checkNotNull(access1);
    InvariantChecks.checkNotNull(access2);
    InvariantChecks.checkNotNull(dependency);

    if (dependency == null) {
      return true;
    }

    final MemoryAccessPath accessPath1 = access1.getPath();
    final MemoryAccessPath accessPath2 = access2.getPath();

    for (final MemoryHazard hazard : dependency.getHazards()) {
      if (hazard.getType() == MemoryHazard.Type.TAG_EQUAL) {
        final MmuBuffer device = hazard.getDevice();
        InvariantChecks.checkNotNull(device);

        if (BufferAccessEvent.HIT == accessPath1.getEvent(device)
            && BufferAccessEvent.HIT == accessPath2.getEvent(device)) {
          final List<IntegerVariable> fields = device.getFields();

          for (final IntegerVariable field : fields) {
            final String value1 = createVariableName(field, i);
            final String value2 = createVariableName(field, j);

            List<String> values1 = varCopyToFields.get(value1);
            if (values1 == null) {
              addVariable(formulaVariables, field, i);
              values1 = varCopyToFields.get(value1);
            }

            List<String> values2 = varCopyToFields.get(value2);
            if (values2 == null) {
              addVariable(formulaVariables, field, j);
              values2 = varCopyToFields.get(value2);
            }

            for (int k = 0; k < values1.size(); k++) {
              final IntegerVariable variable1 = fieldToFormulaVar.get(values1.get(k));
              final IntegerVariable variable2 = fieldToFormulaVar.get(values2.get(k));

              formula.addEquation(variable1, variable2, true);
            }
          }
        }
      }

      final MmuCondition condition = hazard.getCondition();

      if (condition == null) {
        continue;
      }

      final List<MmuConditionAtom> atoms = condition.getAtoms();

      for (final MmuConditionAtom atom : atoms) {
        final MmuExpression expression = atom.getExpression();

        if (expression == null || atom.getType() != MmuConditionAtom.Type.EQUAL) {
            continue;
        }

        final List<IntegerField> terms = expression.getTerms();

        if (terms.isEmpty() && atom.isNegated()) {
          return false;
        }

        final IntegerClause<IntegerVariable> clause =
            new IntegerClause<IntegerVariable>(
                !atom.isNegated() ? IntegerClause.Type.AND : IntegerClause.Type.OR);

        for (final IntegerField term : terms) {
          final List<IntegerVariable> variableListI = getVariable(i, term);
          final List<IntegerVariable> variableListJ = getVariable(j, term);

          for (int k = 0; k < variableListI.size(); k++) {
            clause.addEquation(variableListI.get(k), variableListJ.get(k), !atom.isNegated());
          }
        }

        formula.addEquationClause(clause);
      }
    }

    return true;
  }

  private void addVariable(
      final Set<IntegerVariable> formulaVariables, final IntegerVariable variable, final int i) {
    final String baseValue = createVariableName(variable, i);

    final IntegerRange range = new IntegerRange(0, variable.getWidth() - 1);
    final List<IntegerRange> ranges = new ArrayList<>();
    ranges.add(range);
    varCopyToRanges.put(baseValue, ranges);

    final String value = createVariableName(variable, i, range);
    final List<String> values = new ArrayList<>();
    values.add(value);
    varCopyToFields.put(baseValue, values);

    fieldToRange.put(value, range);
    final IntegerVariable formulaVariable = new IntegerVariable(value, variable.getWidth());
    fieldToFormulaVar.put(value, formulaVariable);

    formulaVariables.add(formulaVariable);
  }
}
