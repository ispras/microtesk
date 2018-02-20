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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.model.ArgumentMode;
import ru.ispras.microtesk.model.metadata.MetaArgument;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;

final class StbOperation implements StringTemplateBuilder {
  private final String modelName;
  private final PrimitiveAND primitive;

  public StbOperation(final String modelName, final PrimitiveAND primitive) {
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
    st.add("imps", ru.ispras.microtesk.model.data.Type.class.getName());
    st.add("simps", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".TypeDefs", modelName));
    st.add("instance", "instance");
  }

  private void buildBody(final ST st, final STGroup group) {
    final PrimitiveInfo info = primitive.getInfo();

    final ST stConstructor = group.getInstanceOf("constructor");
    StbAddressingMode.buildName(primitive.getName(), stConstructor);

    stConstructor.add("args", "\"" + primitive.getName() + "\"");
    stConstructor.add("args", primitive.isRoot());

    buildFlags(info, stConstructor);
    StbAddressingMode.buildArguments(group, stConstructor, primitive);

    st.add("members", "");
    st.add("members", stConstructor);

    buildShortcuts(group, st, stConstructor, primitive);
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
      final String className = getShortcutClassName(shortcut);
      buildShortcutClass(group, st, shortcut, className);
      for (final String contextName: shortcut.getContextName()) {
        buildShortcut(group, stConstructor, className, contextName);
      }
    }
  }

  private static void buildShortcutClass(
      final STGroup group,
      final ST st,
      final Shortcut shortcut,
      final String className) {
    final ST stShortcut = group.getInstanceOf("class_ext");

    stShortcut.add("modifs", new String[] {"private", "static", "final"});
    stShortcut.add("name", className);
    stShortcut.add("ext", MetaOperation.class.getSimpleName());
    stShortcut.add("instance", "instance");

    final ST stConstructor = group.getInstanceOf("constructor");

    stConstructor.add("name", className);
    stConstructor.add("args", "\"" + shortcut.getName() + "\"");

    stConstructor.add("args", "\"" + shortcut.getEntry().getName() + "\"");
    stConstructor.add("args", shortcut.getEntry().isRoot());

    buildFlags(shortcut.getInfo(), stConstructor);
    buildArguments(group, stConstructor, shortcut);

    stShortcut.add("members", "");
    stShortcut.add("members", stConstructor);

    st.add("members", "");
    st.add("members", stShortcut);
  }

  private static void buildArguments(
      final STGroup group,
      final ST stConstructor,
      final Shortcut shortcut) {
    for (final Shortcut.Argument argument : shortcut.getArguments()) {
      final String name = argument.getUniqueName();
      final Primitive type = argument.getType();
      final ArgumentMode mode = argument.getSource().getInfo().getArgUsage(argument.getName());

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

  public static void buildShortcut(
      final STGroup group,
      final ST stConstructor,
      final String className,
      final String contextName) {
    final ST stShortcut = group.getInstanceOf("add_shortcut");

    stShortcut.add("context", contextName);
    stShortcut.add("operation", className + ".get()");

    stConstructor.add("stmts", stShortcut);
  }

  private static String getShortcutClassName(final Shortcut shortcut) {
    return shortcut.getEntry().getName() + "_" + shortcut.getTarget().getName();
  }
}
