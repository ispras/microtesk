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
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAssignment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.BufferAccessEvent;
import ru.ispras.microtesk.utils.function.Predicate;

/**
 * {@link MemoryAccessStructureChecker} checks consistency of a {@link MemoryAccessStructure}.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MemoryAccessStructureChecker {
  private final Set<IntegerVariable> formulaVariables = new LinkedHashSet<>();
  private final IntegerFormula formula = new IntegerFormula();

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

  /**
   * Links the terms of the associate range. V[A ... B] : V1[A1 ... B1] ... Vk[Ak ... Bk] A - B ==
   * A1 - B1 == Ak - Bk.
   */
  private final Map<IntegerField, Set<IntegerField>> variablesLinkedMap = new LinkedHashMap<>();

  /**
   * Group of the ranges of one term. V[A ... B] : Range[A ... A1] ... Range[Ak ... B].
   */
  private final Map<IntegerField, Set<IntegerRange>> variablesRangesMap = new LinkedHashMap<>();

  /**
   * List of intersecting ranges of the variable. V[A ... B ... C ... D] : Range[A ... C], Range[B
   * ... D], Range[B ... C].
   */
  private final Map<IntegerVariable, Set<IntegerField>> intersectingRanges = new LinkedHashMap<>();

  /** Template to be checked. */
  private final MemoryAccessStructure template;
  private final Predicate<MemoryAccessStructure> filter;

  /**
   * Constructs a checker for the given pair of executions.
   *
   * @param memory the memory specification.
   * @param access1 the first execution.
   * @param access2 the second execution.
   * @param dependency the dependency between the first and second executions.
   * @param filter the template filter.
   */
  public MemoryAccessStructureChecker(final MmuSubsystem memory, final MemoryAccess access1,
      final MemoryAccess access2, final MemoryDependency dependency,
      final Predicate<MemoryAccessStructure> filter) {
    InvariantChecks.checkNotNull(access1);
    InvariantChecks.checkNotNull(access2);
    InvariantChecks.checkNotNull(filter);

    final List<MemoryAccess> executions = new ArrayList<>();
    executions.add(access1);
    executions.add(access2);

    final MemoryDependency[][] dependencies = new MemoryDependency[2][2];
    dependencies[0][0] = null;
    dependencies[1][0] = dependency;
    dependencies[0][1] = null;
    dependencies[1][1] = null;

    this.template = new MemoryAccessStructure(memory, executions, dependencies);
    this.filter = filter;
  }

  /**
   * Constructs a template checker.
   * 
   * @param template the template to be checked.
   * @param filter the template filter.
   */
  public MemoryAccessStructureChecker(final MemoryAccessStructure template,
      final Predicate<MemoryAccessStructure> filter) {
    InvariantChecks.checkNotNull(template);
    InvariantChecks.checkNotNull(filter);

    this.template = template;
    this.filter = filter;
  }

  /**
   * Check consistency of the template.
   *
   * @return {@code true} if the condition is consistent; {@code false} otherwise.
   */
  public boolean check() {
    // Step 0. Check the memory access structure by using filters.
    if (!filter.test(template)) {
      return false;
    }

    final Map<IntegerVariable, Set<IntegerRange>> variableRange = new LinkedHashMap<>();

    // Step 1. Add ranges for memory dependencies.
    final MemoryDependency[][] dependencies = template.getDependencies();

    for (final MemoryDependency[] arrayDependency : dependencies) {
      for (final MemoryDependency dependency : arrayDependency) {
        if (dependency != null) {
          // Initialize ranges: X = [1 .. a][a+1 .. b][b+1 .. n]
          initVariableRange(dependency, variableRange);
        }
      }
    }

    // Step 2. Add ranges for memory accesses.
    final List<MemoryAccess> accesses = template.getAccesses();

    for (final MemoryAccess execution : accesses) {
      // Initialize ranges: X = [1 .. a][a+1 .. b][b+1 .. n]
      initVariableRange(execution, variableRange);
    }

    // Get list of the unique ranges.
    final Map<IntegerVariable, List<IntegerRange>> rangedVariable =
        transformToUniqueRange(variableRange);

    variableRange.clear();
    for (final Map.Entry<IntegerVariable, List<IntegerRange>> variable : rangedVariable.entrySet()) {
      final Set<IntegerRange> rangeSet = new LinkedHashSet<>();
      final List<IntegerRange> rangeList = variable.getValue();
      for (IntegerRange range : rangeList) {
        rangeSet.add(range);
      }
      variableRange.put(variable.getKey(), rangeSet);
    }

    // Step 3.
    for (final MemoryAccess access : accesses) {
      // Add ranges: X[1 .. n] = Y[1 .. a][a+1 .. b][b+1 .. n]
      addVariableRange(access, variableRange);
    }

    // Get list of all unique ranges.
    final Map<IntegerVariable, List<IntegerRange>> variables =
        transformToUniqueRange(variableRange);

    // Step 4.
    // If we have linked variables.
    if (!variablesLinkedMap.isEmpty()) {
      linkedUniqueRange(variables);
    }

    // Step 5.
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

    // Step 6.
    // Add variables to the solver
    for (final Map.Entry<String, IntegerVariable> variable : mmuVariables.entrySet()) {
      formulaVariables.add(variable.getValue());
    }

    for (int i = 0; i < accesses.size(); i++) {
      // Get equations from execution.
      for (final MmuTransition transition : accesses.get(i).getTransitions()) {
        if (!process(i, transition)) {
          return false;
        }
      }
    }

    // Step 7.
    for (int i = 0; i < accesses.size() - 1; i++) {
      for (int j = i + 1; j < accesses.size(); j++) {

        final MemoryDependency dependency = template.getDependency(i, j);
        if (dependency != null) {
          // Get equations from dependency of i & j execution.
          if (!process(i, j, accesses.get(i), accesses.get(j), dependency)) {
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
   * Adds the ranges of the variable from dependency to the list.
   * 
   * @param dependency the dependency of executions.
   * @param variableRange the list of variable ranges.
   */
  private void initVariableRange(
      final MemoryDependency dependency,
      final Map<IntegerVariable, Set<IntegerRange>> variableRange) {

    final List<MemoryHazard> hazards = dependency.getHazards();
    for (final MemoryHazard hazard : hazards) {
      final MmuCondition condition = hazard.getCondition();
      if (condition != null) {
        initVariableRange(condition, variableRange);
      }
    }
  }

  /**
   * Adds the ranges of the variable from execution to the list.
   * 
   * @param execution the execution of template.
   * @param variableRange the list of variable ranges.
   */
  private void addVariableRange(final MemoryAccess execution,
      final Map<IntegerVariable, Set<IntegerRange>> variableRange) {

    for (final MmuTransition transition : execution.getTransitions()) {
      // Get terms from action
      final MmuAction source = transition.getSource();
      final Map<IntegerField, MmuAssignment> actions = source.getAction();

      for (final Map.Entry<IntegerField, MmuAssignment> action : actions.entrySet()) {
        final MmuAssignment assignment = action.getValue();

        final MmuExpression expression = assignment.getRhs();
        if (expression != null) {
          // Adds the left part of expression to the variableRange.
          addVariableRange(expression, variableRange, assignment.getLhs());
        }
      }
    }
  }

  /**
   * Adds the ranges of the variable from execution to the list.
   * 
   * @param execution the execution of template.
   * @param variableRange the list of variable ranges.
   */
  private static void initVariableRange(final MemoryAccess execution,
      final Map<IntegerVariable, Set<IntegerRange>> variableRange) {
    for (final MmuTransition transition : execution.getTransitions()) {

      // Get terms from action
      final MmuAction source = transition.getSource();
      final Map<IntegerField, MmuAssignment> actions = source.getAction();

      for (final Map.Entry<IntegerField, MmuAssignment> action : actions.entrySet()) {
        final MmuAssignment assignment = action.getValue();

        final MmuExpression expression = assignment.getRhs();
        if (expression != null) {
          initVariableRange(expression, variableRange);
        }
      }

      // Get terms from condition
      final MmuGuard guard = transition.getGuard();

      if (guard != null) {
        final MmuCondition condition = guard.getCondition();
        if (condition != null) {
          initVariableRange(condition, variableRange);
        }
      }
    }
  }

  /**
   * Adds the ranges of the variable from condition to the list.
   * 
   * @param condition the condition.
   * @param variableRange the list of variable ranges.
   */
  private static void initVariableRange(
      final MmuCondition condition,
      final Map<IntegerVariable, Set<IntegerRange>> variableRange) {
    final List<MmuConditionAtom> atoms = condition.getAtoms();
    for (final MmuConditionAtom atom : atoms) {
      final MmuExpression expression = atom.getExpression();

      if (expression != null) {
        initVariableRange(expression, variableRange);
      }
    }
  }

  /**
   * Adds the ranges of this variable from the expression to the list.
   * 
   * @param expression the expression.
   * @param variableRange the list of variable ranges.
   * @param variable the variable.
   */
  private void addVariableRange(final MmuExpression expression,
      final Map<IntegerVariable, Set<IntegerRange>> variableRange, final IntegerField field) {
    Set<IntegerRange> range;

    if (variableRange.containsKey(field.getVariable())) {
      range = variableRange.get(field.getVariable());
    } else {
      range = new LinkedHashSet<>();
      // Init range / Add to range: [0, this width - 1]
      range.add(new IntegerRange(field.getLoIndex(), field.getHiIndex()));
    }

    // Get all terms for this variable
    final List<IntegerField> terms = expression.getTerms();

    final Map<IntegerVariable, Set<IntegerRange>> variablesRangesTemp = new LinkedHashMap<>();

    int termsSize = 0;
    // Get the shift value for variable
    for (final IntegerField term : terms) {
      final IntegerVariable termVariable = term.getVariable();

      Set<IntegerRange> termRanges;
      if (variablesRangesTemp.containsKey(termVariable)) {
        termRanges = variablesRangesTemp.get(termVariable);
      } else {
        termRanges = new LinkedHashSet<>();
      }
      termRanges.add(new IntegerRange(term.getLoIndex(), term.getHiIndex()));
      variablesRangesTemp.put(term.getVariable(), termRanges);

      termsSize += term.getWidth();
    }

    final int variableWidth = field.getWidth();

    // Get variable shift
    int variableShift = 0;
    int zeroShift = field.getLoIndex();

    if (termsSize < variableWidth) {
      variableShift = variableWidth - termsSize;
      // [[Min .. ] ... [Max - variableShift .. Max]]
      // [Max - variableShift .. Max] = Const = 0
      range.add(new IntegerRange(zeroShift + variableWidth - variableShift, zeroShift
          + variableWidth - 1));
    } else if (termsSize > variableWidth) {
      throw new IllegalStateException(String.format("The length of the variable is too small: %s",
          field));
    }

    if (variablesRangesTemp.size() == 1) {
      // V1 [] = V2 []
      final IntegerField baseTerm =
          new IntegerField(field.getVariable(), zeroShift, zeroShift + variableWidth - 1
              - variableShift);

      final Map.Entry<IntegerVariable, Set<IntegerRange>> variableTemp =
          variablesRangesTemp.entrySet().iterator().next();
      final Set<IntegerRange> rangesTemp = variableTemp.getValue();

      int min = 0;
      int max = 0;

      final Set<IntegerRange> baseRangeTemp = new LinkedHashSet<>();
      if (zeroShift == 0) {
        baseRangeTemp.addAll(rangesTemp);
      }
      for (final IntegerRange rangeTemp : rangesTemp) {
        min = rangeTemp.getMin().intValue() < min ? rangeTemp.getMin().intValue() : min;
        max = rangeTemp.getMax().intValue() > max ? rangeTemp.getMax().intValue() : max;
        if (zeroShift > 0) {
          baseRangeTemp.add(new IntegerRange(rangeTemp.getMin().intValue() + zeroShift, rangeTemp
              .getMax().intValue() + zeroShift));
        }
      }

      // Add range to global value
      range.addAll(baseRangeTemp);
      variableRange.put(field.getVariable(), range);

      final IntegerField baseTerm2 = new IntegerField(variableTemp.getKey(), min, max);

      addTermToGlobalList(baseTerm, baseTerm2, baseRangeTemp, rangesTemp);

    } else {
      // V1 [] = V2 [] :: V3 [] ... VN[]
      int incr = 0;
      for (final IntegerField term : terms) {
        final Set<IntegerRange> rangesTemp = variablesRangesTemp.get(term.getVariable());
        int min = 0;
        int max = 0;

        final Set<IntegerRange> baseRangeTemp = new LinkedHashSet<>();

        for (final IntegerRange rangeTemp : rangesTemp) {
          min = rangeTemp.getMin().intValue() < min ? rangeTemp.getMin().intValue() : min;
          max = rangeTemp.getMax().intValue() > max ? rangeTemp.getMax().intValue() : max;
          if ((zeroShift == 0) && (incr == 0)) {
            baseRangeTemp.addAll(rangesTemp);
          } else {
            baseRangeTemp.add(new IntegerRange(rangeTemp.getMin().intValue() + zeroShift + incr,
                rangeTemp.getMax().intValue() + zeroShift + incr));
          }
        }

        final IntegerField baseTerm =
            new IntegerField(field.getVariable(), zeroShift + incr, zeroShift + max + incr);
        final IntegerField baseNewTerm = new IntegerField(term.getVariable(), min, max);

        incr += max + 1;
        addTermToGlobalList(baseTerm, baseNewTerm, baseRangeTemp, rangesTemp);
        // Add range to global value
        range.addAll(baseRangeTemp);
      }
      variableRange.put(field.getVariable(), range);
    }
  }

  /**
   * [n .. m] = [a .. b], m - n = b - a
   * 
   * @param baseTerm base term.
   * @param term a term that we want to associate with the baseTerm.
   * @param baseRanges the ranges of the baseTerm.
   * @param ranges the ranges of the term.
   */
  private void addTermToGlobalList(final IntegerField baseTerm, final IntegerField term,
      final Set<IntegerRange> baseRanges, final Set<IntegerRange> ranges) {
    if (variablesLinkedMap.containsKey(baseTerm)) {
      final Set<IntegerField> linkedTerms = variablesLinkedMap.get(baseTerm);

      // We have link to this range
      if (linkedTerms.contains(term)) {
        final Set<IntegerRange> tempRanges = variablesRangesMap.get(term);
        tempRanges.addAll(ranges);
        variablesRangesMap.put(term, tempRanges);
      } else {
        linkedTerms.add(term);
        variablesLinkedMap.put(baseTerm, linkedTerms);

        final Set<IntegerField> tempSet = new LinkedHashSet<>();
        tempSet.add(baseTerm);
        variablesLinkedMap.put(term, tempSet);

        // Add new range
        variablesRangesMap.put(term, ranges);
        variablesRangesMap.put(baseTerm, baseRanges);
      }
    } else {
      // Add new

      final Set<IntegerField> linkedTerms = new LinkedHashSet<>();
      linkedTerms.add(term);
      variablesLinkedMap.put(baseTerm, linkedTerms);
      variablesRangesMap.put(baseTerm, baseRanges);

      if (variablesLinkedMap.containsKey(term)) {
        final Set<IntegerRange> tempRanges = variablesRangesMap.get(term);
        tempRanges.addAll(ranges);
        variablesRangesMap.put(term, tempRanges);

        final Set<IntegerField> tempSet = variablesLinkedMap.get(term);
        tempSet.add(baseTerm);
        variablesLinkedMap.put(term, tempSet);
      } else {
        // Add new linked value[] to map
        variablesRangesMap.put(term, ranges);

        // Add new link for this value[] to map
        final Set<IntegerField> tempSet = new LinkedHashSet<>();
        tempSet.add(baseTerm);
        variablesLinkedMap.put(term, tempSet);
      }
    }

    Set<IntegerField> tempSet;
    if (intersectingRanges.containsKey(baseTerm.getVariable())) {
      tempSet = intersectingRanges.get(baseTerm.getVariable());
    } else {
      tempSet = new LinkedHashSet<>();
    }
    tempSet.add(baseTerm);
    intersectingRanges.put(baseTerm.getVariable(), tempSet);

  }

  /**
   * Link the a unique range of.
   * 
   * @param variables the variables.
   */
  private void linkedUniqueRange(final Map<IntegerVariable, List<IntegerRange>> variables) {
    for (final Map.Entry<IntegerField, Set<IntegerField>> variable : variablesLinkedMap.entrySet()) {
      linkedUniqueRange(variables, variable.getKey());
    }
  }

  /**
   * Link the a unique range of.
   * 
   * @param variables the variables.
   * @param baseTerm the term.
   */
  private void linkedUniqueRange(final Map<IntegerVariable, List<IntegerRange>> variables,
      final IntegerField baseTerm) {
    final IntegerRange baseRange = new IntegerRange(baseTerm.getLoIndex(), baseTerm.getHiIndex());
    List<IntegerRange> variableRanges = variables.get(baseTerm.getVariable());

    if (variableRanges != null) {
      final Set<IntegerRange> baseRanges = getRanges(baseRange, variableRanges);
      final List<IntegerRange> rangesList = IntegerRange.divide(baseRanges);

      for (final IntegerField term : variablesLinkedMap.get(baseTerm)) {
        int shift = term.getLoIndex() - baseTerm.getLoIndex();
        List<IntegerRange> termVariableRanges = variables.get(term.getVariable());
        final IntegerRange termRange = new IntegerRange(term.getLoIndex(), term.getHiIndex());
        final Set<IntegerRange> termRanges = getRanges(termRange, termVariableRanges);
        final List<IntegerRange> termRangesList = IntegerRange.divide(termRanges);

        boolean recalculation = false;
        if (rangesList.size() == termRangesList.size()) {
          for (int l = 0; l < rangesList.size(); l++) {
            if (!rangesList.get(l).size().equals(termRangesList.get(l).size())) {
              recalculation = true;
              break;
            }
          }
        } else {
          recalculation = true;
        }

        if (recalculation) {
          final Set<IntegerRange> rangeSet = new LinkedHashSet<>();
          rangeSet.addAll(variableRanges);
          final Set<IntegerRange> rangeSet2 = new LinkedHashSet<>();
          rangeSet2.addAll(termVariableRanges);
          final List<List<IntegerRange>> returnList =
              combineRanges(rangesList, termRangesList, shift);
          rangeSet.addAll(returnList.get(0));
          rangeSet2.addAll(returnList.get(1));

          variableRanges.clear();
          variableRanges = IntegerRange.divide(rangeSet);
          termVariableRanges.clear();
          termVariableRanges = IntegerRange.divide(rangeSet2);

          variables.put(baseTerm.getVariable(), variableRanges);
          variables.put(term.getVariable(), termVariableRanges);

          updateUniqueRange(variables, baseTerm);
          updateUniqueRange(variables, term);
        }
      }
    }
  }

  /**
   * Updates the range of the variable.
   * 
   * @param variables the variables.
   * @param term the term.
   */
  private void updateUniqueRange(final Map<IntegerVariable, List<IntegerRange>> variables,
      final IntegerField term) {
    linkedUniqueRange(variables, term);
    final Set<IntegerField> intersecting = intersectingRanges.get(term.getVariable());
    if ((intersecting != null) && (intersecting.size() > 1)) {
      for (IntegerField intersectingItem : intersecting) {
        final IntegerRange tempRange1 =
            new IntegerRange(intersectingItem.getLoIndex(), intersectingItem.getHiIndex());
        final IntegerRange tempRange2 = new IntegerRange(term.getLoIndex(), term.getHiIndex());
        if (tempRange1.intersect(tempRange2) != null) {
          linkedUniqueRange(variables, intersectingItem);
        }
      }
    }
  }

  /**
   * Make the intersection of a unique range of.
   * 
   * @param range1 the range1.
   * @param range2 the range2.
   * @param shift - shift for range2.
   * 
   * @return List of intersection of range1 & range2.
   */
  private static List<List<IntegerRange>> combineRanges(List<IntegerRange> range1,
      List<IntegerRange> range2, int shift) {
    final Set<IntegerRange> rangeSet1 = new LinkedHashSet<>();
    final Set<IntegerRange> rangeSet2 = new LinkedHashSet<>();

    // Get ranges of range1.
    for (final IntegerRange r : range1) {
      rangeSet1.add(r);
      final IntegerRange rShift =
          new IntegerRange(r.getMin().intValue() + shift, r.getMax().intValue() + shift);
      rangeSet2.add(rShift);
    }

    // Get ranges of range2.
    for (final IntegerRange r2 : range2) {
      final IntegerRange rShift =
          new IntegerRange(r2.getMin().intValue() - shift, r2.getMax().intValue() - shift);
      rangeSet1.add(rShift);
      rangeSet2.add(r2);
    }

    range1.clear();
    range2.clear();
    // Split ranges.
    range1 = IntegerRange.divide(rangeSet1);
    range2 = IntegerRange.divide(rangeSet2);

    final List<List<IntegerRange>> returnList = new ArrayList<>();
    returnList.add(range1);
    returnList.add(range2);

    return returnList;
  }

  /**
   * Returns the included ranges.
   * 
   * @param baseRange the baseRange.
   * @param listRanges the listRanges.
   * @param shift - shift for range2.
   * @return the included ranges.
   */
  private static Set<IntegerRange> getRanges(final IntegerRange baseRange,
      final List<IntegerRange> listRanges) {
    final Set<IntegerRange> ranges = new LinkedHashSet<>();

    for (final IntegerRange listRange : listRanges) {
      if (baseRange.contains(listRange)) {
        ranges.add(listRange);
      }
    }

    return ranges;
  }

  /**
   * Adds the ranges of the variable from the expression to the list.
   * 
   * @param expression the expression.
   * @param variableRange the list of variable ranges.
   */
  private static void initVariableRange(
      final MmuExpression expression,
      final Map<IntegerVariable, Set<IntegerRange>> variableRange) {

    final List<IntegerField> terms = expression.getTerms();

    for (final IntegerField term : terms) {
      final IntegerVariable variable = term.getVariable();

      Set<IntegerRange> range;
      if (variableRange.containsKey(variable)) {
        range = variableRange.get(variable);
      } else {
        range = new LinkedHashSet<>();
        range.add(new IntegerRange(0, variable.getWidth() - 1));
      }
      final IntegerRange varRange = new IntegerRange(term.getLoIndex(), term.getHiIndex());
      range.add(varRange);

      variableRange.put(variable, range);
    }
  }

  /**
   * Transforms the ranges of the variable to unique ranges.
   * 
   * @param variables the variables map.
   */
  private static Map<IntegerVariable, List<IntegerRange>> transformToUniqueRange(
      final Map<IntegerVariable, Set<IntegerRange>> variables) {

    final Map<IntegerVariable, List<IntegerRange>> returnVariables = new LinkedHashMap<>();

    for (final Map.Entry<IntegerVariable, Set<IntegerRange>> variable : variables.entrySet()) {
      final List<IntegerRange> ranges = IntegerRange.divide(variable.getValue());
      returnVariables.put(variable.getKey(), ranges);
    }

    return returnVariables;
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

  /**
   * Adds to the solver equalities.
   * 
   * @param i the index of execution.
   * @param equality the equality.
   * @return {@code true} if the equality is consistent; {@code false} otherwise.
   */
  private boolean process(final int i, final MmuConditionAtom atom) {
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

  /**
   * Adds assignments equalities to the solver.
   * 
   * @param i the index of execution.
   * @param assignments the assignments.
   * @return {@code true} if the assignments is consistent; {@code false} otherwise.
   */
  private boolean process(final int i, final Map<IntegerField, MmuAssignment> assignments) {
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

  /**
   * Adds condition equalities.
   * 
   * @param i the index of execution.
   * @param condition the condition.
   * @return {@code true} if the condition is consistent; {@code false} otherwise.
   */
  private boolean process(final int i, final MmuCondition condition) {
    InvariantChecks.checkNotNull(condition);
    // The code is not applicable to OR-connected constraints.
    InvariantChecks.checkTrue(condition.getType() == MmuCondition.Type.AND);

    final List<MmuConditionAtom> atoms = condition.getAtoms();

    for (final MmuConditionAtom atom : atoms) {
      if (!process(i, atom)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Adds transition equalities.
   * 
   * @param i the index of execution.
   * @param transition the transition.
   * @return {@code true} if the transition is solved; {@code false} otherwise.
   */
  private boolean process(final int i, final MmuTransition transition) {
    InvariantChecks.checkNotNull(transition);

    final MmuGuard guard = transition.getGuard();

    if (guard != null) {
      final MmuCondition condition = guard.getCondition();

      if (condition != null) {
        if (!process(i, condition)) {
          return false;
        }
      }
    }

    final MmuAction source = transition.getSource();
    final Map<IntegerField, MmuAssignment> assignments = source.getAction();

    if (assignments != null) {
      if (!process(i, assignments)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Adds the constraints for the given dependency between two memory accesses.
   * 
   * @param i the index of the primary memory access.
   * @param j the index of the secondary memory access.
   * @param access1 the primary memory access.
   * @param access2 the secondary memory access.
   * @param dependency the dependency between the memory accesses.
   * @return {@code true} if the constraints may be SAT; {@code false} otherwise.
   */
  private boolean process(
      final int i,
      final int j,
      final MemoryAccess access1,
      final MemoryAccess access2,
      final MemoryDependency dependency) {
    InvariantChecks.checkNotNull(access1);
    InvariantChecks.checkNotNull(access2);
    InvariantChecks.checkNotNull(dependency);

    if (dependency == null) {
      return true;
    }

    for (final MemoryHazard hazard : dependency.getHazards()) {
      if (hazard.getType() == MemoryHazard.Type.TAG_EQUAL) {
        final MmuDevice device = hazard.getDevice();
        InvariantChecks.checkNotNull(device);

        if (BufferAccessEvent.HIT == access1.getEvent(device)
            && BufferAccessEvent.HIT == access2.getEvent(device)) {
          final List<IntegerVariable> fields = device.getFields();

          for (final IntegerVariable field : fields) {
            final String value1 = gatherVariableName(field, i);
            final String value2 = gatherVariableName(field, j);

            List<String> values1 = variableLink.get(value1);
            if (values1 == null) {
              addVariable(field, i);
              values1 = variableLink.get(value1);
            }

            List<String> values2 = variableLink.get(value2);
            if (values2 == null) {
              addVariable(field, j);
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

  private void addVariable(final IntegerVariable variable, final int i) {
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
