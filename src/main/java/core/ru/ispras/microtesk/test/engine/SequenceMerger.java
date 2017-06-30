/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.AbstractCall;

public final class SequenceMerger {
  public static final AbstractSequence merge(
      final AbstractSequence originalSequence,
      final List<AbstractSequence> engineSequences) {
    InvariantChecks.checkNotNull(originalSequence);
    InvariantChecks.checkNotNull(engineSequences);

    if (engineSequences.isEmpty()) {
      return originalSequence;
    }

    final List<AbstractCall> originalCalls = originalSequence.getSequence();
    final List<AbstractCall> resultCalls = new ArrayList<>();

    for (int position = 0; position < originalCalls.size(); position++) {
      final List<AbstractCall> calls = getCallsForPosition(engineSequences, position);
      if (null != calls) {
        resultCalls.addAll(originalCalls);
      } else {
        resultCalls.add(originalCalls.get(position));
      }
    }

    return new AbstractSequence(originalSequence.getSection(), resultCalls);
  }

  private static List<AbstractCall> getCallsForPosition(
      final List<AbstractSequence> sequences,
      final int position) {

    for (final AbstractSequence sequence : sequences) {
      final Map<Integer, Integer> positions = sequence.getPositions();
      InvariantChecks.checkNotNull(positions);

      final Integer index = positions.get(position);
      if (null != index) {
        final List<AbstractCall> prologue = sequence.getPrologues().get(index);
        final AbstractCall call = sequence.getSequence().get(index);
        final List<AbstractCall> epilogue = sequence.getEpilogues().get(index);

        return merge(prologue, call, epilogue);
      }
    }

    return null;
  }

  private static List<AbstractCall> merge(
      final List<AbstractCall> prologue,
      final AbstractCall call,
      final List<AbstractCall> epilogue) {
    InvariantChecks.checkNotNull(call);
    final List<AbstractCall> result = new ArrayList<>();

    if (null != prologue) {
      result.addAll(prologue);
    }

    result.add(call);

    if (null != epilogue) {
      result.addAll(epilogue);
    }

    return result;
  }
}
