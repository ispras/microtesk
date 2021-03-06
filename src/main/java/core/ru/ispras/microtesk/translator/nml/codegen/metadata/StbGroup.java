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

package ru.ispras.microtesk.translator.nml.codegen.metadata;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.metadata.MetaGroup;
import ru.ispras.microtesk.translator.codegen.PackageInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOr;

final class StbGroup implements StringTemplateBuilder {
  private final String modelName;
  private final PrimitiveOr primitive;

  public StbGroup(final String modelName, final PrimitiveOr primitive) {
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(primitive);
    this.modelName = modelName;
    this.primitive = primitive;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildBody(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", primitive.getName());
    st.add("pack", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".metadata", modelName));
    st.add("ext", MetaGroup.class.getSimpleName());
    st.add("imps", MetaGroup.class.getName());
    st.add("instance", "instance");
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("constructor");
    stConstructor.add("name", primitive.getName());

    stConstructor.add("args", String.format("%s.%s.%s",
        MetaGroup.class.getSimpleName(),
        MetaGroup.Kind.class.getSimpleName(),
        primitive.getKind() == Primitive.Kind.OP ? MetaGroup.Kind.OP : MetaGroup.Kind.MODE
        ));
    stConstructor.add("args", String.format("\"%s\"", primitive.getName()));

    for (final Primitive item : primitive.getOrs()) {
      if (item.getModifier() != Primitive.Modifier.INTERNAL) {
        stConstructor.add("args", item.getName() + ".get()");
      }
    }

    st.add("members", "");
    st.add("members", stConstructor);
  }
}
