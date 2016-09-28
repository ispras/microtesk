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

package ru.ispras.microtesk.translator.nml.generation.metadata;

import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveInfo;

final class STBOperation implements STBuilder {
  private final String modelName;
  private final PrimitiveAND primitive;

  public STBOperation(final String modelName, final PrimitiveAND primitive) {
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
    st.add("ext", MetaOperation.class.getSimpleName());
    st.add("imps", MetaOperation.class.getName());
    st.add("instance", "instance");
  }

  private void buildBody(final ST st, final STGroup group) {
    final PrimitiveInfo info = primitive.getInfo();

    final ST stConstructor = group.getInstanceOf("constructor");

    stConstructor.add("name", primitive.getName());
    stConstructor.add("args", "\"" + primitive.getName() + "\"");
    stConstructor.add("args", "\"" + primitive.getName() + "\"");
    stConstructor.add("args", primitive.isRoot());
    stConstructor.add("args", info.isBranch());
    stConstructor.add("args", info.isConditionalBranch());
    stConstructor.add("args", info.canThrowException());
    stConstructor.add("args", info.isLoad());
    stConstructor.add("args", info.isStore());
    stConstructor.add("args", info.getBlockSize());

    for (final Map.Entry<String, Primitive> entry : primitive.getArguments().entrySet()) {
      final String name = entry.getKey();
      final Primitive type = entry.getValue();
      final ArgumentMode mode = info.getArgUsage(name);

      final ST stArgument = group.getInstanceOf("add_argument");

      stArgument.add("type", MetaArgument.class.getSimpleName());
      stArgument.add("args", "\"" + name + "\"");
      stArgument.add("args", type.getName());

      if (type.getKind() != Primitive.Kind.IMM) {
        stArgument.add("args", String.format("%s.%s", ArgumentMode.class.getSimpleName(), mode));
      }

      stConstructor.add("stmts", stArgument);
    }

    st.add("members", "");
    st.add("members", stConstructor);
  }
}
