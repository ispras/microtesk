/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.test.template.ConcreteCall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@link ConcreteSequence} class describes a test sequence, a symbolic test program (or a part
 * of a test program) that consists of concrete calls which can be simulated on the microprocessor
 * model or dumped to textual representation (assembler code). The sequence is split into tree
 * parts: (1) prologue that holds the initialization code and (2) body that holds the main code
 * (test case).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class ConcreteSequence {

  public static final class Builder {
    private final Section section;
    private final List<ConcreteCall> prologue;
    private final List<ConcreteCall> body;
    private int instructionCount;

    public Builder(final Section section) {
      InvariantChecks.checkNotNull(section);

      this.section = section;
      this.prologue = new ArrayList<>();
      this.body = new ArrayList<>();
      this.instructionCount = 0;
    }

    private void addTo(final List<ConcreteCall> target, final ConcreteCall call) {
      InvariantChecks.checkNotNull(target);
      InvariantChecks.checkNotNull(call);

      target.add(call);
      if (call.isInstruction()) {
        instructionCount++;
      }
    }

    private void addTo(final List<ConcreteCall> target, final List<ConcreteCall> calls) {
      InvariantChecks.checkNotNull(calls);
      for (final ConcreteCall call : calls) {
        addTo(target, call);
      }
    }

    public void addToPrologue(final ConcreteCall call) {
      addTo(prologue, call);
    }

    public void addToPrologue(final List<ConcreteCall> calls) {
      addTo(prologue, calls);
    }

    public void add(final ConcreteCall call) {
      addTo(body, call);
    }

    public void add(final List<ConcreteCall> calls) {
      addTo(body, calls);
    }

    public ConcreteSequence build() {
      return new ConcreteSequence(section, prologue, body, instructionCount);
    }
  }

  private final Section section;
  private final List<ConcreteCall> all;
  private final List<ConcreteCall> prologue;
  private final List<ConcreteCall> body;

  private final int instructionCount;
  private String title;

  private boolean allocated;
  private long startAddress;
  private long endAddress;

  private ConcreteSequence(
      final Section section,
      final List<ConcreteCall> prologue,
      final List<ConcreteCall> body,
      final int instructionCount) {
    InvariantChecks.checkNotNull(prologue);
    InvariantChecks.checkNotNull(body);
    InvariantChecks.checkGreaterOrEqZero(instructionCount);

    this.section = section;

    final List<ConcreteCall> allCalls = merge(prologue, body);
    this.all = Collections.unmodifiableList(allCalls);

    this.prologue = prologue.isEmpty()
        ? Collections.<ConcreteCall>emptyList()
        : Collections.unmodifiableList(allCalls.subList(0, prologue.size()));

    this.body = body.isEmpty()
        ? Collections.<ConcreteCall>emptyList()
        : prologue.isEmpty()
            ? all
            : Collections.unmodifiableList(allCalls.subList(prologue.size(), allCalls.size()));

    this.instructionCount = instructionCount;
    this.title = "";

    this.allocated = false;
    this.startAddress = 0;
    this.endAddress = 0;
  }

  private static <T> List<T> merge(final List<T> first, final List<T> second) {
    if (first.isEmpty()) {
      return second;
    }

    if (second.isEmpty()) {
      return first;
    }

    final List<T> result = new ArrayList<>(first.size() + second.size());

    result.addAll(first);
    result.addAll(second);

    return result;
  }

  public Section getSection() {
    return section;
  }

  public List<ConcreteCall> getAll() {
    return all;
  }

  public List<ConcreteCall> getPrologue() {
    return prologue;
  }

  public List<ConcreteCall> getBody() {
    return body;
  }

  public boolean isEmpty() {
    return all.isEmpty();
  }

  public int getInstructionCount() {
    return instructionCount;
  }

  protected String getTitle() {
    return title;
  }

  protected void setTitle(final String value) {
    InvariantChecks.checkNotNull(value);
    this.title = value;
  }

  protected boolean isAllocated() {
    return allocated;
  }

  protected void setAllocationAddresses(final long start, final long end) {
    this.allocated = true;
    this.startAddress = start;
    this.endAddress = end;
  }

  protected long getStartAddress() {
    InvariantChecks.checkTrue(allocated, "Sequence is not allocated!");
    return startAddress;
  }

  protected long getEndAddress() {
    InvariantChecks.checkTrue(allocated, "Sequence is not allocated!");
    return endAddress;
  }

  // TODO: temporary implementation of self-checks.
  // Must be removed once simulation-based is implemented.
  private List<SelfCheck> selfChecks = null;

  public void setSelfChecks(final List<SelfCheck> selfChecks) {
    InvariantChecks.checkNotNull(selfChecks);
    this.selfChecks = selfChecks;
  }

  public List<SelfCheck> getSelfChecks() {
    return selfChecks;
  }
}
