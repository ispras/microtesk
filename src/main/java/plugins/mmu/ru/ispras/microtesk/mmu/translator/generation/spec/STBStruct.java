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
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuStruct;
import ru.ispras.microtesk.translator.generation.STBuilder;

final class STBStruct implements STBuilder {
  public static final Class<?> STRUCT_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuStruct.class;

  public static final Class<?> INTEGER_CLASS =
      ru.ispras.microtesk.basis.solver.integer.IntegerVariable.class;

  private final String packageName;
  private final Type type;

  public  STBStruct(final String packageName, final Type type) {
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

    st.add("imps", InvariantChecks.class.getName());
    st.add("imps", INTEGER_CLASS.getName());
    st.add("imps", STRUCT_CLASS.getName());
  }

  private void buildFields(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("constructor");
    stConstructor.add("name", type.getId());

    for (final Map.Entry<String, Type> field : type.getFields().entrySet()) {
      final String name = field.getKey();
      final Type type = field.getValue();

      final ST fieldDecl = group.getInstanceOf("field_decl");
      final ST fieldDef;
      if (type.isStruct()) {
        fieldDecl.add("type", MmuStruct.class.getSimpleName());

        fieldDef = group.getInstanceOf("field_def_struct");
        fieldDef.add("type", type.getId());
      } else {
        fieldDecl.add("type", IntegerVariable.class.getSimpleName());

        fieldDef = group.getInstanceOf("field_def_var");
        fieldDef.add("size", type.getBitSize());
      }

      fieldDecl.add("name", name);
      fieldDef.add("name", name);

      st.add("members", fieldDecl);
      stConstructor.add("stmts", fieldDef);
    }

    stConstructor.add("stmts", "");
    for (final String fieldName : type.getFields().keySet()) {
      final ST addField = group.getInstanceOf("add_field");
      addField.add("name", fieldName);
      stConstructor.add("stmts", addField);
    }

    st.add("members", "");
    st.add("members", stConstructor);
  }
}
