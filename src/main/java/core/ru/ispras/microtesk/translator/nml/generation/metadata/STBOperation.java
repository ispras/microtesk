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
import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;

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
    st.add("imps", ArgumentMode.class.getName());
    st.add("imps", MetaArgument.class.getName());
    st.add("imps", MetaOperation.class.getName());
    st.add("imps", ru.ispras.microtesk.model.api.data.Type.class.getName());
    st.add("simps", String.format(PackageInfo.SHARED_CLASS_FORMAT, modelName));
    st.add("instance", "instance");
  }

  private void buildBody(final ST st, final STGroup group) {
    final PrimitiveInfo info = primitive.getInfo();

    final ST stConstructor = group.getInstanceOf("constructor");
    STBAddressingMode.buildName(primitive.getName(), stConstructor);

    stConstructor.add("args", "\"" + primitive.getName() + "\"");
    stConstructor.add("args", primitive.isRoot());

    buildFlags(info, stConstructor);
    STBAddressingMode.buildArguments(group, stConstructor, primitive);
    buildShortcuts(group, st, stConstructor, primitive);

    st.add("members", "");
    st.add("members", stConstructor);
  }

  private static void buildFlags(
      final PrimitiveInfo info,
      final ST stConstructor) {
    stConstructor.add("args", info.isBranch());
    stConstructor.add("args", info.isConditionalBranch());
    stConstructor.add("args", info.canThrowException());
    stConstructor.add("args", info.isLoad());
    stConstructor.add("args", info.isStore());
    stConstructor.add("args", info.getBlockSize());
  }

  private static void buildShortcuts(
      final STGroup group,
      final ST st,
      final ST stConstructor,
      final PrimitiveAND primitive) {
    if (!primitive.getShortcuts().isEmpty()) {
      stConstructor.add("stmts", "");
    }

    for (final Shortcut shortcut : primitive.getShortcuts()) {
      buildShortcutClass(group, st, shortcut);
      for (final String contextName: shortcut.getContextName()) {
        buildShortcut(group, stConstructor, shortcut, contextName);
      }
    }
  }

  private static void buildShortcutClass(
      final STGroup group,
      final ST st,
      final Shortcut shortcut) {
    // TODO Auto-generated method stub
    
  }

  public static void buildShortcut(
      final STGroup group,
      final ST stConstructor,
      final Shortcut shortcut,
      final String contextName) {
    final ST stShortcut = group.getInstanceOf("add_shortcut");

    stShortcut.add("context", contextName);
    stShortcut.add("operation", shortcut.getName());

    //stConstructor.add("stmts", stShortcut);
  }
}
