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
import ru.ispras.microtesk.basis.iterator.Iterator;
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
import ru.ispras.microtesk.mmu.translator.coverage.MemoryDependency;
import ru.ispras.microtesk.mmu.translator.coverage.MemoryAccess;
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

  /** Supported data types, i.e. sizes of data blocks accessed by load/store instructions. */
  private final DataType[] dataTypes;
  /** Data type randomization option. */
  private final boolean randomDataType;

  /** Number of memory accesses in a structure. */
  private final int size;

  /** Array of indices in the data types array. */
  private final int[] dataTypeIndices;
  /** Array of indices in the executions array. */
  private final int[] executionPathIndices;
  /** Array of indices in the dependencies array. */
  private final int[][] dependencyIndices;

  /** Sequence of memory accesses */
  private List<MemoryAccess> accesses = new ArrayList<>();
  /** Matrix of dependencies. */
  private MemoryDependency[][] dependencies;
  /** Nested lists of dependencies. */
  private List<List<List<MemoryDependency>>> dependencyMatrix = new ArrayList<>();

  /** {@code true} if the iteration has more elements; {@code false} otherwise. */
  private boolean hasValue;

  /** Checks the consistency of execution path pairs. */
  private Predicate<MemoryAccessStructure> accessPairFilter;
  /** Checks the consistency of whole test templates. */
  private Predicate<MemoryAccessStructure> structureFilter;

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
 
    this.memory = memory;
    this.dataTypes = dataTypes;
    this.randomDataType = randomDataType;
    this.size = size;

    this.dataTypeIndices = new int[size];
    this.executionPathIndices = new int[size];
    this.dependencyIndices = new int[size][size];

    final List<MemoryAccess> accesses = CoverageExtractor.get().getCoverage(memory);
    this.accessClasses = classifier.classify(accesses);

    init();
  }

  /**
   * Returns the list of dependencies for the given memory accesses.
   * 
   * @param access1 the primary memory access.
   * @param access2 the secondary memory access.
   * @return the dependencies between the memory accesses.
   */
  public List<MemoryDependency> getDependencies(
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

    List<MemoryDependency> addrDeps = new ArrayList<>();
    for (final MmuAddress address : addresses) {
      final List<MemoryHazard> hazards = CoverageExtractor.get().getCoverage(address);

      addrDeps = addConflictToDependency(hazards,  addrDeps, access1, access2);

      if (!hazards.isEmpty()) {
        unsat = false;
      }
    }

    List<MemoryDependency> allDeps = new ArrayList<>();
    for (final MemoryDependency addrDep : addrDeps) {
      List<MemoryDependency> dependency = new ArrayList<>();

      dependency.add(addrDep);
      for (final MmuDevice device : devices) {
        final List<MemoryHazard> hazards = CoverageExtractor.get().getCoverage(device);

        dependency = addConflictToDependency(hazards, dependency, access1, access2);

        if (!hazards.isEmpty()) {
          unsat = false;
        }

        if (dependency.isEmpty()) {
          break;
        }
      }

      allDeps.addAll(dependency);
    }

    return allDeps.isEmpty() && !unsat ? null : allDeps;
  }

  /**
   * Returns the list of dependencies created from this list of dependencies & hazards.
   * 
   * @param hazards the hazards
   * @param dependencies the list of dependencies
   * @return the list of dependencies.
   * @throws IllegalArgumentException if some parameters are null.
   */
  private List<MemoryDependency> addConflictToDependency(
      final List<MemoryHazard> hazards, final List<MemoryDependency> dependencies,
      final MemoryAccess access1, final MemoryAccess access2) {

    InvariantChecks.checkNotNull(hazards);
    InvariantChecks.checkNotNull(dependencies);
    InvariantChecks.checkNotNull(access1);
    InvariantChecks.checkNotNull(access2);

    if (!hazards.isEmpty()) {
      if (dependencies.isEmpty()) {
        // Initialize the list of dependencies.
        for (final MemoryHazard hazard : hazards) {
          final MemoryDependency dependency = new MemoryDependency();

          dependency.addHazard(hazard);

          // Check consistency
          final MemoryAccessStructureChecker checker = new MemoryAccessStructureChecker(memory, access1, access2,
              dependency, accessPairFilter);

          if (checker.check()) {
            dependencies.add(dependency);
          }
        }
      } else {
        // Add the hazards to the dependency list.
        final List<MemoryDependency> dependencyListTemp = new ArrayList<>();
        dependencyListTemp.addAll(dependencies);
        dependencies.clear();

        for (final MemoryDependency dependency : dependencyListTemp) {
          for (final MemoryHazard hazard : hazards) {
            final MemoryDependency newDependency = new MemoryDependency(dependency);

            newDependency.addHazard(hazard);

            final MemoryAccessStructureChecker newChecker = new MemoryAccessStructureChecker(memory, access1, access2,
                newDependency, accessPairFilter);

            if (newChecker.check()) {
              dependencies.add(newDependency);
            }
          }
        }
      }
    }

    return dependencies;
  }

  @Override
  public void init() {
    // Initialize the filter for checking execution pairs.
    this.accessPairFilter = filterBuilder.build();

    // Initialize the filter for checking whole templates.
    final FilterBuilder structureFilterBuilder = new FilterBuilder(filterBuilder);

    structureFilterBuilder.addFilterBuilder(ADVANCED_FILTERS);
    this.structureFilter = structureFilterBuilder.build();

    Arrays.fill(dataTypeIndices, 0);
    Arrays.fill(executionPathIndices, 0);

    hasValue = true;
    assignExecutions();
    assignDependency();

    if (!checkAbstractSequence()) {
      step();
    }
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  @Override
  public MemoryAccessStructure value() {
    setDataTypes();
    return new MemoryAccessStructure(memory, accesses, dependencies);
  }

  // TODO:
  private void setDataTypes() {
    for (int i = 0; i < accesses.size(); i++) {
      final MemoryAccess execution = accesses.get(i);
      execution.setType(dataTypes[dataTypeIndices[i]]);
    }
  }

  @Override
  public void next() {
    // TODO:
    for (int i = dataTypeIndices.length - 1; i >= 0 ; i--) {
      if (!randomDataType) {
        if (dataTypeIndices[i] < dataTypes.length - 1) {
          dataTypeIndices[i]++;
          setDataTypes();
          return;
        }

        dataTypeIndices[i] = 0;
      } else {
        dataTypeIndices[i] = Randomizer.get().nextIntRange(0, dataTypes.length - 1);
      }
    }

    step();
  }

  private void step() {
    if (!nextDependencies()) {
      while (!nextExecution() && hasValue) {
        // If the dependencies are inconsistent, try the next variant.
      }
    } else {
      assignRecentDependencies();
    }

    while (!checkAbstractSequence() && hasValue) {
      if (!nextDependencies()) {
        nextExecution();
      } else {
        assignRecentDependencies();
      }
    }
  }

  /**
   * Iterate the execution.
   * 
   * @return {@code true} increment the executions iterator; {@code false} otherwise.
   */
  private boolean nextExecution() {
    executionPathIndices[0]++;
    if (executionPathIndices[0] == accessClasses.size()) {
      for (int i = 0; i < size - 1; i++) {
        if (executionPathIndices[i] == accessClasses.size()) {
          executionPathIndices[i] = 0;
          executionPathIndices[i + 1]++;
        }
      }
      if (executionPathIndices[size - 1] == accessClasses.size()) {
        hasValue = false;
      }
    }

    if (hasValue) {
      assignExecutions();
      if (!assignDependency()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Iterate the dependencies.
   * 
   * @return {@code true} increment the dependencies iterator; {@code false} otherwise.
   */
  private boolean nextDependencies() {
    if (size == 1) {
      return false;
    }

    if (dependencyIndices[0][0] + 1 >= dependencyMatrix.get(0).get(0).size()) {
      for (int i = 0; i < size; i++) {
        final List<List<MemoryDependency>> dependencyMatrixI = dependencyMatrix.get(i);
        for (int j = 0; j < dependencyMatrixI.size(); j++) {
          dependencyIndices[i][j]++;

          if (dependencyIndices[i][j] >= dependencyMatrixI.get(j).size()) {
            dependencyIndices[i][j] = 0;
          } else {
            return true;
          }
        }
      }

      return false;

    } else {
      dependencyIndices[0][0]++;
      return true;
    }
  }

  /**
   * Assigns the template executions.
   */
  private void assignExecutions() {
    final List<MemoryAccess> templateExecutions = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      final Set<MemoryAccess> executionClass = accessClasses.get(executionPathIndices[i]);
      final MemoryAccess execution = Randomizer.get().choose(executionClass);

      templateExecutions.add(execution);
    }

    this.accesses.clear();
    this.accesses = templateExecutions;
  }

  /**
   * Assigns the recent dependencies.
   */
  private void assignRecentDependencies() {
    dependencies = new MemoryDependency[size][size];
    for (int i = 0; i < size - 1; i++) {
      Arrays.fill(dependencies[i], null);
    }

    if (!dependencyMatrix.isEmpty()) {
      for (int i = 0; i < size; i++) {
        final List<List<MemoryDependency>> dependencyMatrixI = dependencyMatrix.get(i);
        for (int j = 0; j < dependencyMatrixI.size(); j++) {
          final List<MemoryDependency> dependencyMatrixJ = dependencyMatrixI.get(j);
          if (!dependencyMatrixJ.isEmpty()) {
            dependencies[(size - 1) - i][(size - 1) - j] =
                dependencyMatrixJ.get(dependencyIndices[i][j]);
          }
        }
      }
    }
  }

  /**
   * Assigns the template dependencies.
   * 
   * @return {@code false} if the executions dependencies is not solved; {@code true} otherwise.
   */
  private boolean assignDependency() {
    if (size > 1) {
      final List<List<List<MemoryDependency>>> dependencyMatrix = new ArrayList<>();
      for (int i = size - 1; i >= 0; i--) {
        final List<List<MemoryDependency>> dependencyMatrixI = new ArrayList<>();
        for (int j = size - 1; j >= 0; j--) {
          if (i <= j) {
            dependencyMatrixI.add(new ArrayList<MemoryDependency>());
          } else {
            final List<MemoryDependency> dependency =
                getDependencies(accesses.get(i), accesses.get(j));

            if (dependency == null) {
              return false;
            }

            dependencyMatrixI.add(dependency);
          }
        }
        dependencyMatrix.add(dependencyMatrixI);
      }

      this.dependencyMatrix = dependencyMatrix;

      for (int i = 0; i < size - 1; i++) {
        Arrays.fill(dependencyIndices[i], 0);
      }
    }

    assignRecentDependencies();
    return true;
  }

  private boolean checkAbstractSequence() {
    final MemoryAccessStructure abstractSequence =
        new MemoryAccessStructure(memory, accesses, dependencies);
    final MemoryAccessStructureChecker abstractSequenceChecker =
        new MemoryAccessStructureChecker(abstractSequence, structureFilter);

    setDataTypes();
    return abstractSequenceChecker.check();
  }

  //------------------------------------------------------------------------------------------------
  // Filter Registration
  //------------------------------------------------------------------------------------------------

  public void addExecutionFilter(final Predicate<MemoryAccess> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addExecutionFilter(filter);
  }

  public void addHazardFilter(final TriPredicate<MemoryAccess, MemoryAccess, MemoryHazard> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addHazardFilter(filter);
  }

  public void addDependencyFilter(
      final TriPredicate<MemoryAccess, MemoryAccess, MemoryDependency> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addDependencyFilter(filter);
  }

  public void addUnitedHazardFilter(final BiPredicate<MemoryAccess, MemoryUnitedHazard> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addUnitedHazardFilter(filter);
  }

  public void addUnitedDependencyFilter(final BiPredicate<MemoryAccess, MemoryUnitedDependency> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addUnitedDependencyFilter(filter);
  }

  public void addTemplateFilter(final Predicate<MemoryAccessStructure> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addTemplateFilter(filter);
  }
}
