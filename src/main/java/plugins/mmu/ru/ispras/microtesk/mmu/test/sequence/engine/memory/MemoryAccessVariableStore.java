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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.coverage.CoverageExtractor;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAssignment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * {@link MemoryAccessVariableStore} constructs and stores internal integer variables used by
 * {@link MemoryAccessStructureChecker} to express and check constraints on memory accesses.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
final class MemoryAccessVariableStore {

  private static Map<IntegerVariable, List<IntegerRange>> getDisjointRanges(
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(ranges);

    final Map<IntegerVariable, List<IntegerRange>> uniqueRanges = new LinkedHashMap<>();

    for (final Map.Entry<IntegerVariable, Set<IntegerRange>> entry : ranges.entrySet()) {
      final List<IntegerRange> varRanges = IntegerRange.divide(entry.getValue());
      uniqueRanges.put(entry.getKey(), varRanges);
    }

    return uniqueRanges;
  }

  private static List<List<IntegerRange>> combineRanges(
      final List<IntegerRange> ranges1, final List<IntegerRange> ranges2, final int shift) {
    InvariantChecks.checkNotNull(ranges1);
    InvariantChecks.checkNotNull(ranges2);

    final Set<IntegerRange> rangeSet1 = new LinkedHashSet<>();
    final Set<IntegerRange> rangeSet2 = new LinkedHashSet<>();

    for (final IntegerRange range : ranges1) {
      final int shiftedMin = range.getMin().intValue() + shift;
      final int shiftedMax = range.getMax().intValue() + shift;

      rangeSet1.add(range);
      rangeSet2.add(new IntegerRange(shiftedMin, shiftedMax));
    }

    for (final IntegerRange range : ranges2) {
      final int shiftedMin = range.getMin().intValue() - shift;
      final int shiftedMax = range.getMax().intValue() - shift;

      rangeSet1.add(new IntegerRange(shiftedMin, shiftedMax));
      rangeSet2.add(range);
    }

    final List<IntegerRange> newRanges1 = IntegerRange.divide(rangeSet1);
    final List<IntegerRange> newRanges2 = IntegerRange.divide(rangeSet2);

    final List<List<IntegerRange>> result = new ArrayList<>();

    result.add(newRanges1);
    result.add(newRanges2);

    return result;
  }

  /**
   * Maps a left-hand side field to the set of right-hand side fields used in the assignments.
   * 
   * <p>
   * If there are assignments {@code V[A:B] = V1[A1:B1], ..., V[A:B] = Vk[Ak:Bk]}, then
   * {@code assignments(V[A:B]) = {V1[A1:B1], ..., Vk[Ak:Bk]}.
   * </p>
   */
  private final Map<IntegerField, Set<IntegerField>> fieldAssignments = new LinkedHashMap<>();

  /**
   * Maps a field into the set of its ranges used in the expressions.
   */
  private final Map<IntegerField, Set<IntegerRange>> fieldRanges = new LinkedHashMap<>();

  /**
   * Maps a variable into the set of its fields used in the expressions (the fields may overlap).
   */
  private final Map<IntegerVariable, Set<IntegerField>> variableFields = new LinkedHashMap<>();

  /**
   * Maps a variable into the ordered partition of its bit range (the ranges are disjoint).
   */
  private final Map<IntegerVariable, List<IntegerRange>> disjointRanges = new LinkedHashMap<>();

  /**
   * Constructs a variable store for all possible memory accesses and hazards of the given memory
   * subsystem.
   * 
   * @param memory the memory subsystem.
   */
  public MemoryAccessVariableStore(final MmuSubsystem memory) {
    InvariantChecks.checkNotNull(memory);
    initRanges(
        CoverageExtractor.get().getPaths(memory, null), CoverageExtractor.get().getHazards(memory));
  }

