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

package ru.ispras.microtesk.mmu.translator.ir.spec.builder;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;

final class MmuSpecContext {
  private final IntegerVariableTracker variables = new IntegerVariableTracker();

  private final Map<String, MmuBuffer> buffers = new HashMap<>();

  private final Deque<String> prefixStack = new ArrayDeque<>();
  private final Map<String, Integer> prefixVersion = new HashMap<>();

  public MmuSpecContext() {
    prefixStack.push("");
  }

  public IntegerVariableTracker getVariableRegistry() {
    return variables;
  }

  public Map<String, MmuBuffer> getBuffers() {
    return buffers;
  }

  public void registerBuffer(final Variable struct, final MmuBuffer buffer) {
    buffers.put(struct.getName(), buffer);
    for (final Variable field : struct.getFields().values()) {
      registerBuffer(field, buffer);
    }
  }

  public String getPrefix(final String suffix) {
    final String source = prefixStack.peek() + suffix;

    Integer version = prefixVersion.get(source);
    if (version == null) {
      version = 0;
    }
    prefixVersion.put(source, version + 1);

    return String.format("%s_%d", source, version);
  }

  public void pushPrefix(final String prefix) {
    prefixStack.push(prefix);
  }

  public void popPrefix() {
    prefixStack.pop();
  }

  public AtomExtractor newAtomExtractor() {
    return new AtomExtractor(prefixStack.peek(), variables);
  }
}
