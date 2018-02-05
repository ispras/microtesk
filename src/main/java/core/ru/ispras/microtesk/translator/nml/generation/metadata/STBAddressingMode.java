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

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.ArgumentMode;
import ru.ispras.microtesk.model.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.metadata.MetaArgument;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveInfo;

import java.util.Map;

final class STBAddressingMode implements STBuilder {
  private final String modelName;
  private final PrimitiveAND primitive;

  public STBAddressingMode(final String modelName, final PrimitiveAND primitive) {
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
    st.add("ext", MetaAddressingMode.class.getSimpleName());
    st.add("imps", ArgumentMode.class.getName());
    st.add("imps", MetaAddressingMode.class.getName());
    st.add("imps", MetaArgument.class.getName());
    st.add("imps", ru.ispras.microtesk.model.data.Type.class.getName());
    st.add("simps", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".TypeDefs", modelName));
    st.add("instance", "instance");
  }

  private void buildBody(final ST st, final STGroup group) {
    final PrimitiveInfo info = primitive.getInfo();

    final ST stConstructor = group.getInstanceOf("constructor");
    buildName(primitive.getName(), stConstructor);

    stConstructor.add("args",
        primitive.getReturnType() != null ? primitive.getReturnType().getJavaText() : "null");

    stConstructor.add("args",
        primitive.getModifier() == Primitive.Modifier.LABEL);

    buildFlags(info, stConstructor);
    buildArguments(group, stConstructor, primitive);

    st.add("members", "");
    st.add("members", stConstructor);
  }

  public static void buildName(final String name, final ST stConstructor) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(stConstructor);

    stConstructor.add("name", name);
    stConstructor.add("args", "\"" + name + "\"");
  }

  private static void buildFlags(
      final PrimitiveInfo info,
      final ST stConstructor) {
    stConstructor.add("args", info.canThrowException());
    stConstructor.add("args", info.isMemoryReference());
    stConstructor.add("args", info.isLoad());
    stConstructor.add("args", info.isStore());
    stConstructor.add("args", info.getBlockSize());
  }

  public static void buildArguments(
      final STGroup group,
      final ST stConstructor,
      final PrimitiveAND primitive) {
    InvariantChecks.checkNotNull(group);
    InvariantChecks.checkNotNull(stConstructor);
    InvariantChecks.checkNotNull(primitive);

    final PrimitiveInfo info = primitive.getInfo();
    for (final Map.Entry<String, Primitive> entry : primitive.getArguments().entrySet()) {
      final String name = entry.getKey();
      final Primitive type = entry.getValue();
      final ArgumentMode mode = info.getArgUsage(name);

      final ST stArgument = group.getInstanceOf("add_argument");

      stArgument.add("type", MetaArgument.class.getSimpleName());
      stArgument.add("args", "\"" + name + "\"");

      if (type.getKind() == Primitive.Kind.IMM) {
        stArgument.add("args", type.getName());
      } else {
        stArgument.add("args", type.getName() + ".get()");
        stArgument.add("args", String.format("%s.%s", ArgumentMode.class.getSimpleName(), mode));
      }

      stConstructor.add("stmts", stArgument);
    }
  }
}
