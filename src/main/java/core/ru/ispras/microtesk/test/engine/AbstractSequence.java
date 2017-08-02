/*
 * Copyright 2014-2017 ISP RAS (http://www.ispras.ru)
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.CollectionUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.utils.StringUtils;

public final class AbstractSequence {
  public static final class Builder {
    private final Section section;
    private final List<AbstractCall> sequence;
    private final Map<Integer, Integer> positions;

    public Builder(final Section section) {
      InvariantChecks.checkNotNull(section);

      this.section = section;
      this.sequence = new ArrayList<>();
      this.positions = new HashMap<>();
    }

    public void addCall(final AbstractCall call, final int position) {
      InvariantChecks.checkNotNull(call);
      InvariantChecks.checkGreaterOrEqZero(position);

      sequence.add(call);
      positions.put(position, sequence.size() - 1);
    }

    public boolean isEmpty() {
      return sequence.isEmpty() && positions.isEmpty();
    }

    public AbstractSequence build() {
      return new AbstractSequence(section, sequence, positions);
    }
  }

  private final Section section;
  private final List<AbstractCall> sequence;
  private final Map<Integer, Integer> positions;

  private Map<Integer, List<AbstractCall>> prologues;
  private Map<Integer, List<AbstractCall>> epilogues;

  public AbstractSequence(final Section section, final List<AbstractCall> sequence) {
    this(section, sequence, null);
  }

  private AbstractSequence(
      final Section section,
      final List<AbstractCall> sequence,
      final Map<Integer, Integer> positions) {
    InvariantChecks.checkNotNull(section);
    InvariantChecks.checkNotNull(sequence);

    this.section = section;
    this.sequence = sequence;
    this.positions = positions;

    this.prologues = null;
    this.epilogues = null;
  }

  public Section getSection() {
    return section;
  }

  public List<AbstractCall> getSequence() {
    return sequence;
  }

  public Map<Integer, Integer> getPositions() {
    return positions;
  }

  public Map<Integer, List<AbstractCall>> getPrologues() {
    return prologues;
  }

  public Map<Integer, List<AbstractCall>> getEpilogues() {
    return epilogues;
  }

  public void addPrologue(final int index, final List<AbstractCall> calls) {
    InvariantChecks.checkBounds(index, sequence.size());
    InvariantChecks.checkNotNull(calls);

    if (null == prologues) {
      prologues = new HashMap<>();
    }

    final List<AbstractCall> oldCalls = prologues.get(index);
    prologues.put(index, null == oldCalls ? calls : CollectionUtils.appendToList(oldCalls, calls));
  }

  public void addEpilogue(final int index, final List<AbstractCall> calls) {
    InvariantChecks.checkBounds(index, sequence.size());
    InvariantChecks.checkNotNull(calls);

    if (null == epilogues) {
      epilogues = new HashMap<>();
    }

    final List<AbstractCall> oldCalls = epilogues.get(index);
    epilogues.put(index, null == oldCalls ? calls : CollectionUtils.appendToList(oldCalls, calls));
  }

  @Override
  public String toString() {
    return StringUtils.toString(sequence, System.lineSeparator());
  }
}
