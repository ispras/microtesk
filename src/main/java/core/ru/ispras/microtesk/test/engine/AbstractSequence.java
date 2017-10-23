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
    private final List<Boolean> flags;
    private final List<Integer> indexes; // index -> position
    private final Map<Integer, List<Integer>> positions; // position -> [indexes]

    public Builder(final Section section) {
      InvariantChecks.checkNotNull(section);

      this.section = section;
      this.sequence = new ArrayList<>();
      this.flags = new ArrayList<>();
      this.indexes = new ArrayList<>();
      this.positions = new HashMap<>();
    }

    public void addCall(final AbstractCall call, final int position) {
      addCall(call, position, true);
    }

    public void addCall(final AbstractCall call, final int position, final boolean significant) {
      InvariantChecks.checkNotNull(call);
      InvariantChecks.checkGreaterOrEqZero(position);

      sequence.add(call);
      flags.add(significant);
      indexes.add(position);

      final int index = sequence.size() - 1;
      final List<Integer> callIndexes = new ArrayList<>();

      callIndexes.add(index);
      positions.put(position, callIndexes);
    }

    public boolean isEmpty() {
      return sequence.isEmpty()  && flags.isEmpty() && indexes.isEmpty() && positions.isEmpty();
    }

    public AbstractSequence build() {
      return new AbstractSequence(section, sequence, flags, indexes, positions);
    }
  }

  private final Section section;
  private final List<AbstractCall> sequence;
  private final List<Boolean> flags;
  private final List<Integer> indexes;
  private final Map<Integer, List<Integer>> positions;

  private Map<Integer, List<AbstractCall>> prologues;
  private Map<Integer, List<AbstractCall>> epilogues;

  public AbstractSequence(final AbstractSequence other) {
    InvariantChecks.checkNotNull(other);

    this.section   = other.section;
    this.sequence  = null != other.sequence  ? new ArrayList<>(other.sequence) : null;
    this.indexes   = null != other.indexes   ? new ArrayList<>(other.indexes)  : null;
    this.flags     = null != other.flags     ? new ArrayList<>(other.flags)    : null;
    this.positions = null != other.positions ? new HashMap<>(other.positions)  : null;
    this.prologues = null != other.prologues ? new HashMap<>(other.prologues)  : null;
    this.epilogues = null != other.epilogues ? new HashMap<>(other.epilogues)  : null;
  }

  public AbstractSequence(final Section section, final List<AbstractCall> sequence) {
    this(section, sequence, null, null, null);
  }

  public AbstractSequence(
      final Section section,
      final List<AbstractCall> sequence,
      final List<Boolean> flags,
      final List<Integer> indexes,
      final Map<Integer, List<Integer>> positions) {
    InvariantChecks.checkNotNull(section);
    InvariantChecks.checkNotNull(sequence);

    this.section = section;
    this.sequence = sequence;
    this.flags = flags;
    this.indexes = indexes;
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

  public List<Boolean> getFlags() {
    return flags;
  }

  public boolean isSignificant(final int index) {
    return null != flags ? flags.get(index) : true;
  }

  public List<Integer> getIndexes() {
    return indexes;
  }

  public Map<Integer, List<Integer>> getPositions() {
    return positions;
  }

  public Map<Integer, List<AbstractCall>> getPrologues() {
    return prologues;
  }

  public Map<Integer, List<AbstractCall>> getEpilogues() {
    return epilogues;
  }

  public void addPrologue(final int index, final AbstractCall call) {
    InvariantChecks.checkNotNull(call);

    final List<AbstractCall> calls = new ArrayList<>();
    calls.add(call);

    addPrologue(index, calls);
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

  public void addEpilogue(final int index, final AbstractCall call) {
    InvariantChecks.checkNotNull(call);

    final List<AbstractCall> calls = new ArrayList<>();
    calls.add(call);

    addEpilogue(index, calls);
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
