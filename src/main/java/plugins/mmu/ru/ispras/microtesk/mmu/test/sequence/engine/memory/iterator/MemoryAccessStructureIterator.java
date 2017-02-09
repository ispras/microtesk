/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryHazard;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryUnitedDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryUnitedHazard;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterAccessThenMiss;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterBuilder;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterHitAndTagReplaced;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterHitAndTagReplacedEx;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterMultipleTagReplaced;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterMultipleTagReplacedEx;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterNonReplaceableTagEqual;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterParentMissChildHitOrReplace;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterTagEqualTagReplaced;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterUnclosedEqualRelations;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterVaEqualPaNotEqual;
import ru.ispras.microtesk.mmu.translator.coverage.CoverageExtractor;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryAccessPathChooser;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryGraphAbstraction;
import ru.ispras.microtesk.utils.function.BiPredicate;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.microtesk.utils.function.TriPredicate;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MemoryAccessStructureIterator} implements an iterator of memory access structures, i.e.
 * sequences of memory access paths connected with dependencies.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MemoryAccessStructureIterator implements Iterator<MemoryAccessStructure> {
 
  public enum Mode {
    RANDOM() {
      @Override
      public MemoryAccessIterator getAccessIterator(
          final List<MemoryAccessType> accessTypes,
          final List<Collection<MemoryAccessPathChooser>> accessPathChoosers) {
        return new MemoryAccessIteratorRandom(accessTypes, accessPathChoosers);
      }

      @Override
      public MemoryDependencyIterator getDependencyIterator(
          final MemoryAccess access1,
          final MemoryAccess access2,
          final Predicate<MemoryAccessStructure> checker) {
        return new MemoryDependencyIteratorRandom(access1, access2, checker);
      }
    },

    EXHAUSTIVE() {
      @Override
      public MemoryAccessIterator getAccessIterator(
          final List<MemoryAccessType> accessTypes,
          final List<Collection<MemoryAccessPathChooser>> accessPathChoosers) {
        return new MemoryAccessIteratorExhaustive(accessTypes, accessPathChoosers);
      }

      @Override
      public MemoryDependencyIterator getDependencyIterator(
          final MemoryAccess access1,
          final MemoryAccess access2,
          final Predicate<MemoryAccessStructure> checker) {
        return new MemoryDependencyIteratorExhaustive(access1, access2, checker);
      }
    };

    public abstract MemoryAccessIterator getAccessIterator(
        final List<MemoryAccessType> accessTypes,
        final List<Collection<MemoryAccessPathChooser>> accessPathChoosers);

    public abstract MemoryDependencyIterator getDependencyIterator(
        final MemoryAccess access1,
        final MemoryAccess access2,
        final Predicate<MemoryAccessStructure> checker);
  }

  /** Checks the consistency of memory access pairs (template parts) and whole templates. */
  private final FilterBuilder basicFilters = new FilterBuilder();
  {
    basicFilters.addHazardFilter(new FilterNonReplaceableTagEqual());
    basicFilters.addUnitedHazardFilter(new FilterHitAndTagReplaced());
    basicFilters.addUnitedHazardFilter(new FilterTagEqualTagReplaced());
    basicFilters.addUnitedHazardFilter(new FilterMultipleTagReplaced());
    basicFilters.addUnitedHazardFilter(new FilterParentMissChildHitOrReplace());
    basicFilters.addUnitedDependencyFilter(new FilterHitAndTagReplacedEx());
    basicFilters.addUnitedDependencyFilter(new FilterMultipleTagReplacedEx());
    basicFilters.addUnitedDependencyFilter(new FilterVaEqualPaNotEqual());
    basicFilters.addStructureFilter(new FilterUnclosedEqualRelations());
  }

  /** Checks the consistency of whole templates (not applicable to template parts). */
  private final FilterBuilder advancedFilters = new FilterBuilder();
  {
    advancedFilters.addUnitedDependencyFilter(new FilterAccessThenMiss());
  }

  /** Contains the basic filters as well as user-defined ones. */
  private final FilterBuilder filterBuilder = new FilterBuilder(basicFilters);

  /** Memory access types (descriptors). */
  private final List<MemoryAccessType> accessTypes;

  private final MemoryAccessIterator accessIterator;
  private final MemoryDependencyIterator[][] dependencyIterators;

  /** Checks the consistency of execution path pairs. */
  private Predicate<MemoryAccessStructure> accessPairChecker;
  /** Checks the consistency of whole test templates. */
  private Predicate<MemoryAccessStructure> structureChecker;

  private final Mode mode;
  private final int countLimit;

  private boolean hasValue;
  private int count;
  private boolean enoughDependencies;

  private List<MemoryAccess> accesses;
  private MemoryDependency[][] dependencies;

  public MemoryAccessStructureIterator(
      final MemoryGraphAbstraction abstraction,
      final List<MemoryAccessType> accessTypes,
      final List<MemoryAccessConstraints> accessConstraints,
      final MemoryAccessConstraints constraints,
      final Mode mode,
      final int countLimit) {
    InvariantChecks.checkNotNull(abstraction);
    InvariantChecks.checkNotNull(accessTypes);
    InvariantChecks.checkTrue(
        accessConstraints == null || accessTypes.size() == accessConstraints.size());
    InvariantChecks.checkNotNull(constraints);
    InvariantChecks.checkNotEmpty(accessTypes);
    InvariantChecks.checkTrue(countLimit == -1 || countLimit >= 0);
    InvariantChecks.checkNotNull(mode);

    final int size = accessTypes.size();

    this.accessTypes = accessTypes;
    this.mode = mode;
    this.countLimit = countLimit;

    final List<Collection<MemoryAccessPathChooser>> accessPathChoosers = new ArrayList<>(size);

    int index = 0;
    for (final MemoryAccessType accessType : accessTypes) {
      final MemoryAccessConstraints currentConstraints = MemoryAccessConstraints.merge(
          constraints, accessConstraints != null ? accessConstraints.get(index) : null);

      final List<MemoryAccessPathChooser> choosers = CoverageExtractor.get().getPathChoosers(
          MmuPlugin.getSpecification(), abstraction, accessType, currentConstraints, false);

      InvariantChecks.checkTrue(choosers != null && !choosers.isEmpty());
      Logger.debug("Classifying memory access paths: %s %d classes", accessType, choosers.size());

      accessPathChoosers.add(choosers);
      index++;
    }

    this.accessIterator = mode.getAccessIterator(accessTypes, accessPathChoosers);
    this.dependencyIterators = new MemoryDependencyIterator[size][size];

    this.dependencies = new MemoryDependency[size][size];
  }

  @Override
  public void init() {
    // Initialize the filter for checking memory access pairs.
    final Predicate<MemoryAccessStructure> accessPairFilter = filterBuilder.build();
    this.accessPairChecker = new MemoryAccessStructureChecker(accessPairFilter);

    // Initialize the filter for checking whole memory access structures.
    final FilterBuilder structureFilterBuilder = new FilterBuilder(filterBuilder);
    structureFilterBuilder.addFilterBuilder(advancedFilters);

    final Predicate<MemoryAccessStructure> structureFilter = structureFilterBuilder.build();
    this.structureChecker = new MemoryAccessStructureChecker(structureFilter);

    hasValue = initAccesses();

    while (hasValue && !checkStructure()) {
      hasValue = nextStructure();
    }

    count = 0;
  }

  @Override
  public boolean hasValue() {
    return hasValue && (countLimit == -1 || count < countLimit);
  }

  @Override
  public MemoryAccessStructure value() {
    return new MemoryAccessStructure(accesses, dependencies);
  }

  @Override
  public void next() {
    while (nextStructure()) {
      if (checkStructure()) {
        count++;
        enoughDependencies = (mode == Mode.RANDOM);
        break;
      }
    }
  }

  @Override
  public void stop() {
    hasValue = false;
  }

  @Override
  public MemoryAccessStructureIterator clone() {
    throw new UnsupportedOperationException();
  }

  private boolean checkStructure() {
    final MemoryAccessStructure structure = new MemoryAccessStructure(accesses, dependencies);
    return structureChecker.test(structure);
  }

  private boolean nextStructure() {
    if (nextDependencies()) {
      return true;
    }

    if (nextAccesses()) {
      return true;
    }

    hasValue = false;
    return false;
  }

  private boolean initDependencies() {
    final int size = accessTypes.size();

    for (int i = 0; i < size - 1; i++) {
      final MemoryAccess access1 = accesses.get(i);

      for (int j = i + 1; j < size; j++) {
        final MemoryAccess access2 = accesses.get(j);

        dependencyIterators[i][j] = mode.getDependencyIterator(access1, access2, accessPairChecker);
        dependencyIterators[i][j].init();

        if (!dependencyIterators[i][j].hasValue()) {
          return false;
        }
      }
    }

    enoughDependencies = false;

    assignDependencies();
    return true;
  }

  private boolean nextDependencies() {
    if (enoughDependencies) {
      return false;
    }

    final int size = accessTypes.size();

    for (int i = 0; i < size - 1; i++) {
      for (int j = i + 1; j < size; j++) {
        final MemoryDependencyIterator iterator = dependencyIterators[i][j];

        if (iterator.hasValue()) {
          iterator.next();

          if (iterator.hasValue()) {
            assignDependencies();
            return true;
          }

          iterator.init();
        }
      }
    }

    return false;
  }

  private void assignDependencies() {
    final int size = accessTypes.size();

    for (int i = 0; i < size - 1; i++) {
      for (int j = i + 1; j < size; j++) {
        dependencies[i][j] = dependencyIterators[i][j].value();
      }
    }
  }

  private boolean initAccesses() {
    accessIterator.init();

    if (accessIterator.hasValue()) {
      accesses = accessIterator.value();

      if (initDependencies()) {
        return true;
      }
    }

    return false;
  }

  private boolean nextAccesses() {
    accessIterator.next();

    do {
      if (accessIterator.hasValue()) {
        accesses = accessIterator.value();

        if (initDependencies()) {
          return true;
        }

        accessIterator.next();
      } else {
        if (countLimit == -1) {
          break;
        }

        accessIterator.init();
      }
    } while (true);

    return false;
  }

  //------------------------------------------------------------------------------------------------
  // Filter Registration
  //------------------------------------------------------------------------------------------------

  public void addHazardFilter(
      final TriPredicate<MemoryAccess, MemoryAccess, MemoryHazard> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addHazardFilter(filter);
  }

  public void addDependencyFilter(
      final TriPredicate<MemoryAccess, MemoryAccess, MemoryDependency> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addDependencyFilter(filter);
  }

  public void addUnitedHazardFilter(
      final BiPredicate<MemoryAccess, MemoryUnitedHazard> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addUnitedHazardFilter(filter);
  }

  public void addUnitedDependencyFilter(
      final BiPredicate<MemoryAccess, MemoryUnitedDependency> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addUnitedDependencyFilter(filter);
  }

  public void addStructureFilter(
      final Predicate<MemoryAccessStructure> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addStructureFilter(filter);
  }

  public void addFilterBuilder(final FilterBuilder filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addFilterBuilder(filter);
  }
}
