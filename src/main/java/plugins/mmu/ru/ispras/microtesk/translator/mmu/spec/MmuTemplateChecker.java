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

package ru.ispras.microtesk.translator.mmu.spec;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.spec.basis.BufferAccessEvent;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerEquationSet;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerEquationSolver;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerField;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerRange;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

/**
 * This class implements a solver that checks consistency of dependencies and/or constructs
 * addresses for a given test template.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class MmuTemplateChecker {
  /**
   * MapKey: Variable$ExecutionIndex (%s$%d). MapValue: [Variable$ExecutionIndex$Range[0 .. A], ..,
   * Variable$ExecutionIndex$Range[B .. N]].
   */
  private static final Map<String, List<String>> variableLink = new HashMap<>();
  /**
   * MapKey: Variable$ExecutionIndex (%s$%d). MapValue: [Range[0 .. A] ... [B .. N]].
   */
  private static final Map<String, List<IntegerRange>> variableRanges = new HashMap<>();

  /**
   * MapKey: Variable$ExecutionIndex$Range[A .. B] (%s$%d$%s). MapValue: Range[A .. B].
   */
  private static Map<String, IntegerRange> mmuRanges = new HashMap<>();
  /**
   * MapValue: Variable.Name, Variable.LO = A, Variable.HI = B.
   */
  private static Map<String, IntegerVariable> mmuVariables = new HashMap<>();

  /**
   * Links the terms of the associate range. V[A ... B] : V1[A1 ... B1] ... Vk[Ak ... Bk] A - B ==
   * A1 - B1 == Ak - Bk.
   */
  private static Map<IntegerField, Set<IntegerField>> variablesLinkedMap = new HashMap<>();

  /**
   * Group of the ranges of one term. V[A ... B] : Range[A ... A1] ... Range[Ak ... B].
   */
  private static Map<IntegerField, Set<IntegerRange>> variablesRangesMap = new HashMap<>();

  /**
   * List of intersecting ranges of the variable. V[A ... B ... C ... D] : Range[A ... C], Range[B
   * ... D], Range[B ... C].
   */
  private static Map<IntegerVariable, Set<IntegerField>> intersectingRanges = new HashMap<>();

  /**
   * Check consistency of the given pair of executions.
   *
   * @param i the first instruction index.
   * @param j the second instruction index.
   * @param execution1 the first execution.
   * @param execution2 the second execution.
   * @param dependency the dependency between the first and second executions.
   * @return {@code true} if the condition is consistent; {@code false} otherwise.
   * @throws IllegalArgumentException if {@code i < 0 || j < 0 || j <= i} is true.
   * @throws NullPointerException if {@code execution1} or {@code execution2} is null.
   */
  public static boolean checkConsistency(final int i, final int j, final MmuExecution execution1,
      final MmuExecution execution2, final MmuDependency dependency) {
    InvariantChecks.checkNotNull(execution1);
    InvariantChecks.checkNotNull(execution2);
    // The dependency can be null.

    if (i < 0 || j < 0 || j <= i) {
      throw new IllegalArgumentException();
    }

    final List<MmuExecution> executions = new ArrayList<>();
    executions.add(execution1);
    executions.add(execution2);

    final MmuDependency[][] dependencies = new MmuDependency[2][2];
    dependencies[0][0] = null;
    // Add dependency between the first and second executions.
    dependencies[1][0] = dependency;
    dependencies[0][1] = null;
    dependencies[1][1] = null;

    return checkConsistency(new MmuTemplate(executions, dependencies));
  }

  /**
   * Check consistency of the template.
   *
   * @param template the template.
   * @return {@code true} if the condition is consistent; {@code false} otherwise.
   * @throws NullPointerException if {@code template} is null.
   */
  public static boolean checkConsistency(final MmuTemplate template) {
    InvariantChecks.checkNotNull(template);

    final MmuDependency[][] templateDependency = template.getDependencies();

    // Step 0. Check the dependency combination.
    if (!precheckDependencies(templateDependency)) {
      return false;
    }

    // Initialize class variables.
    initialize();

    final Map<IntegerVariable, Set<IntegerRange>> variableRange = new HashMap<>();

    // Step 1. Add Ranges for constants from dependency.
    for (final MmuDependency[] arrayDependency : templateDependency) {
      for (final MmuDependency dependency : arrayDependency) {
        if (dependency != null) {
          // Init ranges: X = [1 .. a][a+1 .. b][b+1 .. n]
          initVariableRange(dependency, variableRange);
        }
      }
    }

    final List<MmuExecution> templateExecutions = template.getExecutions();

    // Step 2. Add Ranges for constants from execution.
    for (final MmuExecution execution : templateExecutions) {
      // Init ranges: X = [1 .. a][a+1 .. b][b+1 .. n]
      initVariableRange(execution, variableRange);
    }

    // Get list of unique ranges.
    final Map<IntegerVariable, List<IntegerRange>> rangedVariable =
        transformToUniqueRange(variableRange);

    variableRange.clear();
    for (final Map.Entry<IntegerVariable, List<IntegerRange>> variable : rangedVariable.entrySet()) {
      final Set<IntegerRange> rangeSet = new HashSet<>();
      final List<IntegerRange> rangeList = variable.getValue();
      for (IntegerRange range : rangeList) {
        rangeSet.add(range);
      }
      variableRange.put(variable.getKey(), rangeSet);
    }

    // Step 3.
    for (MmuExecution execution : templateExecutions) {
      // Add ranges: X[1 .. n] = Y[1 .. a][a+1 .. b][b+1 .. n]
      addVariableRange(execution, variableRange);
    }

    // Get list of all unique ranges.
    final Map<IntegerVariable, List<IntegerRange>> variables =
        transformToUniqueRange(variableRange);

    // Step 4.
    // If we have linked variables.
    if (!variablesLinkedMap.isEmpty()) {
      linkedUniqueRange(variables);
    }

    int templateExecutionsSize = templateExecutions.size();

    // Step 5.
    for (final Map.Entry<IntegerVariable, List<IntegerRange>> variable : variables.entrySet()) {
      final List<String> nameI = new ArrayList<>();

      for (int i = 0; i < templateExecutionsSize; i++) {
        nameI.add(gatherVariableName(variable.getKey(), i));
        variableRanges.put(nameI.get(i), variable.getValue());
      }

      final List<List<String>> variableLinkI = new ArrayList<>();
      for (int i = 0; i < templateExecutionsSize; i++) {
        variableLinkI.add(new ArrayList<String>());
      }
      for (final IntegerRange range : variable.getValue()) {

        final List<String> variableIRange = new ArrayList<>();

        for (int i = 0; i < templateExecutionsSize; i++) {
          variableIRange.add(gatherVariableName(variable.getKey(), i, range));
          variableLinkI.get(i).add(variableIRange.get(i));
        }

        final List<IntegerVariable> variableI = new ArrayList<>();

        for (int i = 0; i < templateExecutionsSize; i++) {
          variableI.add(new IntegerVariable(variableIRange.get(i), range.size().intValue()));
          mmuVariables.put(variableIRange.get(i), variableI.get(i));
          mmuRanges.put(variableIRange.get(i), range);
        }
      }

      for (int i = 0; i < templateExecutionsSize; i++) {
        variableLink.put(nameI.get(i), variableLinkI.get(i));
      }
    }

    // Step 6.
    // Add equations to the solver.
    final IntegerEquationSolver solver = new IntegerEquationSolver();

    // Add variables to the solver
    for (final Map.Entry<String, IntegerVariable> variable : mmuVariables.entrySet()) {
      solver.addVariable(variable.getValue());
    }

    for (int i = 0; i < templateExecutionsSize; i++) {
      // Get equations from execution.
      for (final MmuTransition transition : templateExecutions.get(i).getTransitions()) {
        if (!process(i, solver, transition)) {
          return false;
        }
      }
    }

    // Step 7.
    for (int i = 0; i < templateExecutionsSize - 1; i++) {
      for (int j = i + 1; j < templateExecutionsSize; j++) {

        MmuDependency dependency = template.getDependency(i, j);
        if (dependency != null) {

         /* for (final MmuConflict conflict : dependency.getConflicts()) {
            if (MmuConflict.Type.TAG_NOT_REPLACED.equals(conflict.getType())) {
              System.out.println("TAG_NOT_REPLACED");
              System.out.println(dependency);
              System.out.println(solver);
            }
          }*/
          // Get equations from dependency of i & j execution.
          if (!process(i, j, solver, templateExecutions.get(i), templateExecutions.get(j),
              dependency)) {
            return false;
          }
        }
      }
    }

    return solver.solve();
  }

  /*
   */
  private static void addConflictsRelation(
      final Map<String, Map<Integer, Set<Integer>>> transitiveRelations, final int iIterator,
      final int jIterator, final MmuDependency dependency) {

    for (final MmuConflict conflict : dependency.getConflicts()) {
      final String conflictName = conflict.getFullName();

      Map<Integer, Set<Integer>> executionsPairs = transitiveRelations.get(conflictName);
      Set<Integer> executionPairI;
      Set<Integer> executionPairJ;
      if (executionsPairs == null) {
        executionsPairs = new HashMap<>();
        executionPairI = new HashSet<>();
        executionPairJ = new HashSet<>();
      } else {
        executionPairI = executionsPairs.get(iIterator);
        if (executionPairI == null) {
          executionPairI = new HashSet<>();
        }

        executionPairJ = executionsPairs.get(jIterator);
        if (executionPairJ == null) {
          executionPairJ = new HashSet<>();
        }
      }

      executionPairI.add(jIterator);
      executionPairJ.add(iIterator);
      executionsPairs.put(iIterator, executionPairI);
      executionsPairs.put(jIterator, executionPairJ);

      transitiveRelations.put(conflictName, executionsPairs);
    }
  }

  /*
   * Checks for the presence of transitive relations in the conflicts.
   */
  private static boolean checkConflictsRelation(
      final Map<String, Map<Integer, Set<Integer>>> transitiveRelations) {
    for (final Map.Entry<String, Map<Integer, Set<Integer>>> transitiveRelation : transitiveRelations
        .entrySet()) {

      final Map<Integer, Set<Integer>> executionsPairs = transitiveRelation.getValue();

      for (final Map.Entry<Integer, Set<Integer>> executionsPair : executionsPairs.entrySet()) {
        final Set<Integer> links = executionsPair.getValue();
        if (links.size() > 1) {
          ArrayList<Integer> arrayLinks = new ArrayList<Integer>(links);
          for (int linkI = 0; linkI < arrayLinks.size() - 1; linkI++) {
            int thisEx = arrayLinks.get(linkI);

            for (int linkJ = linkI + 1; linkJ < arrayLinks.size(); linkJ++) {
              if (!executionsPairs.get(thisEx).contains(arrayLinks.get(linkJ))) {
                return false;
              }
            }
          }
        }
      }
    }

    return true;
  }

  /*
   * Checks the dependency combination.
   * 
   * 1. if TAG_EQUAL && TAG_REPLACED, then the template is inconsistent.
   * 
   * 2. if !(Transitive relations in the conflicts) then template inconsistency.
   */
  private static boolean precheckDependencies(MmuDependency[][] templateDependency) {
    final Map<String, Map<Integer, Set<Integer>>> transitiveRelations = new HashMap<>();

    for (int iIterator = 0; iIterator < templateDependency.length; iIterator++) {
      for (int jIterator = 0; jIterator < templateDependency[iIterator].length; jIterator++) {

        if (templateDependency[iIterator][jIterator] != null) {
          final MmuDependency dependency = templateDependency[iIterator][jIterator];

          // !(TagEqual && TagReplaced)
          final Set<MmuDevice> tagEqualDevices = new LinkedHashSet<>();
          final Set<MmuDevice> tagReplacedDevices = new LinkedHashSet<>();

          for (final MmuConflict conflict : dependency.getConflicts()) {
            switch (conflict.getType()) {
              case TAG_EQUAL:
                tagEqualDevices.add(conflict.getDevice());
                break;
              case TAG_REPLACED:
                tagReplacedDevices.add(conflict.getDevice());
                break;
              default:
                break;
            }
          }

          tagEqualDevices.retainAll(tagReplacedDevices);

          if (!tagEqualDevices.isEmpty()) {
            return false;
          }

          // Add conflict relations
          addConflictsRelation(transitiveRelations, iIterator, jIterator, dependency);
        }
      }
    }

    // Check transitive relation
    if (!checkConflictsRelation(transitiveRelations)) {
      return false;
    }

    return true;
  }

  /*
   * Initialize variables.
   */
  private static void initialize() {
    variableLink.clear();
    variableRanges.clear();

    mmuVariables.clear();
    mmuRanges.clear();

    variablesLinkedMap.clear();
    variablesRangesMap.clear();

    intersectingRanges.clear();
  }

  /**
   * Returns the list of the variables inside the range.
   * 
   * @param i number of execution.
   * @param term contain the ranges.
   * @return list of the variables.
   */
  private static List<IntegerVariable> getVariable(final int i, final IntegerField term) {
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
  private static void initVariableRange(final MmuDependency dependency,
      final Map<IntegerVariable, Set<IntegerRange>> variableRange) {

    final List<MmuConflict> conflicts = dependency.getConflicts();
    for (final MmuConflict conflict : conflicts) {
      final MmuCondition condition = conflict.getCondition();
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
  private static void addVariableRange(final MmuExecution execution,
      final Map<IntegerVariable, Set<IntegerRange>> variableRange) {

    for (final MmuTransition transition : execution.getTransitions()) {
      // Get terms from action
      final MmuAction source = transition.getSource();
      final Map<IntegerVariable, MmuAssignment> actions = source.getAction();

      for (final Map.Entry<IntegerVariable, MmuAssignment> action : actions.entrySet()) {
        final MmuAssignment assignment = action.getValue();

        final MmuExpression expression = assignment.getExpression();
        if (expression != null) {
          // Adds the left part of expression to the variableRange.
          addVariableRange(expression, variableRange, assignment.getVariable());
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
  private static void initVariableRange(final MmuExecution execution,
      final Map<IntegerVariable, Set<IntegerRange>> variableRange) {
    for (final MmuTransition transition : execution.getTransitions()) {

      // Get terms from action
      final MmuAction source = transition.getSource();
      final Map<IntegerVariable, MmuAssignment> actions = source.getAction();

      for (final Map.Entry<IntegerVariable, MmuAssignment> action : actions.entrySet()) {
        final MmuAssignment assignment = action.getValue();

        final MmuExpression expression = assignment.getExpression();
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
  private static void initVariableRange(final MmuCondition condition,
      final Map<IntegerVariable, Set<IntegerRange>> variableRange) {
    final List<MmuEquality> equalities = condition.getEqualities();
    for (final MmuEquality equality : equalities) {
      final MmuExpression expression = equality.getExpression();

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
   * @throws IllegalArgumentException if sum of the sizes of variable terms > variable size.
   */
  private static void addVariableRange(final MmuExpression expression,
      final Map<IntegerVariable, Set<IntegerRange>> variableRange, final IntegerVariable variable) {
    int variableMaxHi = variable.getWidth() - 1;

    Set<IntegerRange> range;
    if (variableRange.containsKey(variable)) {
      range = variableRange.get(variable);
    } else {
      range = new HashSet<>();
      // Init range / Add to range: [0, this width - 1]
      range.add(new IntegerRange(0, variableMaxHi));
    }

    // Get all terms for this variable
    final List<IntegerField> terms = expression.getTerms();

    final Map<IntegerVariable, Set<IntegerRange>> variablesRangesTemp = new HashMap<>();

    int termsSize = 0;
    // Get the shift value for variable
    for (final IntegerField term : terms) {
      final IntegerVariable termVariable = term.getVariable();

      Set<IntegerRange> termRanges;
      if (variablesRangesTemp.containsKey(termVariable)) {
        termRanges = variablesRangesTemp.get(termVariable);
      } else {
        termRanges = new HashSet<>();
      }
      termRanges.add(new IntegerRange(term.getLoIndex(), term.getHiIndex()));
      variablesRangesTemp.put(term.getVariable(), termRanges);

      termsSize += term.getWidth();
    }

    final int variableWidth = variable.getWidth();
    // Get variable shift
    int variableShift = 0;
    int zeroShift = 0;
    if (termsSize < variableWidth) {
      variableShift = variableWidth - termsSize;
      // [[Min .. ] ... [Max - variableShift .. Max]]
      // [Max - variableShift .. Max] = Const = 0
      range.add(new IntegerRange(variableWidth - variableShift, variableWidth - 1));
    } else if (termsSize > variableWidth) {
      throw new IllegalStateException(String.format("The length of the variable is too small: %s",
          variable));
    }

    if (variablesRangesTemp.size() == 1) {
      // V1 [] = V2 []
      final IntegerField baseTerm =
          new IntegerField(variable, zeroShift, variableWidth - 1 - variableShift);

      final Map.Entry<IntegerVariable, Set<IntegerRange>> variableTemp =
          variablesRangesTemp.entrySet().iterator().next();
      final Set<IntegerRange> rangesTemp = variableTemp.getValue();

      int min = 0;
      int max = 0;

      final Set<IntegerRange> baseRangeTemp = new HashSet<>();
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
      variableRange.put(variable, range);

      final IntegerField baseTerm2 = new IntegerField(variableTemp.getKey(), min, max);

      addTermToGlobalList(baseTerm, baseTerm2, baseRangeTemp, rangesTemp);

    } else {
      // V1 [] = V2 [] :: V3 [] ... VN[]
      int incr = 0;
      for (final IntegerField term : terms) {
        // for (final Map.Entry<IntegerVariable, Set<IntegerRange>> variableTemp :
        // variablesRangesTemp
        // .entrySet()) {

        // Set<IntegerRange> variableTemp = variablesRangesTemp.get(term);

        // System.out.println("variablesRangesMap: " + variablesRangesMap);

        final Set<IntegerRange> rangesTemp = variablesRangesTemp.get(term.getVariable());
        int min = 0;
        int max = 0;

        final Set<IntegerRange> baseRangeTemp = new HashSet<>();

        // if (zeroShift == 0) {
        // baseRangeTemp.addAll(rangesTemp);
        // }
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
            new IntegerField(variable, zeroShift + incr, zeroShift + max + incr);
        final IntegerField baseNewTerm = new IntegerField(term.getVariable(), min, max);

        incr += max + 1;
        addTermToGlobalList(baseTerm, baseNewTerm, baseRangeTemp, rangesTemp);
        // Add range to global value
        range.addAll(baseRangeTemp);
        variableRange.put(variable, range);
      }
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
  private static void addTermToGlobalList(final IntegerField baseTerm, final IntegerField term,
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

        final Set<IntegerField> tempSet = new HashSet<>();
        tempSet.add(baseTerm);
        variablesLinkedMap.put(term, tempSet);

        // Add new range
        variablesRangesMap.put(term, ranges);
        variablesRangesMap.put(baseTerm, baseRanges);
      }
    } else {
      // Add new

      final Set<IntegerField> linkedTerms = new HashSet<>();
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
        final Set<IntegerField> tempSet = new HashSet<>();
        tempSet.add(baseTerm);
        variablesLinkedMap.put(term, tempSet);
      }
    }

    Set<IntegerField> tempSet;
    if (intersectingRanges.containsKey(baseTerm.getVariable())) {
      tempSet = intersectingRanges.get(baseTerm.getVariable());
    } else {
      tempSet = new HashSet<>();
    }
    tempSet.add(baseTerm);
    intersectingRanges.put(baseTerm.getVariable(), tempSet);
  }

  /**
   * Link the a unique range of.
   * 
   * @param variables the variables.
   */
  private static void linkedUniqueRange(final Map<IntegerVariable, List<IntegerRange>> variables) {
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
  private static void linkedUniqueRange(final Map<IntegerVariable, List<IntegerRange>> variables,
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
          final Set<IntegerRange> rangeSet = new HashSet<>();
          rangeSet.addAll(variableRanges);
          final Set<IntegerRange> rangeSet2 = new HashSet<>();
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

          linkedUniqueRange(variables, baseTerm);
          final Set<IntegerField> intersecting = intersectingRanges.get(baseTerm.getVariable());
          if ((intersecting != null) && (intersecting.size() > 1)) {
            for (IntegerField intersectingItem : intersecting) {
              final IntegerRange tempRange1 =
                  new IntegerRange(intersectingItem.getLoIndex(), intersectingItem.getHiIndex());
              final IntegerRange tempRange2 =
                  new IntegerRange(baseTerm.getLoIndex(), baseTerm.getHiIndex());
              if (tempRange1.intersect(tempRange2) != null) {
                linkedUniqueRange(variables, intersectingItem);
              }
            }
          }

          // throw new IllegalStateException( String.format("%s =/= %s", rangesList,
          // termRangesList));
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
    final Set<IntegerRange> rangeSet1 = new HashSet<>();
    final Set<IntegerRange> rangeSet2 = new HashSet<>();

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
    final Set<IntegerRange> ranges = new HashSet<>();

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
  private static void initVariableRange(final MmuExpression expression,
      final Map<IntegerVariable, Set<IntegerRange>> variableRange) {

    final List<IntegerField> terms = expression.getTerms();

    for (final IntegerField term : terms) {
      final IntegerVariable variable = term.getVariable();

      Set<IntegerRange> range;
      if (variableRange.containsKey(variable)) {
        range = variableRange.get(variable);
      } else {
        range = new HashSet<>();
        // Init range / Add to range: [0, this width - 1]
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

    final Map<IntegerVariable, List<IntegerRange>> returnVariables = new HashMap<>();

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
  private static BigInteger getRangeConstant(final BigInteger constant, final int lo, final int hi) {
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
   * @param solver the solver.
   * @param equality the equality.
   * @return {@code true} if the equality is consistent; {@code false} otherwise.
   * @throws IllegalStateException if equalityType not EQUAL_CONST || NOT_EQUAL_CONST.
   */
  private static boolean process(final int i, final IntegerEquationSolver solver,
      final MmuEquality equality) {
    final MmuExpression expression = equality.getExpression();

    boolean equalityType;
    switch (equality.getType()) {
      case EQUAL_CONST:
        equalityType = true;
        break;
      case NOT_EQUAL_CONST:
        equalityType = false;
        break;
      default:
        throw new IllegalStateException(String.format("The equality type is not constant: %s",
            equality));
    }

    final List<IntegerField> terms = expression.getTerms();

    final BigInteger equalityConstant = equality.getConstant();
    for (final IntegerField term : terms) {
      // Get variables of the ranges
      final List<IntegerVariable> variableList = getVariable(i, term);

      // Add variables to the solver.
      
      final IntegerEquationSet equationSet = new IntegerEquationSet(equalityType ? IntegerEquationSet.Type.AND : IntegerEquationSet.Type.OR);
      // Add variables to the solver
      for (final IntegerVariable var : variableList) {
        final IntegerRange range = mmuRanges.get(var.getName());
        final int lo = range.getMin().intValue();
        final int hi = range.getMax().intValue();
        // Create constant for solver
        final BigInteger val = getRangeConstant(equalityConstant, lo, hi);
        //solver.addEquation(var, val, equalityType);
        equationSet.addEquation(var, val, equalityType);
      }
      solver.addEquationSet(equationSet);
    }

    return true;
  }

  /**
   * Adds assignments equalities to the solver.
   * 
   * @param i the index of execution.
   * @param solver the solver.
   * @param assignments the assignments.
   * @return {@code true} if the assignments is consistent; {@code false} otherwise.
   * @throws NullPointerException if {@code var} is null.
   * @throws IllegalStateException if ranges size not equal.
   */
  private static boolean process(final int i, final IntegerEquationSolver solver,
      final Map<IntegerVariable, MmuAssignment> assignments) {
    //final boolean equalityType = true;

    for (final Map.Entry<IntegerVariable, MmuAssignment> assignmentSet : assignments.entrySet()) {
      final IntegerVariable variable = assignmentSet.getKey();
      final MmuAssignment assignment = assignmentSet.getValue();

      final String name = gatherVariableName(variable, i);

      final List<IntegerRange> rangesList = variableRanges.get(name);

      final MmuExpression expression = assignment.getExpression();
      if (expression != null) {
        final List<IntegerField> termList = expression.getTerms();

        int termsSize = 0;
        // Get the shift value
        for (final IntegerField term : termList) {
          termsSize += term.getWidth();
        }

        // Get variable shift
        int variableShift = 0;
        final int variableWidth = variable.getWidth();
        if (termsSize < variableWidth) {

          variableShift = variableWidth - termsSize;

          final IntegerRange seachRange =
              new IntegerRange(variableWidth - variableShift, variableWidth - 1);

          final String baseVarName = gatherVariableName(variable, i);
          final List<String> a = variableLink.get(baseVarName);

          for (String b : a) {
            final IntegerRange c = mmuRanges.get(b);
            if (seachRange.contains(c)) {
              final String varName = gatherVariableName(variable, i, c);
              final IntegerVariable var = mmuVariables.get(varName);

              if (var == null) {
                throw new NullPointerException("MmuVariable '" + varName
                    + "' was null inside method 'process'.");
              }

              // Create constant for solver
              final BigInteger val = getRangeConstant(BigInteger.ZERO, 0, c.size().intValue() - 1);
              solver.addEquation(var, val, true);
            }
          }
        }

        int index = 0;
        for (final IntegerField term : termList) {
          List<IntegerVariable> termVariables = getVariable(i, term);

          for (final IntegerVariable termVariable : termVariables) {

            if (index >= rangesList.size()) {
              throw new IllegalStateException("Error: Ranges size not equal.");
            }

            final IntegerRange varRange = rangesList.get(index);
            final IntegerRange var2Range = mmuRanges.get(termVariable.toString());

            if (!varRange.size().equals(var2Range.size())) {
              System.out.format("variable1: %s, %s\n", termVariable.getName(), var2Range);
              System.out.format("variable2: %s, %s, index: %s\n", variable, rangesList, index);
              throw new IllegalStateException("Error: Ranges size not equal: " + varRange + " =/= "
                  + var2Range + ".");
            }

            final IntegerVariable var = mmuVariables.get(gatherVariableName(variable, i, varRange));

            solver.addEquation(var, termVariable, true);
            index++;
          }
        }
      }
    }
    return true;
  }

  /**
   * Adds condition equalities.
   * 
   * @param i the index of execution.
   * @param solver the solver.
   * @param condition the condition.
   * @return {@code true} if the condition is consistent; {@code false} otherwise.
   */
  private static boolean process(final int i, final IntegerEquationSolver solver,
      final MmuCondition condition) {
    final List<MmuEquality> equalities = condition.getEqualities();
    for (final MmuEquality equality : equalities) {
      final MmuExpression expression = equality.getExpression();

      if (expression != null) {
        if (!process(i, solver, equality)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Adds transition equalities.
   * 
   * @param i the index of execution.
   * @param solver the solver.
   * @param transition the transition.
   * @return {@code true} if the transition is solved; {@code false} otherwise.
   */
  private static boolean process(final int i, final IntegerEquationSolver solver,
      final MmuTransition transition) {
    final MmuGuard guard = transition.getGuard();

    if (guard != null) {
      final MmuCondition condition = guard.getCondition();

      if (condition != null) {
        if (!process(i, solver, condition)) {
          return false;
        }
      }
    }

    final MmuAction source = transition.getSource();
    final Map<IntegerVariable, MmuAssignment> assignments = source.getAction();
    if (assignments != null) {
      if (!process(i, solver, assignments)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Adds equalities of the dependency of executions.
   * 
   * @param i the index of the first execution.
   * @param j the index of the second execution.
   * @param solver the solver for the template.
   * @param execution1 the i execution.
   * @param execution2 the j execution.
   * @param dependency the dependency of executions.
   * @return {@code true} if the dependency is solved; {@code false} otherwise.
   */
  private static boolean process(final int i, final int j, final IntegerEquationSolver solver,
      final MmuExecution execution1, final MmuExecution execution2, final MmuDependency dependency) {
    if (dependency == null) {
      return true;
    }

    for (final MmuConflict conflict : dependency.getConflicts()) {
      // !device.isReplaceable() && TAG_EQUAL => execution1.getEvent(device) ==
      // execution2.getEvent(device)
      if (conflict.getType().equals(MmuConflict.Type.TAG_EQUAL)) {
        final MmuDevice device = conflict.getDevice();
        if (!device.isReplaceable()
            && !(execution1.getEvent(device) == execution2.getEvent(device))) {
          return false;
        }

        if (BufferAccessEvent.HIT.equals(execution1.getEvent(device))
            && BufferAccessEvent.HIT.equals(execution2.getEvent(device))) {
          final List<IntegerVariable> fields = device.getFields();

          for (final IntegerVariable field : fields) {
            final String value1 = gatherVariableName(field, i);
            final String value2 = gatherVariableName(field, j);

            List<String> values1 = variableLink.get(value1);
            if (values1 == null) {
              addVariable(field, i, solver);
              values1 = variableLink.get(value1);
            }

            List<String> values2 = variableLink.get(value2);
            if (values2 == null) {
              addVariable(field, j, solver);
              values2 = variableLink.get(value2);
            }

            for (int k = 0; k < values1.size(); k++) {
              final IntegerVariable variable1 = mmuVariables.get(values1.get(k));
              final IntegerVariable variable2 = mmuVariables.get(values2.get(k));

              solver.addEquation(variable1, variable2, true);
            }
          }
        }
      }

      final MmuCondition condition = conflict.getCondition();
      if (condition != null) {
        final List<MmuEquality> equalities = condition.getEqualities();
        for (final MmuEquality equality : equalities) {
          final MmuExpression expression = equality.getExpression();

          if (expression != null) {
            boolean equalityType;
            switch (equality.getType()) {
              case EQUAL:
                equalityType = true;
                break;
              case NOT_EQUAL:
                equalityType = false;
                break;
              case EQUAL_REPLACED:
                // if (j == i + 1)
                final MmuDevice device = conflict.getDevice();
                final BufferAccessEvent event = execution2.getEvent(device);
                if (event.equals(BufferAccessEvent.HIT)) {
                  return false;
                }
                return true;
              default:
                return true;
            }

            final List<IntegerField> terms = expression.getTerms();

            for (final IntegerField term : terms) {
              // Get variables of the ranges.
              final List<IntegerVariable> variableListI = getVariable(i, term);
              final List<IntegerVariable> variableListJ = getVariable(j, term);

              //final IntegerEquationSet equationSet = new IntegerEquationSet(IntegerEquationSet.Type.AND);
              final IntegerEquationSet equationSet = new IntegerEquationSet(equalityType ? IntegerEquationSet.Type.AND : IntegerEquationSet.Type.OR);
              // Add variables to the solver.
              for (int k = 0; k < variableListI.size(); k++) {
                equationSet.addEquation(variableListI.get(k), variableListJ.get(k), equalityType);
              }
              solver.addEquationSet(equationSet);
            }
          }
        }
      }
    }
    return true;
  }

  /*
   * Adds variable.
   * 
   * @param variable the i execution variable.
   * 
   * @param i the index of the execution.
   * 
   * @param solver the solver for the template.
   */

  private static void addVariable(final IntegerVariable variable, final int i,
      final IntegerEquationSolver solver) {
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
    final IntegerVariable solverVariable = new IntegerVariable(value, variable.getWidth());
    mmuVariables.put(value, solverVariable);

    solver.addVariable(solverVariable);
  }

  /**
   * Prints the solver state.
   */
  public static void print() {
    int l = 0;
    for (final Map.Entry<IntegerField, Set<IntegerField>> variable : variablesLinkedMap.entrySet()) {
      l++;
      System.out.format("# %d, MmuTerm: %s, Set<MmuTerm>: %s\n", l, variable.getKey(),
          variable.getValue());
    }
    for (final Map.Entry<IntegerField, Set<IntegerRange>> variable : variablesRangesMap.entrySet()) {
      System.out.format("MmuTerm: %s, Set<MmuRange>: %s\n", variable.getKey(), variable.getValue());
    }
    for (final Map.Entry<IntegerVariable, Set<IntegerField>> variable : intersectingRanges
        .entrySet()) {
      l++;
      System.out.format("# %d, MmuVariable: %s, Set<MmuTerm>: %s\n", l, variable.getKey(),
          variable.getValue());
    }
  }
}
