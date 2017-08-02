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
        resultCalls.addAll(defaultCalls);
      } else {
        resultCalls.add(defaultCalls.get(position));
      }
    }

    return new AbstractSequence(defaultSequence.getSection(), resultCalls);
  }

  private static List<AbstractCall> getCallsForPosition(
      final List<AbstractSequence> sequences,
      final int position) {

    for (final AbstractSequence sequence : sequences) {
      final Map<Integer, Integer> positions = sequence.getPositions();
      InvariantChecks.checkNotNull(positions);

      final Integer index = positions.get(position);
      if (null != index) {
        final List<AbstractCall> prologue =
            null != sequence.getPrologues() ? sequence.getPrologues().get(index) : null;

        final AbstractCall call = sequence.getSequence().get(index);

        final List<AbstractCall> epilogue =
            null != sequence.getEpilogues() ? sequence.getEpilogues().get(index) : null;

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
