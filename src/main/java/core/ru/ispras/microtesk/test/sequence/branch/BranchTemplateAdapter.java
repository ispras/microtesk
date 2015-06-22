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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.test.Adapter;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.template.BlockId;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.LabelReference;
import ru.ispras.microtesk.test.template.Output;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BranchTemplateAdapter implements Adapter<BranchTemplateSolution> {
  @Override
  public Class<BranchTemplateSolution> getSolutionClass() {
    return BranchTemplateSolution.class;
  }

  private Sequence<Call> construct(
      final Sequence<Call> sequence, final BranchTemplateSolution solution) {
    final Map<Integer, Sequence<Call>> steps = new LinkedHashMap<Integer, Sequence<Call>>();
    final Set<Integer> slots = new LinkedHashSet<Integer>();

    final Map<Integer, BranchTraceSituation> situations = solution.getSituations();

    for (int i = 0; i < sequence.size(); i++) {
      final Call call = sequence.get(i);

      if (call.isBranch()) {
        final BranchTraceSituation situation = situations.get(i);
        final Set<Integer> blockCoverage = situation.getBlockCoverage();

        final String labelString = String.format("label%d", situation.getBranchLabel()); // TODO:
        final Label label = new Label(labelString, new BlockId()); // TODO:

        Sequence<Call> labelSequence = steps.get(situation.getBranchLabel());
        boolean insertLabel = true;

        if (labelSequence == null) {
          labelSequence = new Sequence<Call>();
          labelSequence.add(new Call(null, Collections.<Label>emptyList(),
              Collections.<LabelReference>emptyList(), Collections.<Output>emptyList()));
          steps.put(situation.getBranchLabel(), labelSequence);
        } else {
          final Call labelCall = labelSequence.get(0);
          insertLabel = !labelCall.getLabels().contains(label);
        }

        if (insertLabel) {
          final Call labelCall = labelSequence.get(0);
          labelCall.getLabels().add(label);
        }

        // TODO: situation.init(processor, context);

        // Use basic blocks if it is possible.
        if (situation.canInsertStepIntoBlock()) {
          for (final int block : blockCoverage) {
            final Sequence<Call> step = situation.step();

            if (!step.isEmpty()) {
              Sequence<Call> program = steps.get(block);

              if (program == null) {
                steps.put(block, program = new Sequence<Call>());
              }

              program.addAll(step);
            }
          }
        }
        // Use delay slots.
        else {
          // If instruction can nullify a delay slot, do not use delay slots.
          /* TODO:
          if (call.doesNullifyDelaySlot()) {
            return null;
          }
          */

          if (!situation.canInsertStepIntoSlot()) {
            return null;
          }

          final Sequence<Call> step = situation.step();

          if (step == null) {
            return null;
          }

          if (!step.isEmpty()) {
            final int slotPosition = i + 1;

            // Only one instruction can be inserted into delay slot.
            if (step.size() > 1) {
              return null;
            }

            Sequence<Call> program = steps.get(slotPosition);

            if (program == null) {
              program = new Sequence<Call>();
            }

            program.addAll(step);

            slots.add(i + 1);
            steps.put(i + 1, program);
          }
        }
      }
    }

    int correction = 0;

    final Sequence<Call> result = new Sequence<Call>();
    result.addAll(sequence);

    for (final Map.Entry<Integer, Sequence<Call>> entry : steps.entrySet()) {
      final Integer position = entry.getKey();
      final Sequence<Call> program = entry.getValue();

      result.addAll(position + correction, program);

      if (slots.contains(position)) {
        result.remove(position + correction-- + program.size());
      }

      correction += program.size();
    }

    return result;
  }

  @Override
  public TestSequence adapt(
      final Sequence<Call> abstractSequence, final BranchTemplateSolution solution) {
    // TODO:
    construct(abstractSequence, solution);
    // TODO:
    return null;
  }
}
