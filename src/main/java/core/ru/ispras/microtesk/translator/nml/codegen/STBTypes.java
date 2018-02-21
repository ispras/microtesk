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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

import java.util.Map;

final class STBTypes implements StringTemplateBuilder {
  public static final String CLASS_NAME = "TypeDefs";
  private final Ir ir;

  public STBTypes(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
    this.ir = ir;
  }

  private void buildHeader(final ST st) {
    st.add("name", CLASS_NAME);
    st.add("pack", String.format(PackageInfo.MODEL_PACKAGE_FORMAT, ir.getModelName()));
    st.add("imps", ru.ispras.microtesk.model.data.Type.class.getName());

    st.add("members", String.format("private %s() {}", CLASS_NAME));
    if (!ir.getTypes().isEmpty()) {
      st.add("members", "");
    }
  }

  private void buildTypes(final STGroup group, final ST st) {
    for (final Map.Entry<String, Type> type : ir.getTypes().entrySet()) {
      final ST tType = group.getInstanceOf("type_alias");

      final String name = type.getKey();
      final String javaText = type.getValue().getJavaText();

      tType.add("name", name);
      tType.add("alias", String.format("Type.def(\"%s\", %s)", name, javaText));

      st.add("members", tType);
    }
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildTypes(group, st);

    return st;
  }
}
