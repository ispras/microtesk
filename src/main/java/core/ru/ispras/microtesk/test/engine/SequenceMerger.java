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
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * The {@link SequenceMerger} class is responsible for merging instruction sequences describing
 * parts of a test case processed with different engines.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class SequenceMerger implements Iterator<AbstractSequence> {
  private final AbstractSequence defaultSequence;
  private final Iterator<List<AbstractSequence>> sequenceIterator;

  public SequenceMerger(
      final AbstractSequence defaultSequence,
      final Iterator<List<AbstractSequence>> sequenceIterator) {
    InvariantChecks.checkNotNull(defaultSequence);
    InvariantChecks.checkNotNull(sequenceIterator);

    this.defaultSequence = defaultSequence;
    this.sequenceIterator = sequenceIterator;
  }

  @Override
  public void init() {
    sequenceIterator.init();
  }

  @Override
  public boolean hasValue() {
    return sequenceIterator.hasValue();
  }

  @Override
  public AbstractSequence value() {
    final List<AbstractSequence> sequences = sequenceIterator.value();
    return merge(defaultSequence, sequences);
  }

  @Override
  public void next() {
    sequenceIterator.next();
  }

  @Override
  public void stop() {
    sequenceIterator.stop();
  }

  @Override
  public Iterator<AbstractSequence> clone() {
    throw new UnsupportedOperationException();
  }

  private static final AbstractSequence merge(
      final AbstractSequence defaultSequence,
      final List<AbstractSequence> sequences) {
    InvariantChecks.checkNotNull(defaultSequence);
    InvariantChecks.checkNotNull(sequences);

    if (sequences.isEmpty()) {
      return defaultSequence;
    }

    final List<AbstractCall> defaultCalls = defaultSequence.getSequence();
    final List<AbstractCall> resultCalls = new ArrayList<>();

    for (int position = 0; position < defaultCalls.size(); position++) {
      final List<AbstractCall> calls = getCallsForPosition(sequences, position);
      if (null != calls) {
        resultCalls.addAll(calls);
      } else {
        resultCalls.add(defaultCalls.get(position));
      }
    }

    return new AbstractSequence(defaultSequence.getSection(), resultCalls);
  }

  private static List<AbstractCall> getCallsForPosition(
      final List<AbstractSequence> sequences,
      final int position) {

    final List<AbstractCall> prologue = new ArrayList<>();
    AbstractCall call = null;
    boolean isCallSignificant = false;
    final List<AbstractCall> epilogue = new ArrayList<>();

    for (final AbstractSequence sequence : sequences) {
      final Map<Integer, Integer> positions = sequence.getPositions();
      InvariantChecks.checkNotNull(positions);

      final Integer index = positions.get(position);
      if (null != index) {
        final List<AbstractCall> sequencePrologue = null != sequence.getPrologues() ?
            sequence.getPrologues().get(index) : null;

        if (null != sequencePrologue) {
          prologue.addAll(sequencePrologue);
        }

        final List<AbstractCall> sequenceEpilogue = null != sequence.getEpilogues() ?
            sequence.getEpilogues().get(index) : null;

        if (null != sequenceEpilogue) {
          epilogue.addAll(sequenceEpilogue);
        }

        final AbstractCall sequenceCall = sequence.getSequence().get(index);
        final boolean isSequenceCallSignificant = sequence.isSignificant(index);

        // Call is not selected yet. Select the new.
        if (null == call) {
          call = sequenceCall;
          isCallSignificant = isSequenceCallSignificant;
          continue;
        }

        // Call is selected. New selection is insignificant.
        if (!isSequenceCallSignificant) {
          // Do not modify anything.
          continue;
        }

        // Call is selected. Old selection is insignificant. New selection is applied.
        if (!isCallSignificant) {
          call = sequenceCall;
          isCallSignificant = isSequenceCallSignificant;
          continue;
        }

        // Call is selected. Old and new selections are significant.
        // New is ignored. Warning is printed.
        Logger.warning(
            "Instruction at position %d was selected by several engines. " +
            "Only results of the first engine were accepted.", position);
      }
    }

    final List<AbstractCall> result = new ArrayList<>();

    result.addAll(prologue);
    if (null != call) {
      result.add(call);
    }
    result.addAll(epilogue);

    return result.isEmpty() ? null : result;
  }
}
