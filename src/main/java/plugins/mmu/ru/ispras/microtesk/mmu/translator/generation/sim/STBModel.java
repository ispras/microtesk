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

package ru.ispras.microtesk.mmu.translator.generation.sim;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.generation.spec.MemoryControlFlowExplorer;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBModel implements STBuilder {
  public static final String CLASS_NAME = "Model";

  private static final Class<?> MODEL_CLASS =
      ru.ispras.microtesk.mmu.model.api.MmuModel.class;

  private static final Class<?> BUF_CLASS =
      ru.ispras.microtesk.mmu.model.api.BufferObserver.class;

  private static final Class<?> MEM_CLASS =
      ru.ispras.microtesk.model.api.memory.MemoryDevice.class;

  private final String packageName;
  private final Ir ir;

  public STBModel(final String packageName, final Ir ir) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(ir);

    this.packageName = packageName;
    this.ir = ir;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");
    buildHeader(st);
    buildBody(st, group);
    return st;
  }

  protected final void buildHeader(final ST st) {
    st.add("name", CLASS_NAME); 
    st.add("impls", MODEL_CLASS.getSimpleName());
    st.add("pack", packageName);

    st.add("imps", java.util.Map.class.getName());
    st.add("imps", java.util.HashMap.class.getName());
    st.add("imps", MEM_CLASS.getName());
    st.add("imps", String.format("%s.*", MODEL_CLASS.getPackage().getName()));
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stBody = group.getInstanceOf("body");

    stBody.add("name", CLASS_NAME);
    stBody.add("buf_type", BUF_CLASS.getSimpleName());
    stBody.add("mem_type", MEM_CLASS.getSimpleName()); 

    for (final Buffer buffer : ir.getBuffers().values()) {
      stBody.add("buffers", buffer.getId());
    }

    for (final Segment segment : ir.getSegments().values()) {
      stBody.add("buffers", segment.getId());
    }

    InvariantChecks.checkTrue(ir.getMemories().size() == 1);
    for (final Memory memory : ir.getMemories().values()) {
      stBody.add("memories", memory.getId());

      final MemoryControlFlowExplorer flowExplorer =
          new MemoryControlFlowExplorer(memory);

      final Buffer target = flowExplorer.getTargetBuffer();
      stBody.add("target", target.getId());
    }

    st.add("members", stBody);
  }
}
