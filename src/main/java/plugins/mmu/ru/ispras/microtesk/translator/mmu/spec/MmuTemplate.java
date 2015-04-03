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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class describes a test template. The description includes a set of MMU execution paths and a
 * set of MMU dependencies.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class MmuTemplate {
  /** The list of executions. */
  private List<MmuExecution> executions = new ArrayList<>();
  /** The dependencies between the executions. */
  private MmuDependency[][] dependencies;

  /**
   * Constructs a test template.
   * 
   * @param executions the executions.
   * @param dependencies the dependencies.
   * @throws NullPointerException if {@code executions} or {@code dependencies} is null.
   */
  public MmuTemplate(final List<MmuExecution> executions, final MmuDependency[][] dependencies) {
    InvariantChecks.checkNotNull(executions);
    InvariantChecks.checkNotNull(dependencies);

    this.executions = executions;
    this.dependencies = dependencies;
  }

  /**
   * Returns the size of the test template.
   * 
   * @return the template size.
   */
  public int size() {
    return executions.size();
  }

  /**
   * Returns the executions of the template.
   * 
   * @return the template executions.
   */
  public List<MmuExecution> getExecutions() {
    return executions;
  }

  /**
   * Returns the execution of the template.
   * 
   * @param i the index of execution.
   * @return the execution.
   */
  public MmuExecution getExecution(final int i) {
    return executions.get(i);
  }

  /**
   * Returns the template dependency.
   * 
   * @param i the index of primary execution.
   * @param j the index of secondary execution.
   * @return the dependency.
   */
  public MmuDependency getDependency(final int i, final int j) {
    return dependencies[j][i];
  }

  /**
   * Returns the dependencies between the template executions.
   * 
   * @return the template dependencies.
   */
  public MmuDependency[][] getDependencies() {
    return dependencies;
  }

  /**
   * Returns the united dependency for the {@code j}-th execution.
   * 
   * @param j the execution index.
   * @return the united dependency.
   */
  public MmuUnitedDependency getUnitedDependency(final int j) {
    final Map<MmuDependency, Integer> dependencies = new HashMap<>();

    for (int i = 0; i < j; i++) {
      final MmuDependency dependency = getDependency(i, j);

      if (dependency != null) {
        dependencies.put(dependency, i);
      }
    }

    return new MmuUnitedDependency(j, dependencies);
  }

  @Override
  public String toString() {
    final String newLine = System.getProperty("line.separator");

    final StringBuilder string = new StringBuilder("Mmu template:");
    string.append(newLine);
    string.append("Executions:");
    string.append(newLine);
    string.append(executions.toString());

    string.append(newLine);
    string.append("Dependencies:");
    string.append(newLine);

    for (int i = 0; i < dependencies.length; i++) {
      for (int j = 0; j < dependencies.length; j++) {
        string.append("[").append(i).append("], ");
        string.append("[").append(j).append("] = ");
        if (dependencies[j][i] == null) {
          string.append("[null]").append(newLine);
        } else {
          string.append(dependencies[j][i].toString()).append(newLine);
        }
      }
    }

    return string.toString();
  }
}
