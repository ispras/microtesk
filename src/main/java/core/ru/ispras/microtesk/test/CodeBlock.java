/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.test.template.ConcreteCall;

final class CodeBlock {
  private final List<ConcreteCall> calls;
  private final long startAddress;
  private final long endAddress;
  private CodeBlock next;

  public CodeBlock(
      final List<ConcreteCall> calls,
      final long startAddress,
      final long endAddress) {
    InvariantChecks.checkNotEmpty(calls);

    this.startAddress = startAddress;
    this.endAddress = endAddress;
    this.calls = calls;
    this.next = null;
  }

  public List<ConcreteCall> getCalls() {
    return calls;
  }

  public long getStartAddress() {
    return startAddress;
  }

  public long getEndAddress() {
    return endAddress;
  }

  public CodeBlock getNext() {
    return next;
  }

  public void setNext(final CodeBlock block) {
    InvariantChecks.checkNotNull(block);
    InvariantChecks.checkTrue(this.next == null);
    InvariantChecks.checkTrue(this.endAddress == block.startAddress);

    this.next = block;
  }

  public Pair<Long, Long> getOverlapping(final CodeBlock other) {
    InvariantChecks.checkNotNull(other);

    final long start = Math.max(this.startAddress, other.startAddress);
    final long end = Math.min(this.endAddress, other.endAddress);

    return start < end ? new Pair<>(start, end) : null;
  }

  @Override
  public String toString() {
    return String.format(
        "[0x%016x..0x%016x] (%d calls)", startAddress, endAddress, calls.size());
  }
}
