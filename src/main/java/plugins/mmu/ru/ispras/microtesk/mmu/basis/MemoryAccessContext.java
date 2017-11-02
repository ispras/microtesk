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

package ru.ispras.microtesk.mmu.basis;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.transformer.Transformer;
import ru.ispras.fortress.transformer.VariableProvider;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;

/**
 * {@link MemoryAccessContext} contains data required for buffer access instantiation.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessContext {
  public static final int BUFFER_ACCESS_INITIAL_ID = 1;
  public static final MemoryAccessContext EMPTY = new MemoryAccessContext();

  private final MemoryAccessStack memoryAccessStack;
  private final Map<MmuBuffer, Integer> bufferAccessIds;
  private final Map<MmuBuffer, BufferAccessEvent> bufferAccessEvents;

  public MemoryAccessContext() {
    this.memoryAccessStack = new MemoryAccessStack();
    this.bufferAccessIds = new HashMap<>();
    this.bufferAccessEvents = new HashMap<>();
  }

  public MemoryAccessContext(final String id) {
    this.memoryAccessStack = new MemoryAccessStack(id);
    this.bufferAccessIds = new HashMap<>();
    this.bufferAccessEvents = new HashMap<>();
  }

  public MemoryAccessContext(final MemoryAccessContext r) {
    this.memoryAccessStack = new MemoryAccessStack(r.memoryAccessStack);
    this.bufferAccessIds = new HashMap<>(r.bufferAccessIds);
    this.bufferAccessEvents = new HashMap<>(r.bufferAccessEvents);
  }

  public boolean isEmptyStack() {
    return memoryAccessStack.isEmpty();
  }

  public int getBufferAccessId(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);

    final Integer bufferAccessId = bufferAccessIds.get(buffer);
    return bufferAccessId != null ? bufferAccessId.intValue() : (BUFFER_ACCESS_INITIAL_ID - 1);
  }

  public MemoryAccessStack getMemoryAccessStack() {
    return memoryAccessStack;
  }

  public void doAccess(final MmuBufferAccess bufferAccess) {
    InvariantChecks.checkNotNull(bufferAccess);

    final MmuBuffer buffer = bufferAccess.getBuffer();
    final BufferAccessEvent nowEvent = bufferAccess.getEvent();
    final BufferAccessEvent preEvent = bufferAccessEvents.get(buffer);

    final boolean nowCheck =
        nowEvent == BufferAccessEvent.HIT || nowEvent == BufferAccessEvent.MISS; 
    final boolean preCheck =
        preEvent == BufferAccessEvent.HIT || preEvent == BufferAccessEvent.MISS; 

    if ((nowCheck || nowEvent == BufferAccessEvent.READ) && !preCheck) {
      bufferAccessIds.put(buffer, getBufferAccessId(buffer) + 1);
    }

    bufferAccessEvents.put(buffer, nowEvent);
  }

  public MemoryAccessStack.Frame doCall(final String frameId, final MmuTransition transition) {
    return memoryAccessStack.call(frameId, transition);
  }

  public MemoryAccessStack.Frame doReturn() {
    return memoryAccessStack.ret();
  }

  public NodeVariable getInstance(final String instanceId, final NodeVariable variable) {
    InvariantChecks.checkNotNull(variable);

    final NodeVariable instance = getVariable(instanceId, variable);
    return memoryAccessStack.getInstance(instance);
  }

  public Node getInstance(final String instanceId, final Node node) {
    if (node == null) {
      return null;
    }

    return Transformer.substitute(node, new VariableProvider() {
      @Override
      public Variable getVariable(final Variable variable) {
        return getInstance(instanceId, new NodeVariable(variable)).getVariable();
      }
    });
  }

  private static NodeVariable getVariable(final String instanceId, final NodeVariable variable) {
    if (instanceId == null) {
      return variable;
    }

    final String instanceName = String.format("%s_%s", variable.getName(), instanceId);
    return new NodeVariable(instanceName, variable.getDataType());
  }

  @Override
  public String toString() {
    return String.format("buffers=%s, stack=%d", bufferAccessIds, memoryAccessStack.size());
  }
}
