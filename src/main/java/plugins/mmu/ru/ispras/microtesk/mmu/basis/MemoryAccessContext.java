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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;

/**
 * {@link MemoryAccessContext} contains data required for buffer access instantiation.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessContext {
  public static final MemoryAccessContext EMPTY = new MemoryAccessContext();

  private final MemoryAccessStack memoryAccessStack;
  private final Map<MmuBuffer, Integer> bufferAccessIds;
  private final Map<MmuBuffer, MmuBufferAccess.Kind> bufferAccessKinds;

  public MemoryAccessContext() {
    this.memoryAccessStack = new MemoryAccessStack();
    this.bufferAccessIds = new HashMap<>();
    this.bufferAccessKinds = new HashMap<>();
  }

  public MemoryAccessContext(final String id) {
    this.memoryAccessStack = new MemoryAccessStack(id);
    this.bufferAccessIds = new HashMap<>();
    this.bufferAccessKinds = new HashMap<>();
  }

  public MemoryAccessContext(final MemoryAccessContext r) {
    this.memoryAccessStack = new MemoryAccessStack(r.memoryAccessStack);
    this.bufferAccessIds = new HashMap<>(r.bufferAccessIds);
    this.bufferAccessKinds = new HashMap<>(r.bufferAccessKinds);
  }

  public boolean isInitial() {
    return memoryAccessStack.isEmpty();
  }

  public boolean isInitial(final MmuBuffer buffer) {
    return memoryAccessStack.isEmpty() && getBufferAccessId(buffer) == 0;
  }

  public int getBufferAccessId(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);

    final Integer bufferAccessId = bufferAccessIds.get(buffer);
    return bufferAccessId != null ? bufferAccessId.intValue() : -1;
  }

  public MemoryAccessStack getMemoryAccessStack() {
    return memoryAccessStack;
  }

  public void doAccess(final MmuBufferAccess bufferAccess) {
    InvariantChecks.checkNotNull(bufferAccess);

    final MmuBuffer buffer = bufferAccess.getBuffer();
    final MmuBufferAccess.Kind nowKind = bufferAccess.getKind();
    final MmuBufferAccess.Kind preKind = bufferAccessKinds.get(buffer);

    if (nowKind != MmuBufferAccess.Kind.WRITE && (preKind != MmuBufferAccess.Kind.CHECK)) {
      bufferAccessIds.put(buffer, getBufferAccessId(buffer) + 1);
    }

    bufferAccessKinds.put(buffer, nowKind);
  }

  public MemoryAccessStack.Frame doCall(final String frameId) {
    return memoryAccessStack.call(frameId);
  }

  public MemoryAccessStack.Frame doReturn() {
    return memoryAccessStack.ret();
  }

  public IntegerVariable getInstance(final IntegerVariable variable) {
    return memoryAccessStack.getInstance(variable);
  }

  public IntegerField getInstance(final IntegerField field) {
    return memoryAccessStack.getInstance(field);
  }

  @Override
  public String toString() {
    return String.format("%s, %s", memoryAccessStack, bufferAccessIds);
  }
}
