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

package ru.ispras.microtesk.mmu.translator.codegen.sim;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Operation;
import ru.ispras.microtesk.mmu.translator.ir.Segment;

final class StbModel implements StringTemplateBuilder {
  public static final String CLASS_NAME = "Model";

  private static final Class<?> MODEL_CLASS =
      ru.ispras.microtesk.mmu.model.sim.MmuModel.class;

  private final String packageName;
  private final Ir ir;
  private final Buffer targetBuffer;

  public StbModel(final String packageName, final Ir ir, final Buffer targetBuffer) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(targetBuffer);

    this.packageName = packageName;
    this.ir = ir;
    this.targetBuffer = targetBuffer;
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
    st.add("ext", MODEL_CLASS.getSimpleName());
    st.add("pack", packageName);

    st.add("imps", MODEL_CLASS.getName());
    st.add("imps",
        ru.ispras.microtesk.mmu.model.sim.BufferInstanceProxy.class.getName());
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stBody = group.getInstanceOf("body");

    stBody.add("name", CLASS_NAME);

    for (final Operation operation : ir.getOperations().values()) {
      stBody.add("ops", operation.getId());
    }

    for (final Buffer buffer : ir.getBuffers().values()) {
      stBody.add("buffers", buffer.getId());
    }

    for (final Segment segment : ir.getSegments().values()) {
      stBody.add("segments", segment.getId());
    }

    InvariantChecks.checkTrue(ir.getMemories().size() == 1);
    for (final Memory memory : ir.getMemories().values()) {
      stBody.add("memories", memory.getId());
      stBody.add("target", targetBuffer.getId());
    }

    st.add("members", stBody);
  }
}
