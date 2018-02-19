/*
 * Copyright 2016-2017 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.test.template.ConcreteCall;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The {@link Code2} class describes the organization of code sections to be simulated.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Code2 {
  private final Map<Long, Entry> addressEntries;
  private final Map<String, Long> handlerAddresses;
  private final Set<Long> breakAddresses;

  public static final class Entry {
    private ConcreteCall call = null;
    private List<ConcreteCall> prePseudoCalls = null;
    private List<ConcreteCall> postPseudoCalls = null;

    public ConcreteCall getCall() {
      return call;
    }

    public List<ConcreteCall> getPrePseudoCalls() {
      return prePseudoCalls;
    }

    public List<ConcreteCall> getPostPseudoCalls() {
      return postPseudoCalls;
    }

    protected void addCall(final ConcreteCall newCall) {
      if (newCall.isExecutable()) {
        InvariantChecks.checkTrue(call == null, "Call is already assigned!");
        call = newCall;
        return;
      }

      if (null == call) {
        prePseudoCalls.add(newCall);
      } else {
        postPseudoCalls.add(newCall);
      }
    }
  }

  public Code2() {
    this.addressEntries = new HashMap<>();
    this.handlerAddresses = new HashMap<>();
    this.breakAddresses = new HashSet<>();
  }

  public void addCall(final ConcreteCall call, final long address) {
    InvariantChecks.checkNotNull(call);

    Entry entry = addressEntries.get(address);
    if (null == entry) {
      entry = new Entry();
      addressEntries.put(address, entry);
    }

    entry.addCall(call);
  }

  public boolean hasAddress(final long address) {
    return addressEntries.containsKey(address);
  }

  public Entry getEntry(final long address) {
    return addressEntries.get(address);
  }

  public void addHandlerAddress(final String id, final long address) {
    InvariantChecks.checkNotNull(id);
    handlerAddresses.put(id, address);
  }

  public boolean hasHandler(final String id) {
    return handlerAddresses.containsKey(id);
  }

  public long getHandlerAddress(final String id) {
    final Long address = handlerAddresses.get(id);
    InvariantChecks.checkNotNull(address);
    return address;
  }

  public boolean isBreakAddress(final long address) {
    return breakAddresses.contains(address);
  }

  public void addBreakAddress(final long address) {
    breakAddresses.add(address);
  }
}
