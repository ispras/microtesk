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

import ru.ispras.microtesk.test.template.ConcreteCall;

/**
 * The {@code TestSequence} class describes a test sequence, a symbolic test program (or a part of a
 * test program) that consists of concrete calls which can be simulated on the microprocessor model
 * or dumped to textual representation (assembler code). The sequence is split into two parts: (1)
 * prologue that holds the initialization code and (2) body that holds the main code (test case).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

final class TestSequence {

  public static final class Builder {
    private final List<ConcreteCall> prologue;
    private final List<ConcreteCall> body;
    private int byteSize;

    public Builder() {
      this.prologue = new ArrayList<ConcreteCall>();
      this.body = new ArrayList<ConcreteCall>();
      this.byteSize = 0;
    }

    public void addToPrologue(final ConcreteCall call) {
      checkNotNull(call);
      prologue.add(call);
      byteSize += call.getByteSize();
    }

    public void add(final ConcreteCall call) {
      checkNotNull(call);
      body.add(call);
      byteSize += call.getByteSize();
    }

    public TestSequence build() {
      return new TestSequence(byteSize, prologue, body);
    }
  }

  private final List<ConcreteCall> prologue;
  private final List<ConcreteCall> body;
  private final int byteSize;

  private long address = 0;
  private boolean isAddressSet = false;

  private TestSequence(
      final int byteSize,
      final List<ConcreteCall> prologue,
      final List<ConcreteCall> body) {
    checkNotNull(prologue);
    checkNotNull(body);

    this.byteSize = byteSize;
    this.prologue = Collections.unmodifiableList(prologue);
    this.body = Collections.unmodifiableList(body);
  }

  public List<ConcreteCall> getPrologue() {
    return prologue;
  }

  public List<ConcreteCall> getBody() {
    return body;
  }

  public int getByteSize() {
    return byteSize;
  }

  public long getAddress() {
    if (!isAddressSet) {
      throw new IllegalStateException("Address is not assigned");
    }

    return address;
  }

  public void setAddress(final long address) {
    this.address = address;
    this.isAddressSet = true;

    long currentCallAddress = address;
    for (final ConcreteCall call : prologue) {
      call.setAddress(currentCallAddress);
      currentCallAddress += call.getByteSize();
    }

    for (final ConcreteCall call : body) {
      call.setAddress(currentCallAddress);
      currentCallAddress += call.getByteSize();
    }
  }
}
