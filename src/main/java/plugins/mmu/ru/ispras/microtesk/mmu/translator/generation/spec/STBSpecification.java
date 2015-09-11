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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBSpecification implements STBuilder {
  public static final Class<?> SPEC_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem.class;

  public static final String CLASS_NAME = "Specification";

  private final String packageName;
  private final Ir ir;

  public STBSpecification(final String packageName, final Ir ir) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(ir);

    this.packageName = packageName;
    this.ir = ir;
  }

  protected final void buildHeader(final ST st) {
    st.add("name",  CLASS_NAME); 
    st.add("pack",  packageName);
    st.add("impls", SPEC_CLASS.getSimpleName() + ".Holder");
    st.add("imps",  SPEC_CLASS.getName());
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");
    buildHeader(st);
    buildBody(st, group);
    return st;
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stBody = group.getInstanceOf("body");

    stBody.add("name", CLASS_NAME);
    stBody.add("spec", SPEC_CLASS.getSimpleName());
    st.add("members", stBody);

    registerAddresses(stBody, group);
    registerBuffers(stBody, group);
    registerSegments(stBody, group);
    registerMemories(stBody, group);

    final Map<String, Memory> memories = ir.getMemories();
    InvariantChecks.checkFalse(memories.isEmpty(), "Collection of mmu specifications is empty.");

    if (memories.size() > 1) {
      throw new IllegalStateException("Only one mmu specification is allowed.");
    }

    final Memory memory = memories.values().iterator().next();
    InvariantChecks.checkNotNull(memory, "Specification mmu is null.");

    buildEntryPoint(memory, stBody, group); 
  }

  private void registerAddresses(final ST st, final STGroup group) {
    for(final Address address : ir.getAddresses().values()) {
      final String name = address.getId();
      final ST stReg = group.getInstanceOf("address_reg");

      stReg.add("name", name);
      st.add("stmts", stReg);
    }
  }

  private void registerBuffers(final ST st, final STGroup group) {
    st.add("stmts", "");
    for(final Buffer buffer : ir.getBuffers().values()) {
      final ST stReg = group.getInstanceOf("buffer_reg");
      stReg.add("name", buffer.getId());
      st.add("stmts", stReg);
    }
  }

  private void registerSegments(final ST st, final STGroup group) {
    st.add("stmts", "");
    for(final Segment segment : ir.getSegments().values()) {
      final ST stReg = group.getInstanceOf("segment_reg");
      stReg.add("name", segment.getId());
      st.add("stmts", stReg);
    }
  }

  private void registerMemories(final ST st, final STGroup group) {
    st.add("stmts", "");
    for(final Memory memory : ir.getMemories().values()) {
      final String id = memory.getId();
      st.add("stmts", String.format("final %s %s = new %s(builder);", id, id, id));
    }
  }

  private void buildEntryPoint(final Memory memory, final ST st, final STGroup group) {
    st.add("stmts", "");

    final MemoryControlFlowExplorer flowExplorer =
        new MemoryControlFlowExplorer(memory);

    final Buffer targetBuffer = flowExplorer.getTargetBuffer();

    final ST stVA = group.getInstanceOf("set_va");
    stVA.add("name", memory.getAddress().getId());
    st.add("stmts", stVA);

    final ST stPA = group.getInstanceOf("set_pa");
    stPA.add("name", targetBuffer.getAddress().getId());
    st.add("stmts", stPA);

    final ST stStart = group.getInstanceOf("set_start_action");
    stStart.add("name", memory.getId());
    st.add("stmts", stStart);

    final ST stTarget = group.getInstanceOf("set_target_buffer");
    stTarget.add("name", targetBuffer.getId());
    st.add("stmts", stTarget);
  }
}
