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
import ru.ispras.microtesk.mmu.translator.coverage.Dependency;
import ru.ispras.microtesk.mmu.translator.coverage.ExecutionPath;
import ru.ispras.microtesk.mmu.translator.coverage.Hazard;
import ru.ispras.microtesk.mmu.translator.coverage.UnitedDependency;
import ru.ispras.microtesk.mmu.translator.coverage.UnitedHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSpecification;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.DataType;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;
import ru.ispras.microtesk.utils.function.BiPredicate;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.microtesk.utils.function.TriPredicate;

/**
 * {@link AbstractSequenceIterator} implements an iterator of abstract sequences (templates) for
 * memory access instructions (loads and stores).
 * 
 * <p>A template is a sequence of execution paths (situations) linked together with a number of
 * dependencies. The iterator systematically enumerates templates to cover a representative set of
 * cases of the memory subsystem behavior.</p>
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class AbstractSequenceIterator implements Iterator<AbstractSequence> {
  /** Checks the consistency of execution path pairs (template parts) and templates. */
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

  /** Checks the consistency of templates (such filters cannot be applied to template parts). */
  private static final FilterBuilder ADVANCED_FILTERS = new FilterBuilder();
  static {
    ADVANCED_FILTERS.addUnitedDependencyFilter(new FilterAccessThenMiss());
  }

  /** Contains the basic filters as well as user-defined ones. */
  private final FilterBuilder filterBuilder = new FilterBuilder(BASIC_FILTERS);

  /** Memory subsystem specification. */
  private final MmuSpecification memory;
  /** Supported data types, i.e. sizes of data blocks accessed by load/store instructions. */
  private final DataType[] dataTypes;
  /** Data type randomization option. */
  private final boolean randomDataType;

  /** Number of execution paths in a template. */
  private final int numberOfExecutions;
  /** Execution path classifier. */
  private final ExecutionPathClassifier executionPathClassifier;

  /** Array of indices in the data types array. */
  private final int[] dataTypeIndices;
  /** Array of indices in the executions array. */
  private final int[] executionPathIndices;
  /** Array of indices in the dependencies array. */
  private final int[][] dependencyIndices;

  /** The executions paths. */
  private List<ExecutionPathClass> executions = new ArrayList<>();
  /** The current test template. */
  private AbstractSequence template;

  /** The list of executions. */
  private List<ExecutionPath> templateExecutions = new ArrayList<>();
  /** The array of dependencies. */
  private Dependency[][] templateDependencies;
  /** The dependencies matrix. */
  private List<List<List<Dependency>>> dependencyMatrix = new ArrayList<>();

  /** {@code true} if the iteration has more elements; {@code false} otherwise. */
  private boolean hasNext;

  /** Checks the consistency of execution path pairs. */
  private Predicate<AbstractSequence> executionPairFilter;
  /** Checks the consistency of whole test templates. */
  private Predicate<AbstractSequence> wholeTemplateFilter;

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
  public AbstractSequenceIterator(
      final MmuSpecification memory,
      final DataType[] dataTypes,
      final boolean randomDataType,
      final int numberOfExecutions,
      final ExecutionPathClassifier executionPathClassifier) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(dataTypes);
    InvariantChecks.checkNotNull(executionPathClassifier);
 
    this.memory = memory;
    this.dataTypes = dataTypes;
    this.randomDataType = randomDataType;
    this.numberOfExecutions = numberOfExecutions;
    this.executionPathClassifier = executionPathClassifier;

    this.dataTypeIndices = new int[numberOfExecutions];
    this.executionPathIndices = new int[numberOfExecutions];
    this.dependencyIndices = new int[numberOfExecutions][numberOfExecutions];

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
  public List<Dependency> getDependencies(
      final ExecutionPath execution1,
      final ExecutionPath execution2) {
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

    List<Dependency> dependencyList = new ArrayList<>();
    List<Dependency> dependencyAddressList = new ArrayList<>();

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

    for (final Dependency dependencyAddress : dependencyAddressList) {
      List<Dependency> dependency = new ArrayList<>();

      dependency.add(dependencyAddress);
      for (final MmuDevice device : devices) {
        final List<Hazard> hazards = CoverageExtractor.get().getCoverage(device);

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
  private List<Dependency> addConflictToDependency(
      final List<Hazard> hazards, final List<Dependency> dependencies,
      final ExecutionPath execution1, final ExecutionPath execution2) {

    InvariantChecks.checkNotNull(hazards);
    InvariantChecks.checkNotNull(dependencies);
    InvariantChecks.checkNotNull(execution1);
    InvariantChecks.checkNotNull(execution2);

    if (!hazards.isEmpty()) {
      if (dependencies.isEmpty()) {
        // Initialize the list of dependencies.
        for (final Hazard hazard : hazards) {
          final Dependency dependency = new Dependency();

          dependency.addHazard(hazard);

          // Check consistency
          final AbstractSequenceChecker checker = new AbstractSequenceChecker(memory, execution1, execution2,
              dependency, executionPairFilter);

          if (checker.check()) {
            dependencies.add(dependency);
          }
        }
      } else {
        // Add the hazards to the dependency list.
        final List<Dependency> dependencyListTemp = new ArrayList<>();
        dependencyListTemp.addAll(dependencies);
        dependencies.clear();

        for (final Dependency dependency : dependencyListTemp) {
          for (final Hazard hazard : hazards) {
            final Dependency newDependency = new Dependency(dependency);

            newDependency.addHazard(hazard);

            final AbstractSequenceChecker newChecker = new AbstractSequenceChecker(memory, execution1, execution2,
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

    executions.clear();

    final List<ExecutionPath> executionPaths = CoverageExtractor.get().getCoverage(memory);
    final List<ExecutionPathClass> executionPathClasses =
        executionPathClassifier.unifyExecutions(executionPaths);

    for (final ExecutionPathClass unifyExecution : executionPathClasses) {
      this.executions.add(unifyExecution);
    }

    Arrays.fill(dataTypeIndices, 0);
    Arrays.fill(executionPathIndices, 0);

    hasNext = true;
    assignExecutions();
    assignDependency();

    // TODO:
    setDataTypes();
    this.template = new AbstractSequence(memory, templateExecutions, templateDependencies);

    // Check template.
    final AbstractSequenceChecker checker = new AbstractSequenceChecker(template, wholeTemplateFilter);

    if (!checker.check()) {
      step();
    }
  }

  @Override
  public boolean hasValue() {
    return hasNext;
  }

  @Override
  public AbstractSequence value() {
    return template;
  }

  // TODO:
  private void setDataTypes() {
    for (int i = 0; i < templateExecutions.size(); i++) {
      final ExecutionPath execution = templateExecutions.get(i);
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
          this.template = new AbstractSequence(memory, templateExecutions, templateDependencies);

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
      while (!nextExecution() && hasNext) {
        // If the dependencies are inconsistent, try the next variant.
      }
    } else {
      assignRecentDependencies();
    }

    // TODO:
    setDataTypes();
    this.template = new AbstractSequence(memory, templateExecutions, templateDependencies);

    AbstractSequenceChecker checker = new AbstractSequenceChecker(template, wholeTemplateFilter);

    while (!checker.check() && hasNext) {
      if (!nextDependencies()) {
        nextExecution();
      } else {
        assignRecentDependencies();
      }

      // TODO:
      setDataTypes();
      this.template = new AbstractSequence(memory, templateExecutions, templateDependencies);
      checker = new AbstractSequenceChecker(template, wholeTemplateFilter);
    }
  }

  /**
   * Iterate the execution.
   * 
   * @return {@code true} increment the executions iterator; {@code false} otherwise.
   */
  private boolean nextExecution() {
    final int size = executions.size();

    executionPathIndices[0]++;
    if (executionPathIndices[0] == size) {
      for (int i = 0; i < numberOfExecutions - 1; i++) {
        if (executionPathIndices[i] == size) {
          executionPathIndices[i] = 0;
          executionPathIndices[i + 1]++;
        }
      }
      if (executionPathIndices[numberOfExecutions - 1] == size) {
        hasNext = false;
      }
    }

    if (hasNext) {
      assignExecutions();
      if (!assignDependency()) {
        return false;
      }
      this.template = new AbstractSequence(memory, templateExecutions, templateDependencies);
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
        final List<List<Dependency>> dependencyMatrixI = dependencyMatrix.get(i);
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
    final List<ExecutionPath> templateExecutions = new ArrayList<>(numberOfExecutions);
    for (int i = 0; i < numberOfExecutions; i++) {
      final ExecutionPath execution = this.executions.get(executionPathIndices[i]).getExecution();
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
    templateDependencies = new Dependency[numberOfExecutions][numberOfExecutions];
    for (int i = 0; i < size; i++) {
      Arrays.fill(templateDependencies[i], null);
    }

    if (!dependencyMatrix.isEmpty()) {
      for (int i = 0; i < numberOfExecutions; i++) {
        final List<List<Dependency>> dependencyMatrixI = dependencyMatrix.get(i);
        for (int j = 0; j < dependencyMatrixI.size(); j++) {
          final List<Dependency> dependencyMatrixJ = dependencyMatrixI.get(j);
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
      final List<List<List<Dependency>>> dependencyMatrix = new ArrayList<>();
      for (int i = numberOfExecutions - 1; i >= 0; i--) {
        final List<List<Dependency>> dependencyMatrixI = new ArrayList<>();
        for (int j = numberOfExecutions - 1; j >= 0; j--) {
          if (i <= j) {
            dependencyMatrixI.add(new ArrayList<Dependency>());
          } else {
            final List<Dependency> dependency =
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

  // -----------------------------------------------------------------------------------------------
  // Filter Registration
  // -----------------------------------------------------------------------------------------------

  public void addExecutionFilter(final Predicate<ExecutionPath> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addExecutionFilter(filter);
  }

  public void addHazardFilter(final TriPredicate<ExecutionPath, ExecutionPath, Hazard> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addHazardFilter(filter);
  }

  public void addDependencyFilter(
      final TriPredicate<ExecutionPath, ExecutionPath, Dependency> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addDependencyFilter(filter);
  }

  public void addUnitedHazardFilter(final BiPredicate<ExecutionPath, UnitedHazard> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addUnitedHazardFilter(filter);
  }

  public void addUnitedDependencyFilter(final BiPredicate<ExecutionPath, UnitedDependency> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addUnitedDependencyFilter(filter);
  }

  public void addTemplateFilter(final Predicate<AbstractSequence> filter) {
    InvariantChecks.checkNotNull(filter);
    filterBuilder.addTemplateFilter(filter);
  }
}
