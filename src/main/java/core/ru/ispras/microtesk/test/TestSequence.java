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

package ru.ispras.microtesk.test;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.ConcreteCall;

/**
 * The {@code TestSequence} class describes a test sequence, a symbolic test program (or a part of a
 * test program) that consists of concrete calls which can be simulated on the microprocessor model
 * or dumped to textual representation (assembler code). The sequence is split into two parts: (1)
 * prologue that holds the initialization code and (2) body that holds the main code (test case).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class TestSequence {

  public static final class Builder {
    private final List<ConcreteCall> prologue;
    private final List<ConcreteCall> body;

    private int byteSize;
    private int instructionCount;

    public Builder() {
      this.prologue = new ArrayList<>();
      this.body = new ArrayList<>();
      this.byteSize = 0;
      this.instructionCount = 0;
    }

    private void addTo(final List<ConcreteCall> target, final ConcreteCall call) {
      checkNotNull(target);
      checkNotNull(call);

      target.add(call);
      byteSize += call.getByteSize();

      if (call.isExecutable()) {
        instructionCount++;
      }
    }

    private void addTo(final List<ConcreteCall> target, final List<ConcreteCall> calls) {
      checkNotNull(calls);
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

    public TestSequence build() {
      return new TestSequence(prologue, body, byteSize, instructionCount);
    }
  }

  private final List<ConcreteCall> prologue;
  private final List<ConcreteCall> body;

  private final int byteSize;
  private final int instructionCount;

  private long endAddress = 0;
  private boolean isEndAddressSet = false;

  private TestSequence(
      final List<ConcreteCall> prologue,
      final List<ConcreteCall> body,
      final int byteSize,
      final int instructionCount) {
    checkNotNull(prologue);
    checkNotNull(body);

    this.prologue = Collections.unmodifiableList(prologue);
    this.body = Collections.unmodifiableList(body);

    this.byteSize = byteSize;
    this.instructionCount = instructionCount;
  }

  public List<ConcreteCall> getPrologue() {
    return prologue;
  }

  public List<ConcreteCall> getBody() {
    return body;
  }

  public List<ConcreteCall> getAll() {
    final List<ConcreteCall> result = new ArrayList<>(prologue.size() + body.size());

    result.addAll(prologue);
    result.addAll(body);

    return Collections.unmodifiableList(result);
  }

  public int getByteSize() {
    return byteSize;
  }

  public long getEndAddress() {
    InvariantChecks.checkTrue(isEndAddressSet, "Address is not assigned");
    return endAddress;
  }

  public long setAddress(final long address) {
    long currentAddress = address;
    for (final ConcreteCall call : prologue) {
      currentAddress = call.setAddress(currentAddress);
    }

    for (final ConcreteCall call : body) {
      currentAddress = call.setAddress(currentAddress);
    }

    this.endAddress = currentAddress;
    this.isEndAddressSet = true;

    return currentAddress;
  }

  public int getInstructionCount() {
    return instructionCount;
  }
}