  /**
   * Constructs a variable store for the given memory access structure.
   * 
   * @param structure the memory access structure.
   */
  public MemoryAccessVariableStore(final MemoryAccessStructure structure) {
    InvariantChecks.checkNotNull(structure);

    final Collection<MemoryHazard> hazards = new ArrayList<>();

    for (int i = 0; i < structure.size() - 1; i++) {
      for (int j = i + 1; j < structure.size(); j++) {
        final MemoryDependency dependency = structure.getDependency(i, j);

        if (dependency != null) {
          hazards.addAll(dependency.getHazards());
        }
      }
    }

    final Collection<MemoryAccessPath> paths = new ArrayList<>();

    for (final MemoryAccess access : structure.getAccesses()) {
      paths.add(access.getPath());
    }

    initRanges(paths, hazards);
  }

  public Map<IntegerVariable, List<IntegerRange>> getDisjointRanges() {
    return disjointRanges;
  }

  private void initRanges(
      final Collection<MemoryAccessPath> paths, final Collection<MemoryHazard> hazards) {
    InvariantChecks.checkNotNull(paths);
    InvariantChecks.checkNotNull(hazards);

    final Map<IntegerVariable, Set<IntegerRange>> ranges = new LinkedHashMap<>();

    for (final MemoryHazard hazard : hazards) {
      initRanges(hazard, ranges);
    }

    for (final MemoryAccessPath path : paths) {
      initRanges(path, ranges);
    }

    final Map<IntegerVariable, List<IntegerRange>> disjointRanges1 = getDisjointRanges(ranges);

    ranges.clear();
    for (final Map.Entry<IntegerVariable, List<IntegerRange>> entry : disjointRanges1.entrySet()) {
      // Convert map values from lists to sets.
      ranges.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
    }

    for (final MemoryAccessPath path : paths) {
      addRanges(path, ranges);
    }

    final Map<IntegerVariable, List<IntegerRange>> disjointRanges2 = getDisjointRanges(ranges);

    for (final IntegerField field : fieldAssignments.keySet()) {
      linkedUniqueRange(disjointRanges2, field);
    }

    disjointRanges.putAll(disjointRanges2);
  }

  private void initRanges(
      final MemoryAccessPath path,
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(ranges);

    for (final MmuTransition transition : path.getTransitions()) {
      initRanges(transition, ranges);
    }
  }

  private void initRanges(
      final MmuTransition transition,
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(transition);
    InvariantChecks.checkNotNull(ranges);

    initRanges(transition.getSource(), ranges);

    final MmuGuard guard = transition.getGuard();
    if (guard != null) {
      initRanges(guard, ranges);
    }
  }

  private void initRanges(
      final MmuAction action,
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(action);
    InvariantChecks.checkNotNull(ranges);

    final Map<IntegerField, MmuAssignment> assignments = action.getAction();
    for (final Map.Entry<IntegerField, MmuAssignment> entry : assignments.entrySet()) {
      final MmuAssignment assignment = entry.getValue();

      final MmuExpression expression = assignment.getRhs();
      if (expression != null) {
        initRanges(expression, ranges);
      }
    }
  }

  private void initRanges(
      final MmuGuard guard,
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(guard);
    InvariantChecks.checkNotNull(ranges);

    final MmuCondition condition = guard.getCondition();

    if (condition != null) {
      initRanges(condition, ranges);
    }
  }

  private void initRanges(
      final MemoryHazard hazard,
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(hazard);
    InvariantChecks.checkNotNull(ranges);

    final MmuCondition condition = hazard.getCondition();
    if (condition != null) {
      initRanges(condition, ranges);
    }
  }

  private void initRanges(
      final MmuCondition condition,
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(condition);
    InvariantChecks.checkNotNull(ranges);

    final List<MmuConditionAtom> atoms = condition.getAtoms();
    for (final MmuConditionAtom atom : atoms) {
      final MmuExpression expression = atom.getExpression();

      if (expression != null) {
        initRanges(expression, ranges);
      }
    }
  }

