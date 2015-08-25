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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBStruct implements STBuilder {
  public static final Class<?> STRUCT_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuStruct.class;

  public static final Class<?> INTEGER_CLASS =
      ru.ispras.microtesk.basis.solver.integer.IntegerVariable.class;

  private final String packageName;
  private final Type type;

  public STBStruct(final String packageName, final Type type) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(type);

    this.packageName = packageName;
    this.type = type;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildFields(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", type.getId()); 
    st.add("pack", packageName);
    st.add("ext",  STRUCT_CLASS.getSimpleName());

    st.add("imps", INTEGER_CLASS.getName());
    st.add("imps", STRUCT_CLASS.getName());
  }

  private void buildFields(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("constructor");

    stConstructor.add("name", type.getId());
    buildFieldDecls(type, st, stConstructor, group);

    stConstructor.add("stmts", "");
    buildAddField(type, stConstructor, group);

    st.add("members", "");
    st.add("members", stConstructor);
  }

  protected static void buildFieldDecls(
      final Type structType,
      final ST st,
      final ST stConstructor,
      final STGroup group) {
    for (final Map.Entry<String, Type> field : structType.getFields().entrySet()) {
      final String name = field.getKey();
      final Type type = field.getValue();
      buildFieldDecl(name, type, st, stConstructor, group);
    }
  }

  protected static void buildFieldDecl(
      final String name,
      final Type type,
      final ST st,
      final ST stConstructor,
      final STGroup group) {
    final ST fieldDecl = group.getInstanceOf("field_decl");
    final ST fieldDef;
    if (type.isStruct()) {
      fieldDecl.add("type", type.getId());

      fieldDef = group.getInstanceOf("field_def_struct");
      fieldDef.add("type", type.getId());
    } else {
      fieldDecl.add("type", INTEGER_CLASS.getSimpleName());

      fieldDef = group.getInstanceOf("field_def_var");
      fieldDef.add("size", type.getBitSize());
    }

    fieldDecl.add("name", name);
    fieldDef.add("name", name);

    st.add("members", fieldDecl);
    stConstructor.add("stmts", fieldDef);
  }

  protected static void buildAddField(
      final Type structType,
      final ST stConstructor,
      final STGroup group) {
    for (final String fieldName : structType.getFields().keySet()) {
      final ST addField = group.getInstanceOf("add_field");
      addField.add("name", fieldName);
      stConstructor.add("stmts", addField);
    }
  }
}
