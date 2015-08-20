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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.classifier.Classifier;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
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
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.mmu.translator.coverage.CoverageExtractor;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.utils.function.BiPredicate;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.microtesk.utils.function.TriPredicate;
import ru.ispras.testbase.knowledge.iterator.IntRangeIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.ProductIterator;

/**
 * {@link MemoryAccessStructureIterator} implements an iterator of memory access structures, i.e.
 * templates over memory access instructions (loads and stores).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MemoryAccessStructureIterator implements Iterator<MemoryAccessStructure> {
  /** Checks the consistency of memory access pairs (template parts) and whole templates. */
  private static final FilterBuilder BASIC_FILTERS = new FilterBuilder();
  static {
    BASIC_FILTERS.addHazardFilter(new FilterNonReplaceableTagEqual());
    BASIC_FILTERS.addUnitedHazardFilter(new FilterHitAndTagReplaced());
    BASIC_FILTERS.addUnitedHazardFilter(new FilterTagEqualTagReplaced());
    BASIC_FILTERS.addUnitedHazardFilter(new FilterMultipleTagReplaced());
    BASIC_FILTERS.addUnitedHazardFilter(new FilterParentMissChildHitOrReplace());
    BASIC_FILTERS.addUnitedDependencyFilter(new FilterHitAndTagReplacedEx());
    BASIC_FILTERS.addUnitedDependencyFilter(new FilterMultipleTagReplacedEx());
    BASIC_FILTERS.addUnitedDependencyFilter(new FilterVaEqualPaNotEqual());
    BASIC_FILTERS.addStructureFilter(new FilterUnclosedEqualRelations());
  }

  /** Checks the consistency of whole templates (not applicable to template parts). */
  private static final FilterBuilder ADVANCED_FILTERS = new FilterBuilder();
  static {
    ADVANCED_FILTERS.addUnitedDependencyFilter(new FilterAccessThenMiss());
  }

  /** Contains the basic filters as well as user-defined ones. */
  private final FilterBuilder filterBuilder = new FilterBuilder(BASIC_FILTERS);

  /** Memory access types (descriptors). */
  private final List<MemoryAccessType> accessTypes;

  /** Memory access equivalence classes. */
  private final List<List<Set<MemoryAccessPath>>> accessPathClasses = new ArrayList<>();

  /** Array of indices in the dependencies array. */
  private final int[][] dependencyIndices;

  /** Sequence of memory accesses */
  private final List<MemoryAccess> accesses = new ArrayList<>();
  /** Matrix of dependencies. */
  private final MemoryDependency[][] dependencies;
  /** Nested lists of dependencies. */
  private final List<List<List<MemoryDependency>>> possibleDependencies = new ArrayList<>();

  /** Iterator of memory access classes. */
  private final Iterator<List<Integer>> accessPathIterator;

  private final GeneratorSettings settings;

  /** Checks the consistency of execution path pairs. */
  private Predicate<MemoryAccessStructure> accessPairChecker;
  /** Checks the consistency of whole test templates. */
  private Predicate<MemoryAccessStructure> structureChecker;

  /** Availability of the value. */
  private boolean hasValue;

  public MemoryAccessStructureIterator(
      final List<MemoryAccessType> accessTypes,
      final Classifier<MemoryAccessPath> classifier,
      final Collection<IntegerConstraint<IntegerField>> constraints,
      final GeneratorSettings settings) {
    InvariantChecks.checkNotNull(accessTypes);
    InvariantChecks.checkNotEmpty(accessTypes);
    InvariantChecks.checkNotNull(classifier);
    // Parameter {@code constraints} can be null.
    // Parameter {@code settings} can be null.
 
    this.accessTypes = accessTypes;

    this.dependencyIndices = new int[accessTypes.size()][accessTypes.size()];
    this.dependencies = new MemoryDependency[accessTypes.size()][accessTypes.size()];

    // Classify the memory access paths and initialize the path iterator.
    final ProductIterator<Integer> accessPathIterator = new ProductIterator<>();

    for (final MemoryAccessType accessType : accessTypes) {
      final Collection<MemoryAccessPath> paths = 
          CoverageExtractor.get().getPaths(MmuTranslator.getSpecification(), accessType);
      final Collection<MemoryAccessPath> feasiblePaths = constraints != null ?
          MemoryEngineUtils.getFeasiblePaths(paths, constraints) : paths;

      final List<Set<MemoryAccessPath>> accessPathClasses = classifier.classify(feasiblePaths);

      this.accessPathClasses.add(accessPathClasses);
      accessPathIterator.registerIterator(new IntRangeIterator(0, accessPathClasses.size() - 1));
    }

    this.accessPathIterator = accessPathIterator;

    this.settings = settings;
  }

  public List<MemoryAccessType> getAccessTypes() {
    return accessTypes;
  }

  @Override
  public void init() {
    // Initialize the filter for checking memory access pairs.
    final Predicate<MemoryAccessStructure> accessPairFilter = filterBuilder.build();
    this.accessPairChecker = new MemoryAccessStructureChecker(accessPairFilter);

    // Initialize the filter for checking whole memory access structures.
    final FilterBuilder structureFilterBuilder = new FilterBuilder(filterBuilder);
    structureFilterBuilder.addFilterBuilder(ADVANCED_FILTERS);

    final Predicate<MemoryAccessStructure> structureFilter = structureFilterBuilder.build();
    this.structureChecker = new MemoryAccessStructureChecker(structureFilter);

    hasValue = true;

    initAccesses();
    initDependencies();

    if (hasValue() && !checkStructure()) {
      next();
    }
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  @Override
  public MemoryAccessStructure value() {
    return new MemoryAccessStructure(accesses, dependencies);
  }

  @Override
  public void next() {
    while (nextStructure()) {
      if (checkStructure()) {
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
    assignDependencies();
  }

  private boolean nextDependencies() {
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

  private void initAccesses() {
    accessPathIterator.init();

    if (accessPathIterator.hasValue()) {
      if (assignAccesses()) {
        recalculatePossibleDependencies();
        assignDependencies();
      } else {
        nextAccesses();
      }
    }
  }

  private boolean nextAccesses() {
    accessPathIterator.next();

    while (accessPathIterator.hasValue()) {
      if (assignAccesses()) {
        if (recalculatePossibleDependencies()) {
          assignDependencies();
          return true;
        }
      }

      accessPathIterator.next();
    }

    return false;
  }

  private boolean assignAccesses() {
    if (!accessPathIterator.hasValue()) {
      return false;
    }

    final List<Integer> accessIndices = accessPathIterator.value();

    accesses.clear();
    for (int i = 0; i < accessTypes.size(); i++) {
      final List<Set<MemoryAccessPath>> classes = accessPathClasses.get(i);
      final Set<MemoryAccessPath> accessPathClass = classes.get(accessIndices.get(i));
      final MemoryAccessType accessType = accessTypes.get(i);
      final MemoryAccessPath accessPath = Randomizer.get().choose(accessPathClass);
      final MemoryAccess access = MemoryAccess.create(accessType, accessPath, settings);

      if (access == null) {
        return false;
      }

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
          dependenciesI.add(new ArrayList<MemoryDependency>());
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

    final Set<MmuAddressType> addresses = new LinkedHashSet<>();

    for (final MmuBuffer buffer : buffers) {
      addresses.add(buffer.getAddress());
    }

    List<MemoryDependency> addrDependencies = new ArrayList<>();
    for (final MmuAddressType address : addresses) {
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
