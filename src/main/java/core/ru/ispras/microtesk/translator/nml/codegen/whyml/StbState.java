/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.whyml;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryResource;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

final class StbState extends StbBase implements StringTemplateBuilder {
  public static final String FILE_NAME = "State";

  private final Ir ir;

  public StbState(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
    this.ir = ir;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("state_file");

    st.add("time", new Date().toString());
    st.add("name", FILE_NAME);

    addImport(st, "mach.int.Int32");
    addImport(st, "mach.array.Array32");

    buildTypes(st);
    buildMemoryStorages(st);

    return st;
  }

  private void buildTypes(final ST st) {
    for (final Map.Entry<String, Type> entry : ir.getTypes().entrySet()) {
      final String name = WhymlUtils.getTypeName(entry.getKey());
      final Type type = entry.getValue();

      final int typeSize = type.getBitSize();
      final String typeName = WhymlUtils.getTypeFullName(typeSize);

      BVTheoryGenerator.getInstance().generate(type.getBitSize());
      addImport(st, typeName);

      st.add("types", String.format("%s = %s", name, WhymlUtils.getTypeName(typeSize)));
    }
  }

  private void buildMemoryStorages(final ST st) {
    for (final Map.Entry<String, MemoryResource> entry : ir.getMemory().entrySet()) {
      final MemoryResource memory = entry.getValue();

      final String name = memory.getName().toLowerCase();
      final BigInteger length = memory.getSize();

      final Type type = memory.getType();
      final String typeName = makeTypeName(st, type);

      st.add("var_names", name);
      st.add("var_types", typeName);
      st.add("var_lengths", length);
      st.add("var_arrays", !length.equals(BigInteger.ONE));
    }
  }
}
