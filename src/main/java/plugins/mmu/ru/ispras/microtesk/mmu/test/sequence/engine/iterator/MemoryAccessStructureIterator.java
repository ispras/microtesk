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
 * {@link MemoryAccessStructureIterator} implements an iterator of abstract sequences (templates) for
 * memory access instructions (loads and stores).
 * 
 * <p>A template is a sequence of execution paths (situations) linked together with a number of
 * dependencies. The iterator systematically enumerates templates to cover a representative set of
 * cases of the memory subsystem behavior.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MemoryAccessStructureIterator implements Iterator<MemoryAccessStructure> {
  /** Checks the consistency of execution path pairs (template parts) and whole templates. */
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

  /** Executions path equivalence classes. */
  private final List<Set<MemoryAccess>> allExecutionClasses = new ArrayList<>();

  /** Supported data types, i.e. sizes of data blocks accessed by load/store instructions. */
  private final DataType[] dataTypes;
  /** Data type randomization option. */
  private final boolean randomDataType;

  /** Number of execution paths in a template. */
  private final int numberOfExecutions;

  /** Array of indices in the data types array. */
  private final int[] dataTypeIndices;
  /** Array of indices in the executions array. */
  private final int[] executionPathIndices;
  /** Array of indices in the dependencies array. */
  private final int[][] dependencyIndices;

  /** The list of executions. */
  private List<MemoryAccess> templateExecutions = new ArrayList<>();
  /** The array of dependencies. */
  private MemoryDependency[][] templateDependencies;
  /** The dependencies matrix. */
  private List<List<List<MemoryDependency>>> dependencyMatrix = new ArrayList<>();

  /** {@code true} if the iteration has more elements; {@code false} otherwise. */
  private boolean hasValue;

  /** Checks the consistency of execution path pairs. */
  private Predicate<MemoryAccessStructure> executionPairFilter;
  /** Checks the consistency of whole test templates. */
  private Predicate<MemoryAccessStructure> wholeTemplateFilter;

  /**
   * Constructs a MMU iterator.
   * 
   * @param memory the memory specification.
   * @param numberOfExecutions the number of execution paths in a template.
   * @param dataTypes the array of supported data types.
   * @param randomDataType the data type randomization option.
   * @param executionPathClassifier the policy of unification executions.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public MemoryAccessStructureIterator(
      final MmuSubsystem memory,
      final DataType[] dataTypes,
      final boolean randomDataType,
      final int numberOfExecutions,
      final Classifier<MemoryAccess> classifier) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(dataTypes);
    InvariantChecks.checkNotNull(classifier);
 
    this.memory = memory;
    this.dataTypes = dataTypes;
    this.randomDataType = randomDataType;
    this.numberOfExecutions = numberOfExecutions;

    this.dataTypeIndices = new int[numberOfExecutions];
    this.executionPathIndices = new int[numberOfExecutions];
    this.dependencyIndices = new int[numberOfExecutions][numberOfExecutions];

    final List<MemoryAccess> executionPaths = CoverageExtractor.get().getCoverage(memory);
    final List<Set<MemoryAccess>> executionClasses = classifier.classify(executionPaths);

    for (final Set<MemoryAccess> executionClass : executionClasses) {
      this.allExecutionClasses.add(executionClass);
    }

    init();
  }

  /**
   * Returns the list of execution dependencies.
   * 
   * @param execution1 the execution
   * @param execution2 the execution
   * @return the list of dependencies.
   * @throws IllegalArgumentException if {@code execution1} or {@code execution2} is null.
   */
  public List<MemoryDependency> getDependencies(
      final MemoryAccess execution1,
      final MemoryAccess execution2) {
    InvariantChecks.checkNotNull(execution1);
    InvariantChecks.checkNotNull(execution2);

    boolean notSolved = true;

    final List<MmuDevice> devices1 = execution1.getDevices();
    final List<MmuDevice> devices2 = execution2.getDevices();
    final List<MmuDevice> devices0 = new ArrayList<>();

    // Intersect the lists.
    devices0.addAll(devices1);
    devices0.retainAll(devices2);

    final List<MmuDevice> devices = new ArrayList<>();
    // TODO fix it
    for (final MmuDevice device : devices0) {
    //  if(device.isParent() != true) {
        devices.add(device);
    //  }
    }

    List<MemoryDependency> dependencyList = new ArrayList<>();
    List<MemoryDependency> dependencyAddressList = new ArrayList<>();

    final Set<MmuAddress> addresses = new LinkedHashSet<>();

    for (final MmuDevice device : devices) {
      addresses.add(device.getAddress());
    }

    for (MmuAddress address : addresses) {
      dependencyAddressList =
          addConflictToDependency(CoverageExtractor.get().getCoverage(address),
              dependencyAddressList, execution1, execution2);

      if (!CoverageExtractor.get().getCoverage(address).isEmpty()) {
        notSolved = false;
      }
    }

    for (final MemoryDependency dependencyAddress : dependencyAddressList) {
      List<MemoryDependency> dependency = new ArrayList<>();

      dependency.add(dependencyAddress);
      for (final MmuDevice device : devices) {
        final List<MemoryHazard> hazards = CoverageExtractor.get().getCoverage(device);

        if (!hazards.isEmpty()) {
          notSolved = false;
        }

        dependency = addConflictToDependency(hazards, dependency, execution1, execution2);

        if (dependency.isEmpty()) {
          break;
        }
      }

      dependencyList.addAll(dependency);
    }

    return dependencyList.isEmpty() && !notSolved ? null : dependencyList;
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
      final MemoryAccess execution1, final MemoryAccess execution2) {

    InvariantChecks.checkNotNull(hazards);
    InvariantChecks.checkNotNull(dependencies);
    InvariantChecks.checkNotNull(execution1);
    InvariantChecks.checkNotNull(execution2);

    if (!hazards.isEmpty()) {
      if (dependencies.isEmpty()) {
        // Initialize the list of dependencies.
        for (final MemoryHazard hazard : hazards) {
          final MemoryDependency dependency = new MemoryDependency();

          dependency.addHazard(hazard);

          // Check consistency
          final MemoryAccessStructureChecker checker = new MemoryAccessStructureChecker(memory, execution1, execution2,
              dependency, executionPairFilter);

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

            final MemoryAccessStructureChecker newChecker = new MemoryAccessStructureChecker(memory, execution1, execution2,
                newDependency, executionPairFilter);

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
    this.executionPairFilter = filterBuilder.build();

    // Initialize the filter for checking whole templates.
    final FilterBuilder wholeTemplateFilterBuilder = new FilterBuilder(filterBuilder);

    wholeTemplateFilterBuilder.addFilterBuilder(ADVANCED_FILTERS);
    this.wholeTemplateFilter = wholeTemplateFilterBuilder.build();

    Arrays.fill(dataTypeIndices, 0);
    Arrays.fill(executionPathIndices, 0);

    hasValue = true;
    assignExecutions();
    assignDependency();

    // TODO:
    setDataTypes();

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
    return new MemoryAccessStructure(memory, templateExecutions, templateDependencies);
  }

  // TODO:
  private void setDataTypes() {
    for (int i = 0; i < templateExecutions.size(); i++) {
      final MemoryAccess execution = templateExecutions.get(i);
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

    // TODO:
    setDataTypes();

    while (!checkAbstractSequence() && hasValue) {
      if (!nextDependencies()) {
        nextExecution();
      } else {
        assignRecentDependencies();
      }

      // TODO:
      setDataTypes();
    }
  }

  /**
   * Iterate the execution.
   * 
   * @return {@code true} increment the executions iterator; {@code false} otherwise.
   */
  private boolean nextExecution() {
    final int size = allExecutionClasses.size();

    executionPathIndices[0]++;
    if (executionPathIndices[0] == size) {
      for (int i = 0; i < numberOfExecutions - 1; i++) {
        if (executionPathIndices[i] == size) {
          executionPathIndices[i] = 0;
          executionPathIndices[i + 1]++;
        }
      }
      if (executionPathIndices[numberOfExecutions - 1] == size) {
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
    if (numberOfExecutions == 1) {
      return false;
    }

    if (dependencyIndices[0][0] + 1 >= dependencyMatrix.get(0).get(0).size()) {
      for (int i = 0; i < numberOfExecutions; i++) {
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
    final List<MemoryAccess> templateExecutions = new ArrayList<>(numberOfExecutions);
    for (int i = 0; i < numberOfExecutions; i++) {
      final Set<MemoryAccess> executionClass = allExecutionClasses.get(executionPathIndices[i]);
      final MemoryAccess execution = Randomizer.get().choose(executionClass);

      templateExecutions.add(execution);
    }

    this.templateExecutions.clear();
    this.templateExecutions = templateExecutions;
  }

  /**
   * Assigns the recent dependencies.
   */
  private void assignRecentDependencies() {
    final int size = numberOfExecutions - 1;
    templateDependencies = new MemoryDependency[numberOfExecutions][numberOfExecutions];
    for (int i = 0; i < size; i++) {
      Arrays.fill(templateDependencies[i], null);
    }

    if (!dependencyMatrix.isEmpty()) {
      for (int i = 0; i < numberOfExecutions; i++) {
        final List<List<MemoryDependency>> dependencyMatrixI = dependencyMatrix.get(i);
        for (int j = 0; j < dependencyMatrixI.size(); j++) {
          final List<MemoryDependency> dependencyMatrixJ = dependencyMatrixI.get(j);
          if (!dependencyMatrixJ.isEmpty()) {
            templateDependencies[size - i][size - j] =
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
    if (numberOfExecutions > 1) {
      final List<List<List<MemoryDependency>>> dependencyMatrix = new ArrayList<>();
      for (int i = numberOfExecutions - 1; i >= 0; i--) {
        final List<List<MemoryDependency>> dependencyMatrixI = new ArrayList<>();
        for (int j = numberOfExecutions - 1; j >= 0; j--) {
          if (i <= j) {
            dependencyMatrixI.add(new ArrayList<MemoryDependency>());
          } else {
            final List<MemoryDependency> dependency =
                getDependencies(templateExecutions.get(i), templateExecutions.get(j));

            if (dependency == null) {
              return false;
            }

            dependencyMatrixI.add(dependency);
          }
        }
        dependencyMatrix.add(dependencyMatrixI);
      }

      this.dependencyMatrix = dependencyMatrix;

      final int size = numberOfExecutions - 1;
      for (int i = 0; i < size; i++) {
        Arrays.fill(dependencyIndices[i], 0);
      }
    }

    assignRecentDependencies();
    return true;
  }

  private boolean checkAbstractSequence() {
    final MemoryAccessStructure abstractSequence =
        new MemoryAccessStructure(memory, templateExecutions, templateDependencies);
    final MemoryAccessStructureChecker abstractSequenceChecker =
        new MemoryAccessStructureChecker(abstractSequence, wholeTemplateFilter);

    return abstractSequenceChecker.check();
  }

  // -----------------------------------------------------------------------------------------------
  // Filter Registration
  // -----------------------------------------------------------------------------------------------

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
