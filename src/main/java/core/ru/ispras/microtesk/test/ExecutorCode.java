/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.ConcreteCall;

public final class ExecutorCode {
  private final List<ConcreteCall> calls;
  private final Map<Long, Integer> addresses;
  private final Map<String, Long> handlerAddresses;

  public ExecutorCode(
      final List<ConcreteCall> calls,
      final Map<Long, Integer> addresses,
      final Map<String, Long> handlerAddresses) {
    this.calls = calls;
    this.addresses = addresses;
    this.handlerAddresses = handlerAddresses;
  }

  public ExecutorCode() {
    this.calls = new ArrayList<>();
    this.addresses = new LinkedHashMap<>();
    this.handlerAddresses = new LinkedHashMap<>();
  }

  public int getCallCount() {
    return calls.size();
  }

  public boolean isInBounds(final int index) {
    return 0 <= index && index < calls.size();
  }

  public boolean hasAddress(final long address) {
    return addresses.containsKey(address);
  }

  public ConcreteCall getCall(final int index) {
    return calls.get(index);
  }

  public int getCallIndex(final long address) {
    final Integer index = addresses.get(address);
    InvariantChecks.checkNotNull(index);
    return index;
  }

  public ConcreteCall getCallAt(final long address) {
    return getCall(getCallIndex(address));
  }

  public long getCallAddress(final int index) {
    return getCall(index).getAddress();
  }

  public void addCalls(final List<ConcreteCall> calls) {
    InvariantChecks.checkNotNull(calls);
    for (final ConcreteCall call : calls) {
      addCall(call);
    }
  }

  public void addCall(final ConcreteCall call) {
    InvariantChecks.checkNotNull(call);
    calls.add(call);

    final int index = calls.size() - 1;
    final long address = call.getAddress();

    if (!hasAddress(address)) {
      addresses.put(address, index);
      return;
    }

    final int conflictIndex = getCallIndex(address);
    final ConcreteCall conflictCall = getCall(conflictIndex);

    // Several calls can be mapped to the same address when a pseudo call with
    // zero size (label, compiler directive, text output etc.) precedes
    // an executable (non-zero size) call. This is considered normal. When such
    // a situation is detected, address mapping is not changed in order to allow
    // jumping on the beginning of calls mapped to the same address.

    boolean isConflictLegal = false;

    // Tests whether all calls mapped to the given address and preceding the current
    // call (ending with the conflict call) are zero-size. If the condition holds,
    // this is considered legal.

    for (int testIndex = index - 1; testIndex >= 0; --testIndex) {
      final ConcreteCall testCall = getCall(testIndex);

      if (testCall.getByteSize() > 0 || testCall.getAddress() != address) {
        break;
      }

      if (testIndex == conflictIndex) {
        isConflictLegal = true;
        break;
      }
    }

    if (!isConflictLegal) {
      throw new GenerationAbortedException(String.format(
          "Mapping '%s' (index %d): Address 0x%x is already used by '%s' (index %d).",
          call.getText(), index, address, conflictCall.getText(), conflictIndex));
    }
  }

  public boolean hasHandler(final String id) {
    return handlerAddresses.containsKey(id);
  }

  public long getHandlerAddress(final String id) {
    final Long address = handlerAddresses.get(id);
    InvariantChecks.checkNotNull(address);
    return address;
  }

  public void addHanderAddress(final String id, final long address) {
    InvariantChecks.checkNotNull(id);
    handlerAddresses.put(id, address);
  }
}
