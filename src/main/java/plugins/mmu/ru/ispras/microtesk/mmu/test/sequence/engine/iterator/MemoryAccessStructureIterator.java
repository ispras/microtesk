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

package ru.ispras.microtesk.mmu.test.sequence.engine.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.Classifier;
import ru.ispras.microtesk.mmu.test.sequence.engine.filter.FilterAccessThenMiss;
import ru.ispras.microtesk.mmu.test.sequence.engine.filter.FilterBuilder;
import ru.ispras.microtesk.mmu.test.sequence.engine.filter.FilterHitAndTagReplaced;
import ru.ispras.microtesk.mmu.test.sequence.engine.filter.FilterHitAndTagReplacedEx;
import ru.ispras.microtesk.mmu.test.sequence.engine.filter.FilterMultipleTagReplaced;
import ru.ispras.microtesk.mmu.test.sequence.engine.filter.FilterMultipleTagReplacedEx;
import ru.ispras.microtesk.mmu.test.sequence.engine.filter.FilterNonReplaceableTagEqual;
import ru.ispras.microtesk.mmu.test.sequence.engine.filter.FilterParentMissChildHitOrReplace;
import ru.ispras.microtesk.mmu.test.sequence.engine.filter.FilterTagEqualTagReplaced;
import ru.ispras.microtesk.mmu.test.sequence.engine.filter.FilterUnclosedEqualRelations;
import ru.ispras.microtesk.mmu.test.sequence.engine.filter.FilterVaEqualPaNotEqual;
import ru.ispras.microtesk.mmu.translator.coverage.CoverageExtractor;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryAccess;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryDependency;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryHazard;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryUnitedDependency;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryUnitedHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.DataType;
import ru.ispras.microtesk.utils.function.BiPredicate;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.microtesk.utils.function.TriPredicate;
import ru.ispras.testbase.knowledge.iterator.ArrayIterator;
import ru.ispras.testbase.knowledge.iterator.IntRangeIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.ProductIterator;
import ru.ispras.testbase.knowledge.iterator.RandomValueIterator;

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
    BASIC_FILTERS.addTemplateFilter(new FilterUnclosedEqualRelations());
  }

  /** Checks the consistency of whole templates (not applicable to template parts). */
  private static final FilterBuilder ADVANCED_FILTERS = new FilterBuilder();
  static {
    ADVANCED_FILTERS.addUnitedDependencyFilter(new FilterAccessThenMiss());
  }

  /** Contains the basic filters as well as user-defined ones. */
  private final FilterBuilder filterBuilder = new FilterBuilder(BASIC_FILTERS);

  /** Memory subsystem specification. */
  private final MmuSubsystem memory;

  /** Memory access equivalence classes. */
  private final List<Set<MemoryAccess>> accessClasses;

  /** Number of memory accesses in a structure. */
  private final int size;

  /** Array of indices in the dependencies array. */
  private final int[][] dependencyIndices;

  /** Sequence of memory accesses */
  private final List<MemoryAccess> accesses = new ArrayList<>();
  /** Matrix of dependencies. */
  private final MemoryDependency[][] dependencies;
  /** Nested lists of dependencies. */
  private final List<List<List<MemoryDependency>>> possibleDependencies = new ArrayList<>();

  /** Iterator of data types. */
  private final Iterator<List<DataType>> dataTypeIterator;
  /** Iterator of memory access classes. */
  private final Iterator<List<Integer>> accessIterator;

  /** Checks the consistency of execution path pairs. */
  private Predicate<MemoryAccessStructure> accessPairFilter;
  /** Checks the consistency of whole test templates. */
  private Predicate<MemoryAccessStructure> structureFilter;

  /** Availability of the value. */
  private boolean hasValue;

  /**
   * Constructs an iterator of memory access structures.
   * 
   * @param memory the memory subsystem specification.
   * @param size the number of memory accesses in a structure.
   * @param dataTypes the array of supported data types.
   * @param randomDataType the data type randomization option.
   * @param classifier the memory access classification policy.
   */
  public MemoryAccessStructureIterator(
      final MmuSubsystem memory,
      final DataType[] dataTypes,
      final boolean randomDataType,
      final int size,
      final Classifier<MemoryAccess> classifier) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(dataTypes);
    InvariantChecks.checkNotNull(classifier);
    InvariantChecks.checkTrue(size > 0);
 
    this.memory = memory;
    this.size = size;

    this.dependencyIndices = new int[size][size];
    this.dependencies = new MemoryDependency[size][size];

    final List<MemoryAccess> accesses = CoverageExtractor.get().getCoverage(memory);
    this.accessClasses = classifier.classify(accesses);

    // Initialize the data type iterator.
    final ProductIterator<DataType> dataTypeIterator = new ProductIterator<>();
    for (int i = 0; i < size; i++) {
      dataTypeIterator.registerIterator(randomDataType ?
          new RandomValueIterator<DataType>(dataTypes) : new ArrayIterator<DataType>(dataTypes));
    }
    this.dataTypeIterator = dataTypeIterator;

    // Initialize the memory access iterator.
    final ProductIterator<Integer> accessIterator = new ProductIterator<>();
    for (int i = 0; i < size; i++) {
      accessIterator.registerIterator(new IntRangeIterator(0, accessClasses.size() - 1));
    }
    this.accessIterator = accessIterator;

    init();
  }


  @Override
  public void init() {
    // Initialize the filter for checking memory access pairs.
    this.accessPairFilter = filterBuilder.build();
    // Initialize the filter for checking whole memory access structures.
    final FilterBuilder structureFilterBuilder = new FilterBuilder(filterBuilder);

    structureFilterBuilder.addFilterBuilder(ADVANCED_FILTERS);
    this.structureFilter = structureFilterBuilder.build();

    hasValue = true;

    initAccesses();
    initDataTypes();
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
    return new MemoryAccessStructure(memory, accesses, dependencies);
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
    final MemoryAccessStructure abstractSequence =
        new MemoryAccessStructure(memory, accesses, dependencies);
    final MemoryAccessStructureChecker abstractSequenceChecker =
        new MemoryAccessStructureChecker(abstractSequence, structureFilter);

    return abstractSequenceChecker.check();
  }

  private boolean nextStructure() {
    if (nextDataTypes()) {
      return true;
    }
    initDataTypes();
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
  // Initialize/Iterate Data Types
  //------------------------------------------------------------------------------------------------

  private void initDataTypes() {
    dataTypeIterator.init();
    assignDataTypes();
  }

  private boolean nextDataTypes() {
    if (dataTypeIterator.hasValue()) {
      dataTypeIterator.next();

      if (dataTypeIterator.hasValue()) {
        assignDataTypes();
        return true;
      }
    }

    return false;
  }

  private void assignDataTypes() {
    final List<DataType> dataTypes = dataTypeIterator.value();

    for (int i = 0; i < size; i++) {
      final MemoryAccess access = accesses.get(i);
      access.setType(dataTypes.get(i));
    }
  }

  //------------------------------------------------------------------------------------------------
  // Initialize/Iterate Memory Dependencies
  //------------------------------------------------------------------------------------------------

  private void initDependencies() {
    for (int i = 0; i < size - 1; i++) {
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
          dependencies[(size-1) - i][(size-1) - j] = dependenciesJ.get(dependencyIndices[i][j]);
        }
      }
    }
  }

  //------------------------------------------------------------------------------------------------
  // Initialize/Iterate Memory Accesses
  //------------------------------------------------------------------------------------------------

  private void initAccesses() {
    accessIterator.init();

    assignAccesses();
    assignDataTypes();
    recalculatePossibleDependencies();
    assignDependencies();
  }

  private boolean nextAccesses() {
    accessIterator.next();

    while (accessIterator.hasValue()) {
      assignAccesses();
      assignDataTypes();
      if (recalculatePossibleDependencies()) {
        assignDependencies();
        return true;
      }

      accessIterator.next();
    }

    return false;
  }

  private void assignAccesses() {
    final List<Integer> accessIndices = accessIterator.value();

    accesses.clear();
    for (int i = 0; i < size; i++) {
      final Set<MemoryAccess> accessClass = accessClasses.get(accessIndices.get(i));
      accesses.add(Randomizer.get().choose(accessClass));
    }
  }

  private boolean recalculatePossibleDependencies() {
    possibleDependencies.clear();

    for (int i = size - 1; i >= 0; i--) {
      final List<List<MemoryDependency>> dependenciesI = new ArrayList<>();

      for (int j = size - 1; j >= 0; j--) {
        if (i <= j) {
          dependenciesI.add(new ArrayList<MemoryDependency>());
        } else {
          final List<MemoryDependency> dependencies =
              getDependencies(accesses.get(i), accesses.get(j));

          if (dependencies == null) {
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
   * @return the dependencies between the memory accesses or {@code null}.
   */
  private List<MemoryDependency> getDependencies(
      final MemoryAccess access1,
      final MemoryAccess access2) {
    InvariantChecks.checkNotNull(access1);
    InvariantChecks.checkNotNull(access2);

    boolean unsat = true;

    final List<MmuDevice> devices1 = access1.getDevices();
    final List<MmuDevice> devices2 = access2.getDevices();

    // Intersect the sets of devices used in the memory accesses.
    final List<MmuDevice> devices = new ArrayList<>(devices1);
    devices.retainAll(devices2);

    final Set<MmuAddress> addresses = new LinkedHashSet<>();

    for (final MmuDevice device : devices) {
      addresses.add(device.getAddress());
    }

    List<MemoryDependency> addrDependencies = new ArrayList<>();
    for (final MmuAddress address : addresses) {
      final List<MemoryHazard> hazards = CoverageExtractor.get().getCoverage(address);
      addHazardsToDependencies(addrDependencies, access1, access2, hazards);

      if (!hazards.isEmpty()) {
        unsat = false;
      }
    }

    List<MemoryDependency> allDependencies = new ArrayList<>();
    for (final MemoryDependency addrDependency : addrDependencies) {
      final List<MemoryDependency> dependencies = new ArrayList<>();

      dependencies.add(addrDependency);
      for (final MmuDevice device : devices) {
        final List<MemoryHazard> hazards = CoverageExtractor.get().getCoverage(device);
        addHazardsToDependencies(dependencies, access1, access2, hazards);

        if (!hazards.isEmpty()) {
          unsat = false;
        }

        if (dependencies.isEmpty()) {
          break;
        }
      }

      allDependencies.addAll(dependencies);
    }

    return allDependencies.isEmpty() && !unsat ? null : allDependencies;
  }

  /**
   * Extends the dependencies with the hazards.
   * 
   * @param hazards the hazards
   * @param dependencies the list of dependencies
   * @return the list of dependencies.
   */
  private void addHazardsToDependencies(
      final List<MemoryDependency> dependencies,
      final MemoryAccess access1,
      final MemoryAccess access2,
      final List<MemoryHazard> hazards) {
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

    final List<MemoryDependency> tempDependencies = new ArrayList<>(dependencies);

    dependencies.clear();
    for (final MemoryDependency tempDependency : tempDependencies) {
      for (final MemoryHazard hazard : hazards) {
        final MemoryDependency dependency = new MemoryDependency(tempDependency);
        dependency.addHazard(hazard);

        final MemoryAccessStructureChecker checker = new MemoryAccessStructureChecker(
            memory, access1, access2, dependency, accessPairFilter);

        if (checker.check()) {
          dependencies.add(dependency);
        }
      }
    }
  }

  //------------------------------------------------------------------------------------------------
  // Filter Registration
  //------------------------------------------------------------------------------------------------

  public void addExecutionFilter(
      final Predicate<MemoryAccess> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addExecutionFilter(filter);
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

  public void addTemplateFilter(
      final Predicate<MemoryAccessStructure> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addTemplateFilter(filter);
  }
}
