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

package ru.ispras.microtesk.mmu.translator.coverage;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.MemoryOperation;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
final class MemoryCoverageExtractor {
  private final MmuSubsystem memory;

  public MemoryCoverageExtractor(final MmuSubsystem memory) {
    InvariantChecks.checkNotNull(memory);
    this.memory = memory;
  }

  /**
   * Returns all possible execution paths.
   * 
   * @return the list of execution paths.
   */
  public List<MemoryAccess> getExecutionPaths() {
    final List<MemoryAccess> executions = new ArrayList<>();
    final List<MmuTransition> transitions = memory.getTransitions(memory.getStartAction());

    if (transitions != null && !transitions.isEmpty()) {
      for (final MmuTransition transition : transitions) {
        if (transition.isEnabled()) {
          final MemoryAccess execution =
              new MemoryAccess(transition.getGuard().getOperation(), memory.getStartAddress());

          execution.addTransition(transition);
          executions.add(execution);
        }
      }

      int i = 0;
      while (i < executions.size()) {
        final List<MemoryAccess> executionPrefixList = elongateExecutionPaths(executions.get(i));

        if (executionPrefixList != null) {
          executions.remove(i);
          executions.addAll(executionPrefixList);
        } else {
          i++;
        }
      }
    }

    return executions;
  }

  /**
   * Elongates the execution path.
   * 
   * @param execution the execution path to be elongated.
   * @return the list of possible elongations of the execution path.
   */
  private List<MemoryAccess> elongateExecutionPaths(final MemoryAccess execution) {
    // Get the last transition of the execution path.
    final List<MmuTransition> transitions = execution.getTransitions();
    final MmuTransition lastTransition = transitions.get(transitions.size() - 1);
    final MmuAction target = lastTransition.getTarget();

    // Get the outgoing transitions of this action.
    final List<MmuTransition> targetTransitions = memory.getTransitions(target);

    // Elongate the execution path.
    if (targetTransitions != null && !targetTransitions.isEmpty()) {
      final List<MemoryAccess> elongatedExecutionList = new ArrayList<>();

      for (final MmuTransition transition : targetTransitions) {
        if (!transition.isEnabled()) {
          continue;
        }

        final MemoryOperation executionOperation = execution.getOperation();

        final MmuGuard mmuGuard = transition.getGuard();
        final MemoryOperation transitionOperation =
            mmuGuard != null ? mmuGuard.getOperation() : null;

        MemoryOperation operation;

        if (executionOperation == null || transitionOperation == null) {
          operation = executionOperation == null ? transitionOperation : executionOperation;
        } else if (executionOperation.equals(transitionOperation)) {
          operation = executionOperation;
        } else {
          continue;
        }

        final MemoryAccess elongatedExecution =
            new MemoryAccess(operation, memory.getStartAddress());

        elongatedExecution.addTransitions(execution.getTransitions());
        elongatedExecution.addTransition(transition);
        elongatedExecutionList.add(elongatedExecution);
      }

      return elongatedExecutionList;
    }

    return null;
  }
}
