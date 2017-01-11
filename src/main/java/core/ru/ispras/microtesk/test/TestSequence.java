/*
 * Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.ConcreteCall;

/**
 * The {@code TestSequence} class describes a test sequence, a symbolic test program (or a part of a
 * test program) that consists of concrete calls which can be simulated on the microprocessor model
 * or dumped to textual representation (assembler code). The sequence is split into tree parts: (1)
 * prologue that holds the initialization code and (2) body that holds the main code (test case)
 * and (3) self-checks that must be generated after simulating the sequence.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class TestSequence {

  public static final class Builder {
    private final ArrayList<ConcreteCall> prologue;
    private final ArrayList<ConcreteCall> body;
    private final ArrayList<SelfCheck> checks;

    public Builder() {
      this.prologue = new ArrayList<>();
      this.body = new ArrayList<>();
      this.checks = new ArrayList<>();
    }

    private void addTo(final List<ConcreteCall> target, final ConcreteCall call) {
      InvariantChecks.checkNotNull(target);
      InvariantChecks.checkNotNull(call);

      target.add(call);
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

    public void addCheck(final SelfCheck check) {
      InvariantChecks.checkNotNull(check);
      checks.add(check);
    }

    public TestSequence build() {
      return new TestSequence(prologue, body, checks);
    }
  }

  private final List<ConcreteCall> all;
  private final List<ConcreteCall> prologue;
  private final List<ConcreteCall> body;
  private final List<SelfCheck> checks;

  private long startAddress = 0;
  private long endAddress = 0;
  private boolean isAddressSet = false;

  private TestSequence(
      final ArrayList<ConcreteCall> prologue,
      final ArrayList<ConcreteCall> body,
      final ArrayList<SelfCheck> checks) {
    InvariantChecks.checkNotNull(prologue);
    InvariantChecks.checkNotNull(body);
    InvariantChecks.checkNotNull(checks);

    // Checks are expected to be empty if prologue and body are empty (for correct work of isEmpty).
    InvariantChecks.checkTrue(prologue.isEmpty() && body.isEmpty() ? checks.isEmpty() : true);

    final ArrayList<ConcreteCall> allCalls = merge(prologue, body);
    this.all = Collections.unmodifiableList(allCalls);

    this.prologue = prologue.isEmpty() ?
        Collections.<ConcreteCall>emptyList() :
        Collections.unmodifiableList(allCalls.subList(0, prologue.size()));

    this.body = body.isEmpty() ?
        Collections.<ConcreteCall>emptyList() :
        prologue.isEmpty() ?
            all : Collections.unmodifiableList(allCalls.subList(prologue.size(), allCalls.size()));

    this.checks = Collections.unmodifiableList(checks);
  }

  private static <T> ArrayList<T> merge(final ArrayList<T> first, final ArrayList<T> second) {
    if (first.isEmpty()) {
      return second;
    }

    if (second.isEmpty()) {
      return first;
    }

    final ArrayList<T> result = new ArrayList<>(first.size() + second.size());

    result.addAll(first);
    result.addAll(second);

    return result;
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

  public long getStartAddress() {
    InvariantChecks.checkTrue(isAddressSet, "Address is not assigned");
    return startAddress;
  }

  public long getEndAddress() {
    InvariantChecks.checkTrue(isAddressSet, "Address is not assigned");
    return endAddress;
  }

  public long setAddress(final long address) {
    InvariantChecks.checkFalse(isAddressSet, "Address is already assigned");

    this.endAddress = setAddress(all, address);
    this.startAddress = all.isEmpty() ? address : all.get(0).getAddress();
    this.isAddressSet = true;

    return endAddress;
  }

  private static long setAddress(final List<ConcreteCall> calls, final long address) {
    long currentAddress = address;

    for (final ConcreteCall call : calls) {
      currentAddress = call.setAddress(currentAddress);
    }

    return currentAddress;
  }

  public List<SelfCheck> getChecks() {
    return checks;
  }

  public boolean isEmpty() {
    return all.isEmpty();
  }
}
