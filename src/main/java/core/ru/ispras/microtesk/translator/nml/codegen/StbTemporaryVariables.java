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

package ru.ispras.microtesk.translator.nml.codegen;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.model.memory.Memory;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;

import java.math.BigInteger;

final class StbTemporaryVariables implements StringTemplateBuilder {
  public static final String CLASS_NAME = "TempVars";
  private final Ir ir;

  public StbTemporaryVariables(final Ir ir) {
    this.ir = ir;
  }

  private void buildHeader(final ST st) {
    st.add("name", CLASS_NAME);
    st.add("pack", String.format(PackageInfo.MODEL_PACKAGE_FORMAT, ir.getModelName()));
    st.add("ext", ru.ispras.microtesk.model.TemporaryVariables.class.getSimpleName());

    st.add("imps", BigInteger.class.getName());
    st.add("imps", ru.ispras.microtesk.model.TemporaryVariables.class.getName());
    st.add("imps", ru.ispras.microtesk.model.data.Type.class.getName());
    st.add("imps", ru.ispras.microtesk.model.memory.Memory.class.getName());
  }

  private void buildBody(final STGroup group, final ST st) {
    final ST tCore = group.getInstanceOf("temporary_variables");
    tCore.add("class", CLASS_NAME);

    for (final MemoryExpr memory : ir.getMemory().values()) {
      if (memory.getKind() != Memory.Kind.VAR) {
        continue;
      }

      tCore.add("names", memory.getName());

      final ST stMemoryDef = StbProcessingElement.buildMemoryLine(group, memory);
      tCore.add("defs", stMemoryDef);
    }

    st.add("members", tCore);
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildBody(group, st);

    return st;
  }
}
