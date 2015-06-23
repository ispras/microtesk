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

package ru.ispras.microtesk.test.sequence.branch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.Solver;
import ru.ispras.microtesk.test.SolverResult;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.branch.internal.BranchEntry;
import ru.ispras.microtesk.test.sequence.branch.internal.BranchStructure;
import ru.ispras.microtesk.test.sequence.branch.internal.BranchStructureExecutionIterator;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;
import ru.ispras.microtesk.test.sequence.iterator.SingleValueIterator;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.Label;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchTemplateSolver implements Solver<BranchTemplateSolution> {
  private final int delaySlotSize;
  private final int maxBranchExecution;

  public BranchTemplateSolver(final int delaySlotSize, final int maxBranchExecution) {
    InvariantChecks.checkTrue(delaySlotSize >= 0);

    this.delaySlotSize = delaySlotSize;
    this.maxBranchExecution = maxBranchExecution;
  }

  @Override
  public Class<BranchTemplateSolution> getSolutionClass() {
    return BranchTemplateSolution.class;
  }

  @Override
  public SolverResult<BranchTemplateSolution> solve(final Sequence<Call> abstractSequence) {
    // Collect information about labels.
    final Map<Label, Integer> labels = new HashMap<>();

    for (int i = 0; i < abstractSequence.size(); i++) {
      final Call call = abstractSequence.get(i);

      for (final Label label : call.getLabels()) {
        labels.put(label, i);
      }
    }

    // Transform the abstract sequence into the branch structure.
    final BranchStructure branchStructure = new BranchStructure(abstractSequence.size());

    int delaySlot = 0;
    for (int i = 0; i < abstractSequence.size(); i++) {
      final Call call = abstractSequence.get(i);
      final BranchEntry branchEntry = branchStructure.get(i);

      final boolean isIfThen = call.isBranch() && call.isConditionalBranch();
      final boolean isGoto = call.isBranch() && !call.isConditionalBranch();

      // Set the branch entry type.
      if (isIfThen) {
        // Using branches in delay slots is prohibited.
        InvariantChecks.checkTrue(delaySlot == 0);
        branchEntry.setType(BranchEntry.Type.IF_THEN);
      } else if (isGoto) {
        // Using branches in delay slots is prohibited.
        InvariantChecks.checkTrue(delaySlot == 0);
        branchEntry.setType(BranchEntry.Type.GOTO);
      } else if (delaySlot > 0) {
        branchEntry.setType(BranchEntry.Type.DELAY_SLOT);
        delaySlot--;
      } else {
        branchEntry.setType(BranchEntry.Type.BASIC_BLOCK);
      }

      // Set the target label and start the delay slot.
      if (isIfThen || isGoto) {
        final Label label = call.getTargetLabel();
        InvariantChecks.checkTrue(labels.containsKey(label));

        branchEntry.setBranchLabel(labels.get(label));
        delaySlot = delaySlotSize;
      }
    }

    final Iterator<BranchTemplateSolution> iterator = new Iterator<BranchTemplateSolution>() {
      /** Iterator of branch structures. */
      private final Iterator<BranchStructure> branchStructureIterator =
          new SingleValueIterator<BranchStructure>(branchStructure);

      /** Iterator of branch structures and execution trances. */
      private final BranchStructureExecutionIterator branchStructureExecutionIterator =
          new BranchStructureExecutionIterator(branchStructureIterator, maxBranchExecution);

      @Override
      public void init() {
        branchStructureExecutionIterator.init();
      }

      @Override
      public boolean hasValue() {
        return branchStructureExecutionIterator.hasValue();
      }

      @Override
      public BranchTemplateSolution value() {
        final BranchTemplateSolution branchTemplateSolution = new BranchTemplateSolution();
        branchTemplateSolution.setBranchStructure(branchStructureExecutionIterator.value());

        return branchTemplateSolution;
      }

      @Override
      public void next() {
        branchStructureExecutionIterator.next();
      }
    };

    return new SolverResult<>(SolverResult.Status.OK, iterator, Collections.<String>emptyList());
  }
}