  private void initRanges(
      final MmuExpression expression,
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(expression);
    InvariantChecks.checkNotNull(ranges);

    final List<IntegerField> terms = expression.getTerms();

    for (final IntegerField term : terms) {
      final IntegerVariable variable = term.getVariable();

      Set<IntegerRange> varRanges = ranges.get(variable);
      if (varRanges == null) {
        varRanges = new LinkedHashSet<>();
        varRanges.add(new IntegerRange(0, variable.getWidth() - 1));
        ranges.put(variable, varRanges);
      }

      final IntegerRange termRange = new IntegerRange(term.getLoIndex(), term.getHiIndex());
      varRanges.add(termRange);
    }
  }

  private void addRanges(
      final MemoryAccessPath path,
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(ranges);

    for (final MmuTransition transition : path.getTransitions()) {
      addRanges(transition, ranges);
    }
  }

  private void addRanges(
      final MmuTransition transition,
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(transition);
    InvariantChecks.checkNotNull(ranges);

    addRanges(transition.getSource(), ranges);
  }

  private void addRanges(
      final MmuAction action,
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(action);
    InvariantChecks.checkNotNull(ranges);

    final Map<IntegerField, MmuAssignment> assignments = action.getAction();

    for (final MmuAssignment assignment : assignments.values()) {
      final MmuExpression expression = assignment.getRhs();

      if (expression != null) {
        addRanges(expression, assignment.getLhs(), ranges);
      }
    }
  }

  private void addRanges(
      final MmuExpression expression,
      final IntegerField lhs,
      final Map<IntegerVariable, Set<IntegerRange>> ranges) {
    InvariantChecks.checkNotNull(expression);
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(ranges);

    Set<IntegerRange> range;

    if (ranges.containsKey(lhs.getVariable())) {
      range = ranges.get(lhs.getVariable());
    } else {
      range = new LinkedHashSet<>();
      // Init range / Add to range: [0, this width - 1]
      range.add(new IntegerRange(lhs.getLoIndex(), lhs.getHiIndex()));
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

    final int variableWidth = lhs.getWidth();

    // Get variable shift
    int variableShift = 0;
    int zeroShift = lhs.getLoIndex();

    if (termsSize < variableWidth) {
      variableShift = variableWidth - termsSize;
      // [[Min .. ] ... [Max - variableShift .. Max]]
      // [Max - variableShift .. Max] = Const = 0
      range.add(new IntegerRange(
          zeroShift + variableWidth - variableShift, zeroShift + variableWidth - 1));
    } else if (termsSize > variableWidth) {
      throw new IllegalStateException(
          String.format("The length of the variable is too small: %s", lhs));
    }

    if (variablesRangesTemp.size() == 1) {
      // V1 [] = V2 []
      final IntegerField baseTerm =
          new IntegerField(
              lhs.getVariable(), zeroShift, zeroShift + variableWidth - 1 - variableShift);

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
      ranges.put(lhs.getVariable(), range);

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
            new IntegerField(lhs.getVariable(), zeroShift + incr, zeroShift + max + incr);

        final IntegerField baseNewTerm = new IntegerField(term.getVariable(), min, max);

        incr += max + 1;
        addTermToGlobalList(baseTerm, baseNewTerm, baseRangeTemp, rangesTemp);
        // Add range to global value
        range.addAll(baseRangeTemp);
      }
      ranges.put(lhs.getVariable(), range);
    }
  }

