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

package ru.ispras.microtesk.test.engine;

import java.util.List;

import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Label;

public final class CodeBlock {
  private final Label label;
  private final long startAddress;
  private final long endAddress;
  private final List<ConcreteCall> calls;
  private final CodeBlock next;

  public CodeBlock(
      final Label label,
      final long startAddress,
      final long endAddress,
      final List<ConcreteCall> calls,
      final CodeBlock next) {
    this.label = label;
    this.startAddress = startAddress;
    this.endAddress = endAddress;
    this.calls = calls;
    this.next = next;
  }

  public Label getLabel() {
    return label;
  }

  public long getStartAddress() {
    return startAddress;
  }

  public long getEndAddress() {
    return endAddress;
  }

  public List<ConcreteCall> getCalls() {
    return calls;
  }

  public CodeBlock getNext() {
    return next;
  }
}
