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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.ConcreteCall;

public final class CodeBlock {
  private final List<ConcreteCall> calls;

  public CodeBlock(final List<ConcreteCall> calls) {
    InvariantChecks.checkNotEmpty(calls);
    this.calls = calls;
  }

  public long getStartAddress() {
    return calls.get(0).getAddress();
  }

  public long getEndAddress() {
    return calls.get(calls.size()-1).getAddress();
  }

  public List<ConcreteCall> getCalls() {
    return calls;
  }
}
