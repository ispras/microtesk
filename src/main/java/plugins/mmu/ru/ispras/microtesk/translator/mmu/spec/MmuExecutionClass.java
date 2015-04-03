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
import java.util.List;


import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class contains a list of unified executions.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class MmuExecutionClass {
  /** The list of executions. */
  private List<MmuExecution> executions = new ArrayList<>();

  /**
   * Adds the execution to the execution class.
   * 
   * @param execution the execution to be added.
   * @throws NullPointerException if {@code execution} is null.
   */
  public void addExecution(final MmuExecution execution) {
    InvariantChecks.checkNotNull(execution);

    executions.add(execution);
  }

  /**
   * Returns all executions of the execution class.
   * 
   * @return the executions.
   */
  public List<MmuExecution> getExecutions() {
    return executions;
  }

  /**
   * Returns an execution randomly chosen from the list.
   * 
   * @return a randomly chosen execution or {@code null} if the execution list is empty.
   */
  public MmuExecution getExecution() {
    if (executions.isEmpty()) {
      return null;
    }

    final int index = Randomizer.get().nextIntRange(0, executions.size() - 1);
    return executions.get(index);
  }
}
