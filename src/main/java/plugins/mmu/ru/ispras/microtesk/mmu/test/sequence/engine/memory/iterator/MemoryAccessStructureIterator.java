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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
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
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.utils.function.BiPredicate;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.microtesk.utils.function.TriPredicate;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MemoryAccessStructureIterator} implements an iterator of memory access structures, i.e.
 * templates over memory access instructions (loads and stores).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MemoryAccessStructureIterator implements Iterator<MemoryAccessStructure> {
 
  public enum Mode {
    RANDOM,
    EXHAUSTIVE
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

  /** Array of indices in the dependencies array. */
  private final int[][] dependencyIndices;

  /** Sequence of memory accesses */
  private final List<MemoryAccess> accesses = new ArrayList<>();
  /** Matrix of dependencies. */
  private final MemoryDependency[][] dependencies;
  /** Nested lists of dependencies. */
  private final List<List<List<MemoryDependency>>> possibleDependencies = new ArrayList<>();

  /** Iterator of memory access classes. */
  private final Iterator<List<MemoryAccessPath>> accessSkeletonIterator;

  /** Checks the consistency of execution path pairs. */
  private Predicate<MemoryAccessStructure> accessPairChecker;
  /** Checks the consistency of whole test templates. */
  private Predicate<MemoryAccessStructure> structureChecker;

  /** Availability of the value. */
  private boolean hasValue;

  private final Mode mode;

  private final int countLimit;
  private int nextCount;

  private boolean enoughDependencies;

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

    this.dependencyIndices = new int[size][size];
    this.dependencies = new MemoryDependency[size][size];

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

    this.accessSkeletonIterator = mode == Mode.RANDOM
        ? new MemoryAccessIteratorRandom(accessPathChoosers)
        : new MemoryAccessIteratorExhaustive(accessPathChoosers);
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
    Logger.debug("Has value: %b", hasValue);

    nextCount = 0;

    if (hasValue) {
      initDependencies();

      if (!checkStructure()) {
        next();
      } else {
        nextCount++;
      }
    }
  }

  @Override
  public boolean hasValue() {
    return hasValue && (countLimit == -1 || nextCount <= countLimit);
  }

  @Override
  public MemoryAccessStructure value() {
    return new MemoryAccessStructure(accesses, dependencies);
  }

  @Override
  public void next() {
    while (nextStructure()) {
      if (checkStructure()) {
        nextCount++;
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
    initDependencies();

    if (nextAccesses()) {
      return true;
    }
    initAccesses();

    hasValue = false;
    return false;
  }

  //------------------------------------------------------------------------------------------------
  // Initialize/Iterate Memory Dependencies
  //------------------------------------------------------------------------------------------------

  private void initDependencies() {
    for (int i = 0; i < accessTypes.size() - 1; i++) {
      Arrays.fill(dependencyIndices[i], 0);
    }

    enoughDependencies = false;
    assignDependencies();
  }

  private boolean nextDependencies() {
    if (enoughDependencies) {
      return false;
    }

    for (int i = 0; i < possibleDependencies.size(); i++) {
      final List<List<MemoryDependency>> dependenciesI = possibleDependencies.get(i);

      for (int j = 0; j < dependenciesI.size(); j++) {
        final List<MemoryDependency> dependenciesJ = dependenciesI.get(j);

        dependencyIndices[i][j]++;

        if (dependencyIndices[i][j] >= dependenciesJ.size()) {
          dependencyIndices[i][j] = 0;
        } else {
          assignDependencies();

          return true;
        }
      }
    }

    return false;
  }

  private void assignDependencies() {
    for (int i = 0; i < dependencies.length; i++) {
      Arrays.fill(dependencies[i], null);
    }

    for (int i = 0; i < possibleDependencies.size(); i++) {
      final List<List<MemoryDependency>> dependenciesI = possibleDependencies.get(i);

      for (int j = 0; j < dependenciesI.size(); j++) {
        final List<MemoryDependency> dependenciesJ = dependenciesI.get(j);

        if (!dependenciesJ.isEmpty()) {
          dependencies[(accessTypes.size() - 1) - i][(accessTypes.size() - 1) - j] =
              dependenciesJ.get(dependencyIndices[i][j]);
        }
      }
    }
  }

  //------------------------------------------------------------------------------------------------
  // Initialize/Iterate Memory Accesses
  //------------------------------------------------------------------------------------------------

  private boolean initAccesses() {
    accessSkeletonIterator.init();

    if (accessSkeletonIterator.hasValue()) {
      if (assignAccesses()) {
        recalculatePossibleDependencies();
        assignDependencies();
        return true;
      } else {
        return nextAccesses();
      }
    }

    return false;
  }

  private boolean nextAccesses() {
    accessSkeletonIterator.next();

    do {
      if (accessSkeletonIterator.hasValue()) {
        if (assignAccesses()) {
          if (recalculatePossibleDependencies()) {
            assignDependencies();
            return true;
          }
        }

        accessSkeletonIterator.next();
      } else {
        if (countLimit == -1) {
          break;
        }

        accessSkeletonIterator.init();
      }
    } while (true);

    return false;
  }

  private boolean assignAccesses() {
    final List<MemoryAccessPath> accessPaths = accessSkeletonIterator.value();

    accesses.clear();
    for (int i = 0; i < accessTypes.size(); i++) {
      final MemoryAccessType accessType = accessTypes.get(i);
      final MemoryAccessPath accessPath = accessPaths.get(i);

      final MemoryAccess access = MemoryAccess.create(accessType, accessPath);
      accesses.add(access);
    }

    for (final MemoryAccess access : accesses) {
      for (final Predicate<MemoryAccess> filter : filterBuilder.getAccessFilters()) {
        if (!filter.test(access)) {
          return false;
        }
      }
    }

    return true;
  }

  private boolean recalculatePossibleDependencies() {
    possibleDependencies.clear();

    for (int i = accessTypes.size() - 1; i >= 0; i--) {
      final List<List<MemoryDependency>> dependenciesI = new ArrayList<>();

      for (int j = accessTypes.size() - 1; j >= 0; j--) {
        if (i <= j) {
          dependenciesI.add(Collections.<MemoryDependency>emptyList());
        } else {
          final List<MemoryDependency> dependencies =
              getDependencies(accesses.get(i), accesses.get(j));

          if (dependencies.isEmpty()) {
            return false;
          }

          dependenciesI.add(dependencies);
        }
      }

      possibleDependencies.add(dependenciesI);
    }

    return true;
  }

  /**
   * Returns the list of dependencies for the given memory accesses.
   * 
   * @param access1 the primary memory access.
   * @param access2 the secondary memory access.
   * @return the dependencies between the memory accesses.
   */
  private List<MemoryDependency> getDependencies(
      final MemoryAccess access1,
      final MemoryAccess access2) {
    InvariantChecks.checkNotNull(access1);
    InvariantChecks.checkNotNull(access2);

    final Collection<MmuBuffer> buffers1 = access1.getPath().getBuffers();
    final Collection<MmuBuffer> buffers2 = access2.getPath().getBuffers();

    // Intersect the sets of buffers used in the memory accesses.
    final Collection<MmuBuffer> buffers = new ArrayList<>(buffers1);
    buffers.retainAll(buffers2);

    final Set<MmuAddressInstance> addresses = new LinkedHashSet<>();

    for (final MmuBuffer buffer : buffers) {
      addresses.add(buffer.getAddress());
    }

    List<MemoryDependency> addrDependencies = new ArrayList<>();
    for (final MmuAddressInstance address : addresses) {
      final Collection<MemoryHazard> hazards = CoverageExtractor.get().getHazards(address);
      addHazardsToDependencies(addrDependencies, access1, access2, hazards);
    }

    List<MemoryDependency> allDependencies = new ArrayList<>();
    for (final MemoryDependency addrDependency : addrDependencies) {
      final List<MemoryDependency> dependencies = new ArrayList<>();

      dependencies.add(addrDependency);
      for (final MmuBuffer buffer : buffers) {
        final Collection<MemoryHazard> hazards = CoverageExtractor.get().getHazards(buffer);
        addHazardsToDependencies(dependencies, access1, access2, hazards);

        if (dependencies.isEmpty()) {
          break;
        }
      }

      allDependencies.addAll(dependencies);
    }

    return allDependencies;
  }

  /**
   * Extends the dependencies with the hazards.
   * 
   * @param dependencies the dependencies to be extended.
   * @param access1 the primary memory access.
   * @param access2 the secondary memory access.
   * @param hazards the hazards
   * @return the extended list of dependencies.
   */
  private void addHazardsToDependencies(
      final Collection<MemoryDependency> dependencies,
      final MemoryAccess access1,
      final MemoryAccess access2,
      final Collection<MemoryHazard> hazards) {
    InvariantChecks.checkNotNull(dependencies);
    InvariantChecks.checkNotNull(access1);
    InvariantChecks.checkNotNull(access2);
    InvariantChecks.checkNotNull(hazards);

    if (hazards.isEmpty()) {
      return;
    }

    if (dependencies.isEmpty()) {
      dependencies.add(new MemoryDependency());
    }

    final Collection<MemoryDependency> tempDependencies = new ArrayList<>(dependencies);

    dependencies.clear();
    for (final MemoryDependency tempDependency : tempDependencies) {
      for (final MemoryHazard hazard : hazards) {
        final MemoryDependency newDependency = new MemoryDependency(tempDependency);
        newDependency.addHazard(hazard);

        final MemoryAccessStructure structure =
            new MemoryAccessStructure(access1, access2, newDependency);

        if (accessPairChecker.test(structure)) {
          dependencies.add(newDependency);
        }
      }
    }
  }

  //------------------------------------------------------------------------------------------------
  // Filter Registration
  //------------------------------------------------------------------------------------------------

  public void addAccessFilter(
      final Predicate<MemoryAccess> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addAccessFilter(filter);
  }

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
