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
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

final class StbPrimitive implements StringTemplateBuilder {
  private final Ir ir;
  private final Primitive primitive;

  private final Set<String> imports = new HashSet<>();

  public StbPrimitive(final Ir ir, final Primitive primitive) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(primitive);

    this.ir = ir;
    this.primitive = primitive;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("primitive_file");

    st.add("time", new Date().toString());
    st.add("name", WhymlUtils.getModuleName(primitive.getName()));

    addImport(st, String.format("%s.state.State", ir.getModelName()));

    return st;
  }

  private void addImport(final ST st, final String name) {
    if (!imports.contains(name)) {
      st.add("imps", name);
      imports.add(name);
    }
  }
}
