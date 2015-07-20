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
import ru.ispras.microtesk.basis.SolverResult;
import ru.ispras.microtesk.basis.solver.IntegerClause;
import ru.ispras.microtesk.basis.solver.IntegerField;
import ru.ispras.microtesk.basis.solver.IntegerFormula;
import ru.ispras.microtesk.basis.solver.IntegerFormulaSolver;
import ru.ispras.microtesk.basis.solver.IntegerRange;
import ru.ispras.microtesk.basis.solver.IntegerVariable;
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
  /**
   * MapKey: Variable$ExecutionIndex (%s$%d). MapValue: [Variable$ExecutionIndex$Range[0 .. A], ..,
   * Variable$ExecutionIndex$Range[B .. N]].
   */
  private final Map<String, List<String>> variableLink = new LinkedHashMap<>();

  /**
   * MapKey: Variable$ExecutionIndex (%s$%d). MapValue: [Range[0 .. A] ... [B .. N]].
   */
  private final Map<String, List<IntegerRange>> variableRanges = new LinkedHashMap<>();

  /**
   * MapKey: Variable$ExecutionIndex$Range[A .. B] (%s$%d$%s). MapValue: Range[A .. B].
   */
  private final Map<String, IntegerRange> mmuRanges = new LinkedHashMap<>();

  /**
   * MapValue: Variable.Name, Variable.LO = A, Variable.HI = B.
   */
  private final Map<String, IntegerVariable> mmuVariables = new LinkedHashMap<>();

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

    // TODO:
    variableLink.clear();
    variableRanges.clear();
    mmuRanges.clear();
    mmuVariables.clear();

    if (!filter.test(structure)) {
      return false;
    }

    final MemoryAccessVariableStore store = new MemoryAccessVariableStore(structure);
    final Map<IntegerVariable, List<IntegerRange>> variables = store.getDisjointRanges();

    final List<MemoryAccess> accesses = structure.getAccesses();

    for (final Map.Entry<IntegerVariable, List<IntegerRange>> variable : variables.entrySet()) {
      final List<String> nameI = new ArrayList<>();

      for (int i = 0; i < accesses.size(); i++) {
        nameI.add(gatherVariableName(variable.getKey(), i));
        variableRanges.put(nameI.get(i), variable.getValue());
      }

      final List<List<String>> variableLinkI = new ArrayList<>();

      for (int i = 0; i < accesses.size(); i++) {
        variableLinkI.add(new ArrayList<String>());
      }

      for (final IntegerRange range : variable.getValue()) {
        final List<String> variableIRange = new ArrayList<>();

        for (int i = 0; i < accesses.size(); i++) {
          variableIRange.add(gatherVariableName(variable.getKey(), i, range));
          variableLinkI.get(i).add(variableIRange.get(i));
        }

        final List<IntegerVariable> variableI = new ArrayList<>();

        for (int i = 0; i < accesses.size(); i++) {
          variableI.add(new IntegerVariable(variableIRange.get(i), range.size().intValue()));
          mmuVariables.put(variableIRange.get(i), variableI.get(i));
          mmuRanges.put(variableIRange.get(i), range);
        }
      }

      for (int i = 0; i < accesses.size(); i++) {
        variableLink.put(nameI.get(i), variableLinkI.get(i));
      }
    }

    final IntegerFormula formula = new IntegerFormula();
    final Set<IntegerVariable> formulaVariables = new LinkedHashSet<>();

    for (final Map.Entry<String, IntegerVariable> variable : mmuVariables.entrySet()) {
      formulaVariables.add(variable.getValue());
    }

    for (int i = 0; i < accesses.size(); i++) {
      final MemoryAccess access = accesses.get(i);
      final MemoryAccessPath path = access.getPath();

      for (final MmuTransition transition : path.getTransitions()) {
        if (!process(formula, i, transition)) {
          return false;
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
            return false;
          }
        }
      }
    }

    final IntegerFormulaSolver solver = new IntegerFormulaSolver(formulaVariables, formula);
    final SolverResult<Boolean> result = solver.solve();

    return result.getStatus() == SolverResult.Status.SAT;
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

    final String variableName = gatherVariableName(mmuVariable, i);
    final List<IntegerRange> ranges = variableRanges.get(variableName);

    final IntegerRange variableRange = new IntegerRange(term.getLoIndex(), term.getHiIndex());

    final List<IntegerVariable> variables = new ArrayList<>();
    for (final IntegerRange range : ranges) {
      if (variableRange.contains(range)) {

        final String key = gatherVariableName(mmuVariable, i, range);
        final IntegerVariable var = mmuVariables.get(key);

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
  private static String gatherVariableName(final IntegerVariable mmuVariable, final int i,
      final IntegerRange range) {
    final String executionVariable = gatherVariableName(mmuVariable, i);
    return String.format("%s$%s", executionVariable, range);
  }

  /**
   * Gathers the variable name for this execution.
   * 
   * @param mmuVariable base variable.
   * @param i the index of execution.
   * @return variable name.
   */
  private static String gatherVariableName(final IntegerVariable mmuVariable, final int i) {
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

  private boolean process(final IntegerFormula formula, final int i, final MmuConditionAtom atom) {
    InvariantChecks.checkNotNull(formula);
    InvariantChecks.checkNotNull(atom);

    // The code is not applicable to non-constant constraints.
    InvariantChecks.checkTrue(atom.getType() == MmuConditionAtom.Type.EQUAL_CONST);

    final MmuExpression expression = atom.getExpression();

    if (expression == null) {
      return true;
    }

    final IntegerClause clause =
        new IntegerClause(!atom.isNegated() ? IntegerClause.Type.AND : IntegerClause.Type.OR);

    final BigInteger constant = atom.getConstant();
    final List<IntegerField> terms = expression.getTerms();

    for (final IntegerField term : terms) {
      final List<IntegerVariable> variables = getVariable(i, term);

      for (final IntegerVariable variable : variables) {
        final IntegerRange range = mmuRanges.get(variable.getName());

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
      final IntegerFormula formula,
      final int i,
      final Map<IntegerField, MmuAssignment> assignments) {
    InvariantChecks.checkNotNull(formula);
    InvariantChecks.checkNotNull(assignments);

    for (final Map.Entry<IntegerField, MmuAssignment> assignmentSet : assignments.entrySet()) {
      final IntegerField field = assignmentSet.getKey();
      final MmuAssignment assignment = assignmentSet.getValue();
      final String name = gatherVariableName(field.getVariable(), i);

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

        final IntegerRange seachRange =
            new IntegerRange(zeroShift + variableWidth - variableShift, zeroShift + variableWidth
                - 1);

        final String baseVarName = gatherVariableName(field.getVariable(), i);

        final List<String> a = variableLink.get(baseVarName);
        InvariantChecks.checkNotNull(a);

        for (final String b : a) {
          final IntegerRange c = mmuRanges.get(b);
          if (seachRange.contains(c)) {
            final String varName = gatherVariableName(field.getVariable(), i, c);
            final IntegerVariable var = mmuVariables.get(varName);

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

      final List<IntegerRange> rangesList = variableRanges.get(name);
      InvariantChecks.checkNotNull(rangesList);

      int index = 0;
      for (final IntegerField term : termList) {
        List<IntegerVariable> termVariables = getVariable(i, term);

        for (final IntegerVariable termVariable : termVariables) {

          if (index >= rangesList.size()) {
            throw new IllegalStateException("Error: Ranges size not equal.");
          }

          final IntegerRange varRange = rangesList.get(index);
          final IntegerRange var2Range = mmuRanges.get(termVariable.getName());
          InvariantChecks.checkNotNull(var2Range);

          if (!varRange.size().equals(var2Range.size())) {
            throw new IllegalStateException("Error: Ranges size not equal: " + varRange + " =/= "
                + var2Range + ". Variable:" + field.getVariable());
          }

          final IntegerVariable var =
              mmuVariables.get(gatherVariableName(field.getVariable(), i, varRange));
          formula.addEquation(var, termVariable, true);

          index++;
        }
      }
    }

    return true;
  }

  private boolean process(final IntegerFormula formula, final int i, final MmuCondition condition) {
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
      final IntegerFormula formula, final int i, final MmuTransition transition) {
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
      final IntegerFormula formula,
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
            final String value1 = gatherVariableName(field, i);
            final String value2 = gatherVariableName(field, j);

            List<String> values1 = variableLink.get(value1);
            if (values1 == null) {
              addVariable(formulaVariables, field, i);
              values1 = variableLink.get(value1);
            }

            List<String> values2 = variableLink.get(value2);
            if (values2 == null) {
              addVariable(formulaVariables, field, j);
              values2 = variableLink.get(value2);
            }

            for (int k = 0; k < values1.size(); k++) {
              final IntegerVariable variable1 = mmuVariables.get(values1.get(k));
              final IntegerVariable variable2 = mmuVariables.get(values2.get(k));

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

        final IntegerClause clause =
            new IntegerClause(!atom.isNegated() ? IntegerClause.Type.AND : IntegerClause.Type.OR);

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
    final String baseValue = gatherVariableName(variable, i);

    final IntegerRange range = new IntegerRange(0, variable.getWidth() - 1);
    final List<IntegerRange> ranges = new ArrayList<>();
    ranges.add(range);
    variableRanges.put(baseValue, ranges);

    final String value = gatherVariableName(variable, i, range);
    final List<String> values = new ArrayList<>();
    values.add(value);
    variableLink.put(baseValue, values);

    mmuRanges.put(value, range);
    final IntegerVariable formulaVariable = new IntegerVariable(value, variable.getWidth());
    mmuVariables.put(value, formulaVariable);

    formulaVariables.add(formulaVariable);
  }
}
