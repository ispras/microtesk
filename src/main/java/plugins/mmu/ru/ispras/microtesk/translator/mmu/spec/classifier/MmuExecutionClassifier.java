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

package ru.ispras.microtesk.translator.mmu.spec.classifier;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.spec.MmuExecution;
import ru.ispras.microtesk.translator.mmu.spec.MmuExecutionClass;

/**
 * This class describes the policy of unification by execution paths.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class MmuExecutionClassifier {

  /**
   * Unifies all possible execution paths.
   * 
   * @param executions the execution list
   * @return the list of execution classes paths.
   * @throws NullPointerException if {@code conflicts} is null.
   */
  public List<MmuExecutionClass> unifyExecutions(List<MmuExecution> executions) {
    InvariantChecks.checkNotNull(executions);

    final List<MmuExecutionClass> executionList = new ArrayList<>();

    for (final MmuExecution execution : executions) {
      final MmuExecutionClass mmuExecutionClass = new MmuExecutionClass();
      mmuExecutionClass.addExecution(execution);
      executionList.add(mmuExecutionClass);
    }

    return executionList;
  }

}
