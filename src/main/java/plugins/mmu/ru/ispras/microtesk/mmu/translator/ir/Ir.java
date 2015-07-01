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

package ru.ispras.microtesk.mmu.translator.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Ir {
  private final Map<String, Address> addresses;
  private final Map<String, Segment> segments;
  private final Map<String, Buffer> buffers;
  private final Map<String, Memory> memories;

  public Ir() {
    this.addresses = new LinkedHashMap<>();
    this.segments = new LinkedHashMap<>();
    this.buffers = new LinkedHashMap<>();
    this.memories = new LinkedHashMap<>();
  }

  public Map<String, Address> getAddresses() {
    return Collections.unmodifiableMap(addresses);
  }

  public Map<String, Segment> getSegments() {
    return Collections.unmodifiableMap(segments);
  }

  public Map<String, Buffer> getBuffers() {
    return Collections.unmodifiableMap(buffers);
  }

  public Map<String, Memory> getMemories() {
    return Collections.unmodifiableMap(memories);
  }

  public void addAddress(final Address address) {
    checkNotNull(address);
    addresses.put(address.getId(), address);
  }

  public void addSegment(final Segment segment) {
    checkNotNull(segment);
    segments.put(segment.getId(), segment);
  }

  public void addBuffer(final Buffer buffer) {
    checkNotNull(buffer);
    buffers.put(buffer.getId(), buffer);
  }

  public void addMemory(final Memory memory) {
    checkNotNull(memory);
    memories.put(memory.getId(), memory);
  }

  @Override
  public String toString() {
    return String.format(
        "Mmu Ir:%n addresses=%s%n segments=%s%n buffers=%s%n memories=%s", 
        addresses,
        segments,
        buffers,
        memories
        );
  }
}
