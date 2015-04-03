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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.spec.basis.DataType;
import ru.ispras.microtesk.translator.mmu.spec.basis.Iterator;
import ru.ispras.microtesk.translator.mmu.spec.classifier.MmuExecutionClassifier;

/**
 * This class implements a test situation iterator.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class MmuIterator implements Iterator<MmuTemplate> {
  // TODO:
  private static final DataType[] TYPES = new DataType[] { DataType.BYTE, DataType.DWORD };
  private int[] indices;

  /** The number of execution in template. */
  private int n;
  /** The Memory Management Unit (MMU). */
  private MmuSpecification mmu;
  /** The execution classifier. */
  private MmuExecutionClassifier classifier;

  /** The situation indexes. */
  private int[] indexes;
  /** The dependency indexes. */
  private int[][] dependencyIndexes;

  /** The MMU executions. */
  private List<MmuExecution> executions = new ArrayList<>();
  /** The template. */
  private MmuTemplate template;

  /** The list of executions. */
  private List<MmuExecution> templateExecutions = new ArrayList<>();
  /** The array of dependencies. */
  private MmuDependency[][] templateDependencies;
  /** The dependencies matrix. */
  private List<List<List<MmuDependency>>> dependencyMatrix = new ArrayList<>();

  /** {@code true} if the iteration has more elements; {@code false} otherwise. */
  private boolean hasNext;

  /**
   * Returns the list of execution dependencies.
   * 
   * @param execution1 the execution
   * @param execution2 the execution
   * @return the list of dependencies.
   * @throws NullPointerException if {@code execution1} or {@code execution2} is null.
   */
  public static List<MmuDependency> getDependencies(final MmuExecution execution1,
      final MmuExecution execution2) {
    InvariantChecks.checkNotNull(execution1);
    InvariantChecks.checkNotNull(execution2);

    boolean notSolved = true;

    final List<MmuDevice> devices1 = execution1.getDevices();
    final List<MmuDevice> devices2 = execution2.getDevices();
    final List<MmuDevice> devices = new ArrayList<>();

    // Intersect the lists.
    devices.addAll(devices1);
    devices.retainAll(devices2);

    List<MmuDependency> dependencyList = new ArrayList<>();
    List<MmuDependency> dependencyAddressList = new ArrayList<>();

    final Set<MmuAddress> addresses = new HashSet<>();

    for (final MmuDevice device : devices) {
      addresses.add(device.getAddress());
    }

    for (MmuAddress address : addresses) {
      dependencyAddressList =
          addConflictToDependency(address.getConflicts(), dependencyAddressList,
              execution1, execution2);

      if (!address.getConflicts().isEmpty()) {
        notSolved = false;
      }
    }

    for (final MmuDependency dependencyAddress : dependencyAddressList) {
      List<MmuDependency> dependency = new ArrayList<>();

      dependency.add(dependencyAddress);
      for (final MmuDevice device : devices) {
        final List<MmuConflict> conflicts = device.getConflicts();
        if (!conflicts.isEmpty()) {
          notSolved = false;
        }

        dependency = addConflictToDependency(conflicts, dependency, execution1, execution2);

        if (dependency.isEmpty()) {
          break;
        }
      }

      dependencyList.addAll(dependency);
    }

    return dependencyList.isEmpty() && !notSolved ? null : dependencyList;
  }

  /**
   * Returns the list of dependencies created from this list of dependencies & conflicts.
   * 
   * @param conflicts the conflicts
   * @param dependencies the list of dependencies
   * @return the list of dependencies.
   * @throws NullPointerException if some parameters are null.
   */
  private static List<MmuDependency> addConflictToDependency(final List<MmuConflict> conflicts,
      final List<MmuDependency> dependencies, final MmuExecution execution1,
      final MmuExecution execution2) {

    InvariantChecks.checkNotNull(conflicts);
    InvariantChecks.checkNotNull(dependencies);
    InvariantChecks.checkNotNull(execution1);
    InvariantChecks.checkNotNull(execution2);

    if (!conflicts.isEmpty()) {
      if (dependencies.isEmpty()) {
        // Initialize the list of dependencies.
        for (final MmuConflict conflict : conflicts) {
          final MmuDependency dependency = new MmuDependency();

          dependency.addConflict(conflict);

          // Check consistency
          if (MmuTemplateChecker.checkConsistency(0, 1, execution1, execution2, dependency)) {
            dependencies.add(dependency);
          }
        }
      } else {
        // Add the conflicts to the dependency list.
        final List<MmuDependency> dependencyListTemp = new ArrayList<>();
        dependencyListTemp.addAll(dependencies);
        dependencies.clear();

        for (final MmuDependency dependency : dependencyListTemp) {
          for (final MmuConflict conflict : conflicts) {
            final MmuDependency newDependency = new MmuDependency(dependency);

            newDependency.addConflict(conflict);

            if (MmuTemplateChecker.checkConsistency(0, 1, execution1, execution2, newDependency)) {
              dependencies.add(newDependency);
            }
          }
        }
      }
    }

    return dependencies;
  }

  /**
   * Constructs a MMU iterator.
   * 
   * @param mmu the mmu.
   * @param n the number of execution in template.
   * @param classifier the policy of unification executions.
   * @throws NullPointerException if {@code mmu} or {@code classifier} is null.
   */
  public MmuIterator(final MmuSpecification mmu, final int n, final MmuExecutionClassifier classifier) {
    InvariantChecks.checkNotNull(mmu);
    InvariantChecks.checkNotNull(classifier);

    this.classifier = classifier;
    this.mmu = mmu;
    this.n = n;
    init();
  }

  @Override
  public void init() {
    executions.clear();
    final List<MmuExecution> baseExecutions = mmu.getExecutions();
    final List<MmuExecutionClass> unifyExecutions = classifier.unifyExecutions(baseExecutions);

    for (final MmuExecutionClass unifyExecution : unifyExecutions) {
      this.executions.add(unifyExecution.getExecution());
    }

    // TODO:
    indices = new int[n];

    indexes = new int[n];
    Arrays.fill(indexes, 0);

    hasNext = true;
    assignExecutions();
    assignDependency();

    // TODO:
    setDataTypes();
    this.template = new MmuTemplate(templateExecutions, templateDependencies);

    // Check template.
    if (!MmuTemplateChecker.checkConsistency(template)) {
      step();
    }
  }

  @Override
  public boolean hasValue() {
    return hasNext;
  }

  @Override
  public MmuTemplate value() {
    return template;
  }

  /**
   * Returns the execution indexes.
   * 
   * @return execution indexes.
   */
  public int[] getExecutionIndexes() {
    return indexes;
  }

  /**
   * Returns the dependency indexes.
   * 
   * @return dependency indexes.
   */
  public int[][] getDependencyIndexes() {
    return dependencyIndexes;
  }

  // TODO:
  private void setDataTypes() {
    for (int i = 0; i < templateExecutions.size(); i++) {
      final MmuExecution execution = templateExecutions.get(i);
      execution.setType(TYPES[indices[i]]);
    }
  }

  @Override
  public void next() {
    // TODO:
    for (int i = indices.length - 1; i >= 0 ; i--) {
      if (indices[i] < TYPES.length - 1) {
        indices[i]++;

        setDataTypes();
        this.template = new MmuTemplate(templateExecutions, templateDependencies);

        return;
      }

      indices[i] = 0;
    }

    step();
  }

  private void step() {
    if (!nextDependencies()) {
      while (!nextExecution() && hasNext) {
        // if dependencies not solved, then increment the iterator.
      }
    } else {
      assignRecentDependencies();
    }

    // TODO:
    setDataTypes();
    this.template = new MmuTemplate(templateExecutions, templateDependencies);

    while (!MmuTemplateChecker.checkConsistency(template) && hasNext) {
      if (!nextDependencies()) {
        nextExecution();
      } else {
        assignRecentDependencies();
      }

      // TODO:
      setDataTypes();
      this.template = new MmuTemplate(templateExecutions, templateDependencies);
    }
  }

  /**
   * Iterate the execution.
   * 
   * @return {@code true} increment the executions iterator; {@code false} otherwise.
   */
  private boolean nextExecution() {
    final int size = executions.size();

    indexes[0]++;
    if (indexes[0] == size) {
      for (int i = 0; i < n - 1; i++) {
        if (indexes[i] == size) {
          indexes[i] = 0;
          indexes[i + 1]++;
        }
      }
      if (indexes[n - 1] == size) {
        hasNext = false;
      }
    }

    if (hasNext) {
      assignExecutions();
      if (!assignDependency()) {
        return false;
      }
      this.template = new MmuTemplate(templateExecutions, templateDependencies);
    }
    return true;
  }

  /**
   * Iterate the dependencies.
   * 
   * @return {@code true} increment the dependencies iterator; {@code false} otherwise.
   */
  private boolean nextDependencies() {
    if (n == 1) {
      return false;
    }

    if (dependencyIndexes[0][0] + 1 >= dependencyMatrix.get(0).get(0).size()) {
      for (int i = 0; i < n; i++) {
        final List<List<MmuDependency>> dependencyMatrixI = dependencyMatrix.get(i);
        for (int j = 0; j < dependencyMatrixI.size(); j++) {
          dependencyIndexes[i][j]++;

          if (dependencyIndexes[i][j] >= dependencyMatrixI.get(j).size()) {
            dependencyIndexes[i][j] = 0;
          } else {
            return true;
          }
        }
      }

      return false;

    } else {
      dependencyIndexes[0][0]++;
      return true;
    }
  }

  /**
   * Assigns the template executions.
   */
  private void assignExecutions() {
    final List<MmuExecution> templateExecutions = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      final MmuExecution execution = this.executions.get(indexes[i]);
      templateExecutions.add(execution);
    }

    this.templateExecutions.clear();
    this.templateExecutions = templateExecutions;
  }

  /**
   * Assigns the recent dependencies.
   */
  private void assignRecentDependencies() {
    final int size = n - 1;
    templateDependencies = new MmuDependency[n][n];
    for (int i = 0; i < size; i++) {
      Arrays.fill(templateDependencies[i], null);
    }

    if (!dependencyMatrix.isEmpty()) {
      for (int i = 0; i < n; i++) {
        final List<List<MmuDependency>> dependencyMatrixI = dependencyMatrix.get(i);
        for (int j = 0; j < dependencyMatrixI.size(); j++) {
          final List<MmuDependency> dependencyMatrixJ = dependencyMatrixI.get(j);
          if (!dependencyMatrixJ.isEmpty()) {
            templateDependencies[size - i][size - j] =
                dependencyMatrixJ.get(dependencyIndexes[i][j]);
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
    if (n > 1) {
      final List<List<List<MmuDependency>>> dependencyMatrix = new ArrayList<>();
      for (int i = n - 1; i >= 0; i--) {
        final List<List<MmuDependency>> dependencyMatrixI = new ArrayList<>();
        for (int j = n - 1; j >= 0; j--) {
          if (i <= j) {
            dependencyMatrixI.add(new ArrayList<MmuDependency>());
          } else {
            final List<MmuDependency> dependency =
                getDependencies(templateExecutions.get(i), templateExecutions.get(j));

            if (dependency == null) {
              // All dependencies of this executions is not consistence.
              return false;
            }

            dependencyMatrixI.add(dependency);
          }
        }
        dependencyMatrix.add(dependencyMatrixI);
      }

      this.dependencyMatrix = dependencyMatrix;

      final int size = n - 1;
      dependencyIndexes = new int[n][n];
      for (int i = 0; i < size; i++) {
        Arrays.fill(dependencyIndexes[i], 0);
      }
    }

    assignRecentDependencies();
    return true;
  }
}
