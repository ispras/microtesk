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

package ru.ispras.microtesk.translator.mmu.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Ir {
  private final Map<String, Address> addresses;
  private final Map<String, Buffer> buffers;
  private final Map<String, Memory> memories;

  public Ir() {
    this.addresses = new LinkedHashMap<String, Address>();
    this.buffers = new LinkedHashMap<String, Buffer>();
    this.memories = new LinkedHashMap<String, Memory>();
  }

  public Map<String, Address> getAddresses() {
    return Collections.unmodifiableMap(addresses);
  }

  public Map<String, Buffer> getBuffers() {
    return Collections.unmodifiableMap(buffers);
  }

  public Map<String, Memory> getMemories() {
    return Collections.unmodifiableMap(memories);
  }

  public void addAddress(String name, Address value) {
    checkNotNull(name);
    checkNotNull(value);

    addresses.put(name, value);
  }

  public void addBuffer(String name, Buffer value) {
    checkNotNull(name);
    checkNotNull(value);

    buffers.put(name, value);
  }
}