  private void addTermToGlobalList(final IntegerField baseTerm, final IntegerField term,
      final Set<IntegerRange> baseRanges, final Set<IntegerRange> ranges) {
    if (fieldAssignments.containsKey(baseTerm)) {
      final Set<IntegerField> linkedTerms = fieldAssignments.get(baseTerm);

      // We have link to this range
      if (linkedTerms.contains(term)) {
        final Set<IntegerRange> tempRanges = fieldRanges.get(term);
        tempRanges.addAll(ranges);
      } else {
        linkedTerms.add(term);
        fieldAssignments.put(baseTerm, linkedTerms);

        final Set<IntegerField> tempSet = new LinkedHashSet<>();
        tempSet.add(baseTerm);
        fieldAssignments.put(term, tempSet);

        // Add new range
        fieldRanges.put(term, ranges);
        fieldRanges.put(baseTerm, baseRanges);
      }
    } else {
      // Add new
      final Set<IntegerField> linkedTerms = new LinkedHashSet<>();
      linkedTerms.add(term);
      fieldAssignments.put(baseTerm, linkedTerms);
      fieldRanges.put(baseTerm, baseRanges);

      if (fieldAssignments.containsKey(term)) {
        final Set<IntegerRange> tempRanges = fieldRanges.get(term);
        tempRanges.addAll(ranges);

        final Set<IntegerField> tempSet = fieldAssignments.get(term);
        tempSet.add(baseTerm);
      } else {
        // Add new linked value[] to map
        fieldRanges.put(term, ranges);

        // Add new link for this value[] to map
        final Set<IntegerField> tempSet = new LinkedHashSet<>();
        tempSet.add(baseTerm);
        fieldAssignments.put(term, tempSet);
      }
    }

    Set<IntegerField> tempSet;
    if (variableFields.containsKey(baseTerm.getVariable())) {
      tempSet = variableFields.get(baseTerm.getVariable());
    } else {
      tempSet = new LinkedHashSet<>();
    }

    tempSet.add(baseTerm);
    variableFields.put(baseTerm.getVariable(), tempSet);

  }

  private void linkedUniqueRange(
      final Map<IntegerVariable, List<IntegerRange>> disjointRanges, final IntegerField baseTerm) {
    InvariantChecks.checkNotNull(disjointRanges);
    InvariantChecks.checkNotNull(baseTerm);

    final IntegerRange baseRange = new IntegerRange(baseTerm.getLoIndex(), baseTerm.getHiIndex());
    List<IntegerRange> variableRanges = disjointRanges.get(baseTerm.getVariable());

    if (variableRanges != null) {
      final Collection<IntegerRange> baseRanges = IntegerRange.select(variableRanges, baseRange);
      final List<IntegerRange> rangesList = IntegerRange.divide(baseRanges);

      for (final IntegerField term : fieldAssignments.get(baseTerm)) {
        int shift = term.getLoIndex() - baseTerm.getLoIndex();
        List<IntegerRange> termVariableRanges = disjointRanges.get(term.getVariable());
        final IntegerRange termRange = new IntegerRange(term.getLoIndex(), term.getHiIndex());
        final Collection<IntegerRange> termRanges = IntegerRange.select(termVariableRanges, termRange);
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

          disjointRanges.put(baseTerm.getVariable(), variableRanges);
          disjointRanges.put(term.getVariable(), termVariableRanges);

          updateUniqueRange(disjointRanges, baseTerm);
          updateUniqueRange(disjointRanges, term);
        }
      }
    }
  }

  private void updateUniqueRange(
      final Map<IntegerVariable, List<IntegerRange>> disjointRanges, final IntegerField term) {
    InvariantChecks.checkNotNull(disjointRanges);
    InvariantChecks.checkNotNull(term);

    linkedUniqueRange(disjointRanges, term);
    final Set<IntegerField> intersecting = variableFields.get(term.getVariable());
    if ((intersecting != null) && (intersecting.size() > 1)) {
      for (IntegerField intersectingItem : intersecting) {
        final IntegerRange tempRange1 =
            new IntegerRange(intersectingItem.getLoIndex(), intersectingItem.getHiIndex());
        final IntegerRange tempRange2 = new IntegerRange(term.getLoIndex(), term.getHiIndex());
        if (tempRange1.intersect(tempRange2) != null) {
          linkedUniqueRange(disjointRanges, intersectingItem);
        }
      }
    }
  }
}
